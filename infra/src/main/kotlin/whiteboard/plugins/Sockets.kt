package whiteboard.plugins

import io.ktor.server.websocket.*
import io.ktor.websocket.*
import java.time.Duration
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.*
import whiteboard.EntityControl
import whiteboard.UserControl
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import kotlin.collections.LinkedHashSet

fun Application.configureSockets() {
    install(WebSockets) {
        pingPeriod = Duration.ofSeconds(15)
        timeout = Duration.ofSeconds(15)
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }
    routing {
        val connections = Collections.synchronizedSet<Connection?>(LinkedHashSet())
        webSocket("/chat") {
            println("Chat route in Sockets.kt")
            println("Adding user!")
            val thisConnection = Connection(this)
            connections += thisConnection
            try {
                send("You are connected! There are ${connections.count()} users here.")
                for (frame in incoming) {
                    frame as? Frame.Text ?: continue
                    val receivedText = frame.readText()
                    val textWithUsername = "[${thisConnection.name}]: $receivedText"
                    connections.forEach {
                        it.session.send(textWithUsername)
                    }
                }
            } catch (e: Exception) {
                println(e.localizedMessage)
                close(CloseReason(CloseReason.Codes.INTERNAL_ERROR, "An exception occurred, closing connection"))
            } finally {
                println("Removing $thisConnection!")
                connections -= thisConnection
            }
        }


        // API calls below

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
                    call.respondText { "+" + user.token() }
                } else {
                    call.respondText { "-" + "Failed to create new user" }
                }
            }
        }
        post("/user/login") {
            val formParameters = call.receiveParameters()
            val user = UserControl.login(
                formParameters.getOrFail<String>("username"),
                formParameters.getOrFail<String>("password")
            )
            if (user != null) {
                call.respondText { "+" + user.token() }
            } else {
                call.respondText { "-" + "Failed to login" }
            }
        }
        post("/user/autologin") {
            val formParameters = call.receiveParameters()
            val user = UserControl.loginWithToken(formParameters.getOrFail<String>("token"))
            if (user != null) {
                call.respondText { "+" + user.username }
            } else {
                call.respondText { "-" + "Failed to login" }
            }
        }
        post("/entity/create") {
            val formParameters = call.receiveParameters()
            val entity = EntityControl.create(
                formParameters.getOrFail<String>("id"),
                formParameters.getOrFail<Int>("roomId"),
                formParameters.getOrFail<String>("descriptor"),
                formParameters.getOrFail<Long>("timestamp")
            )
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


class Connection(val session: DefaultWebSocketSession) {
    companion object {
        val lastId= AtomicInteger(0)
    }
    val name = "user${lastId.getAndIncrement()}"
}



