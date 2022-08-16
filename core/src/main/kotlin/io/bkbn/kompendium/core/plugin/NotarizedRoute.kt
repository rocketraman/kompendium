package io.bkbn.kompendium.core.plugin

import io.bkbn.kompendium.core.attribute.KompendiumAttributes
import io.bkbn.kompendium.core.metadata.DeleteInfo
import io.bkbn.kompendium.core.metadata.GetInfo
import io.bkbn.kompendium.core.metadata.HeadInfo
import io.bkbn.kompendium.core.metadata.OptionsInfo
import io.bkbn.kompendium.core.metadata.PatchInfo
import io.bkbn.kompendium.core.metadata.PostInfo
import io.bkbn.kompendium.core.metadata.PutInfo
import io.bkbn.kompendium.core.util.Helpers.addToSpec
import io.bkbn.kompendium.core.util.SpecConfig
import io.bkbn.kompendium.oas.path.Path
import io.bkbn.kompendium.oas.payload.Parameter
import io.ktor.server.application.ApplicationCallPipeline
import io.ktor.server.application.Hook
import io.ktor.server.application.createRouteScopedPlugin
import io.ktor.server.routing.Route

object NotarizedRoute {

  class Config : SpecConfig {
    override var tags: Set<String> = emptySet()
    override var parameters: List<Parameter> = emptyList()
    override var get: GetInfo? = null
    override var post: PostInfo? = null
    override var put: PutInfo? = null
    override var delete: DeleteInfo? = null
    override var patch: PatchInfo? = null
    override var head: HeadInfo? = null
    override var options: OptionsInfo? = null
    override var security: Map<String, List<String>>? = null
    internal var path: Path? = null
  }

  private object InstallHook : Hook<(ApplicationCallPipeline) -> Unit> {
    override fun install(pipeline: ApplicationCallPipeline, handler: (ApplicationCallPipeline) -> Unit) {
      handler(pipeline)
    }
  }

  operator fun invoke() = createRouteScopedPlugin(
    name = "NotarizedRoute",
    createConfiguration = ::Config
  ) {

    // This is required in order to introspect the route path
    on(InstallHook) {
      val route = it as? Route ?: return@on
      val spec = application.attributes[KompendiumAttributes.openApiSpec]
      val routePath = route.calculateRoutePath()
      require(spec.paths[routePath] == null) {
        """
        The specified path ${Parameter.Location.path} has already been documented!
        Please make sure that all notarized paths are unique
      """.trimIndent()
      }
      spec.paths[routePath] = pluginConfig.path!!
    }

    val spec = application.attributes[KompendiumAttributes.openApiSpec]

    val path = Path()
    path.parameters = pluginConfig.parameters

    pluginConfig.get?.addToSpec(path, spec, pluginConfig)
    pluginConfig.delete?.addToSpec(path, spec, pluginConfig)
    pluginConfig.head?.addToSpec(path, spec, pluginConfig)
    pluginConfig.options?.addToSpec(path, spec, pluginConfig)
    pluginConfig.post?.addToSpec(path, spec, pluginConfig)
    pluginConfig.put?.addToSpec(path, spec, pluginConfig)
    pluginConfig.patch?.addToSpec(path, spec, pluginConfig)

    pluginConfig.path = path
  }

  private fun Route.calculateRoutePath() = toString().replace(Regex("/\\(.+\\)"), "")
}
