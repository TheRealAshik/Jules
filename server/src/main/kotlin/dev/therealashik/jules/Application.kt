package dev.therealashik.jules

import dev.therealashik.jules.sdk.JulesApiClient
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

fun main() {
    embeddedServer(Netty, port = SERVER_PORT, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    install(WebSockets) {
        pingPeriod = 15.seconds
        timeout = 15.seconds
        maxFrameSize = Long.MAX_VALUE
        masking = true
    }

    routing {
        get("/") {
            call.respondText("Ktor: ${Greeting().greet()}")
        }

        webSocket("/v1alpha/sessions/{sessionId}/activities/watch") {
            val sessionId = call.parameters["sessionId"] ?: return@webSocket close(
                CloseReason(CloseReason.Codes.CANNOT_ACCEPT, "Missing sessionId")
            )
            val apiKey = call.request.headers["x-goog-api-key"] ?: return@webSocket close(
                CloseReason(CloseReason.Codes.CANNOT_ACCEPT, "Missing API Key")
            )

            val apiClient = JulesApiClient(apiKey)
            val seenActivityIds = mutableSetOf<String>()

            try {
                while (true) {
                    val response = apiClient.listActivities(sessionId, pageSize = 10)
                    val newActivities = response.activities.filter { it.id !in seenActivityIds }

                    for (activity in newActivities.reversed()) {
                        sendSerialized(activity)
                        seenActivityIds.add(activity.id)
                    }

                    delay(2000)
                }
            } catch (e: Exception) {
                // Connection closed or error
            } finally {
                apiClient.close()
            }
        }
    }
}
