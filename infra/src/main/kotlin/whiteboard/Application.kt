package whiteboard

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import whiteboard.plugins.configureEndpoints
import whiteboard.plugins.configureSockets

fun main() {
    DatabaseFactory.init()
    embeddedServer(Netty, port = 8080, module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    configureSockets()
    configureEndpoints()
}
