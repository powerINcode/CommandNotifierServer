package ru.powerman23rus

import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.features.ContentNegotiation
import io.ktor.gson.gson
import io.ktor.http.ContentType
import io.ktor.http.HeaderValue
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.routing
import java.lang.Exception


fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

private val clients: HashMap<String, String> = hashMapOf()
val client = HttpClient(CIO)

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {

    install(ContentNegotiation) {
        gson {
        }
    }

    routing {
        get("/") {

            call.respondText("HELLO WORLD, ${call.parameters["name"]}!", contentType = ContentType.Text.Plain)
        }

        get("/json/gson") {
            call.respond(mapOf("hello" to "world"))
        }

        post("/register/{token}/{name}") {
            val token = call.parameters["token"].orEmpty()
            val name = call.parameters["name"].orEmpty()
            clients[name] = token
            call.respond(HttpStatusCode.OK)
        }

        post("/complete/{name}") {
            val token = clients[call.parameters["name"].orEmpty()] ?: return@post
            val result = """{
                     "to" : "$token",
                     "notification" : {
                     "body" : "Complete",
                     "title" : "Your task is complete"
                     "content_available" : true,
                     "priority" : "high",
                     }
                    }"""

            try {
                client.post<Unit>("https://fcm.googleapis.com/fcm/send") {
                    header("Content-Type", "application/json")
                    header("Authorization", "key=AAAAVJAf9is:APA91bGwlOKs1mH1Y6aH-uZCbkLzuWSi8KyRrdKz5EctxfIvNjYilleUuShNepCzZYb29B510tIhYZyC0T6ZgjvMmdaAoA1ewrMOIKBiz8Ix4P94LQA6twWNDm-37aBwdixQ1VLTvEUi")
                    body = result
                }

                val test = 0
            } catch (e : Exception) {
                call.respond(HttpStatusCode.InternalServerError)
            }

            call.respond(HttpStatusCode.OK)
        }
    }
}

