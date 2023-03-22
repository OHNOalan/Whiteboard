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
                val username = formParameters.getOrFail<String>("username")
                val password = formParameters.getOrFail<String>("password")
                if (username.length < 5 || password.length < 5) {
                    call.respondText { "-Length of username or password is too short." }
                } else {
                    val user = UserControl.create(username, password)
                    if (user != null) {
                        call.respondText { "+" + user!!.token() }
                    } else {
                        call.respondText { "-" + "Failed to create new user" }
                    }
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
            post("/entity/create") {
                val formParameters = call.receiveParameters()
                val entity = EntityControl.create(formParameters.getOrFail<String>("id"), formParameters.getOrFail<Int>("roomId"), formParameters.getOrFail<String>("descriptor"), formParameters.getOrFail<Long>("timestamp"))
                call.respondText { if (entity != null) "+" else "-" }
            }
            post("/entity/load") {
                val formParameters = call.receiveParameters()
                call.respondText { EntityControl.load(formParameters.getOrFail<Int>("roomId")) }
            }
            post("/entity/delete") {
                val formParameters = call.receiveParameters()
                call.respondText { if (EntityControl.delete(formParameters.getOrFail<String>("id"))) "+" else "-" }
            }
            post("/entity/update") {
                // TODO implement this for list of items
                val formParameters = call.receiveParameters()
                call.respondText { if (EntityControl.delete(formParameters.getOrFail<String>("id"))) "+" else "-" }
            }
        }
    }
        .start(wait = true)
}
