package whiteboard

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.*

fun main() {
    DatabaseFactory.init()
    embeddedServer(Netty, port = 8080) {
        routing {
            get("/") {
                call.respondText { "Server online!" }
            }
            post("/user/create") {
                val formParameters = call.receiveParameters()
                val user = UserControl.create(formParameters.getOrFail<String>("username"), formParameters.getOrFail<String>("password"))
                if (user != null) {
                    call.respondText { "+" + user!!.token() }
                } else {
                    call.respondText { "-" + "Failed to create new user" }
                }
            }
            post("/user/login") {
                val formParameters = call.receiveParameters()
                val user = UserControl.login(formParameters.getOrFail<String>("username"), formParameters.getOrFail<String>("password"))
                if (user != null) {
                    call.respondText { "+" + user!!.token() }
                } else {
                    call.respondText { "-" + "Failed to login" }
                }
            }
            post("/user/autologin") {
                val formParameters = call.receiveParameters()
                val user = UserControl.loginWithToken(formParameters.getOrFail<String>("token"))
                if (user != null) {
                    call.respondText { "+" + user!!.username }
                } else {
                    call.respondText { "-" + "Failed to login" }
                }
            }
        }
    }
        .start(wait = true)
}
