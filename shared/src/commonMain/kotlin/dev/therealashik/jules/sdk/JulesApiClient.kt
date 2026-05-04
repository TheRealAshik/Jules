package dev.therealashik.jules.sdk

import dev.therealashik.jules.sdk.models.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

class JulesApiClient(
    private val apiKey: String,
    httpClient: HttpClient? = null
) {
    private val client = (httpClient ?: HttpClient()).config {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
                encodeDefaults = true
                explicitNulls = false
            })
        }
        defaultRequest {
        }
    }

    private val baseUrl = "https://jules.googleapis.com/v1alpha"

    private suspend inline fun <reified T> HttpResponse.bodyOrThrow(): T {
        if (!status.isSuccess()) {
            throw Exception("API Error: ${status.value} - ${bodyAsText()}")
        }
        return body()
    }

    suspend fun createSession(request: CreateSessionRequest): Session {
        val response = client.post("$baseUrl/sessions") {
            setBody(request)
        }
        return response.bodyOrThrow()
    }

    suspend fun listSessions(pageSize: Int = 30, pageToken: String? = null): ListSessionsResponse {
        val response = client.get("$baseUrl/sessions") {
            parameter("pageSize", pageSize)
            if (pageToken != null) {
                parameter("pageToken", pageToken)
            }
        }
        return response.bodyOrThrow()
    }

    suspend fun getSession(sessionId: String): Session {
        val response = client.get("$baseUrl/sessions/$sessionId") {
        }
        return response.bodyOrThrow()
    }

    suspend fun deleteSession(sessionId: String) {
        val response = client.delete("$baseUrl/sessions/$sessionId") {
        }
        if (!response.status.isSuccess()) {
            throw Exception("API Error: ${response.status.value} - ${response.bodyAsText()}")
        }
    }

    suspend fun sendMessage(sessionId: String, request: SendMessageRequest): SendMessageResponse {
        val response = client.post("$baseUrl/sessions/$sessionId:sendMessage") {
            setBody(request)
        }
        return response.bodyOrThrow()
    }

    suspend fun approvePlan(sessionId: String): ApprovePlanResponse {
        val response = client.post("$baseUrl/sessions/$sessionId:approvePlan") {
            setBody(ApprovePlanRequest())
        }
        return response.bodyOrThrow()
    }

    suspend fun listActivities(sessionId: String, pageSize: Int = 50, pageToken: String? = null, createTime: String? = null): ListActivitiesResponse {
        val response = client.get("$baseUrl/sessions/$sessionId/activities") {
            parameter("pageSize", pageSize)
            if (pageToken != null) {
                parameter("pageToken", pageToken)
            }
            if (createTime != null) {
                parameter("createTime", createTime)
            }
        }
        return response.bodyOrThrow()
    }

    suspend fun getActivity(sessionId: String, activityId: String): Activity {
        val response = client.get("$baseUrl/sessions/$sessionId/activities/$activityId") {
        }
        return response.bodyOrThrow()
    }

    suspend fun listSources(pageSize: Int = 30, pageToken: String? = null, filter: String? = null): ListSourcesResponse {
        val response = client.get("$baseUrl/sources") {
            parameter("pageSize", pageSize)
            if (pageToken != null) {
                parameter("pageToken", pageToken)
            }
            if (filter != null) {
                parameter("filter", filter)
            }
        }
        return response.bodyOrThrow()
    }

    suspend fun getSource(sourceId: String): Source {
        val response = client.get("$baseUrl/sources/$sourceId") {
        }
        return response.bodyOrThrow()
    }

    fun close() {
        client.close()
    }
}
