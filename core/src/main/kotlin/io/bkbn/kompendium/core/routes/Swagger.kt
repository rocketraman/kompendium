package io.bkbn.kompendium.core.routes

import io.ktor.server.html.respondHtml
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import kotlinx.html.body
import kotlinx.html.div
import kotlinx.html.head
import kotlinx.html.id
import kotlinx.html.link
import kotlinx.html.meta
import kotlinx.html.script
import kotlinx.html.title
import kotlinx.html.unsafe

/**
 * Provides an out-of-the-box route to view docs using Swagger
 * @see <a href="https://swagger.io/specification/">Swagger OpenApi Specification</a>
 * for the latest supported open api version.
 * @param pageTitle Webpage title you wish to be displayed on your docs
 * @param path path to docs resource
 * @param specUrl url to point Swagger to the OpenAPI json document
 * @param swaggerVersion version of swagger-ui distribution
 */
fun Route.swagger(
  pageTitle: String = "Docs",
  path: String = "/swagger-ui",
  specUrl: String = "/openapi.json",
  swaggerVersion: String? = null,
  cdn: String = "https://cdn.jsdelivr.net/npm",
) {
  val swaggerVersionSuffix = if (swaggerVersion == null) "" else "@$swaggerVersion"

  route(path) {
    get {
      call.respondHtml {
        head {
          title {
            +pageTitle
          }
          meta {
            charset = "utf-8"
          }
          meta {
            name = "viewport"
            content = "width=device-width, initial-scale=1"
          }
          link {
            href = "$cdn/swagger-ui-dist$swaggerVersionSuffix/swagger-ui.css"
            rel = "stylesheet"
          }
        }
        body {
          div {
            id = "swagger-ui"
          }
          script {
            src = "$cdn/swagger-ui-dist$swaggerVersionSuffix/swagger-ui-standalone-preset.js"
          }
          script {
            src = "$cdn/swagger-ui-dist$swaggerVersionSuffix/swagger-ui-bundle.js"
          }
          unsafe {
            +"""
              <script>
                window.onload = function () {
                  // Build a system
                  const ui = SwaggerUIBundle({
                    url: "$specUrl",
                    dom_id: '#swagger-ui',
                    deepLinking: true,
                    presets: [
                      SwaggerUIBundle.presets.apis,
                      SwaggerUIStandalonePreset
                    ],
                    plugins: [
                      SwaggerUIBundle.plugins.DownloadUrl
                    ],
                    layout: "StandaloneLayout",
                  })
                  window.ui = ui
                }
              </script>
            """.trimIndent()
          }
        }
      }
    }
  }
}
