package whiteboard.plugins

import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.*
import whiteboard.AppUtils
import whiteboard.models.RoomControl
import whiteboard.models.UserControl


fun Application.configureEndpoints() {
    routing {
        get("/") {
            call.respondText { AppUtils.generateResponse(true, "Server online!") }
        }
        post("/user/create") {
            val formParameters = call.receiveParameters()
            val username = formParameters.getOrFail<String>("username")
            val password = formParameters.getOrFail<String>("password")
            if (username.length < 5) {
                call.respondText {
                    AppUtils.generateResponse(
                        false,
                        "Username length must be greater than 5 characters."
                    )
                }
                return@post
            }
            if (username.length > 16) {
                call.respondText {
                    AppUtils.generateResponse(
                        false,
                        "Username length must be less than or equal to 16 characters."
                    )
                }
                return@post
            }
            if (password.length < 5) {
                call.respondText {
                    AppUtils.generateResponse(
                        false,
                        "Password length must be greater than 5 characters."
                    )
                }
                return@post
            }
            if (UserControl.getByUsername(username) != null) {
                call.respondText {
                    AppUtils.generateResponse(
                        false,
                        "A user with this username already exists."
                    )
                }
                return@post
            }
            val user = UserControl.create(username, password)
            if (user != null) {
                call.respondText {
                    AppUtils.generateResponse(
                        true,
                        user.token(),
                        RoomControl.generateRoom()
                    )
                }
            } else {
                call.respondText {
                    AppUtils.generateResponse(
                        false,
                        "Failed to create user. Please try again later."
                    )
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
                call.respondText {
                    AppUtils.generateResponse(
                        true,
                        user.token(),
                        RoomControl.generateRoom()
                    )
                }
            } else {
                call.respondText {
                    AppUtils.generateResponse(
                        false,
                        "Incorrect username or password."
                    )
                }
            }
        }
        post("/user/autologin") {
            val formParameters = call.receiveParameters()
            val user =
                UserControl.loginWithToken(formParameters.getOrFail<String>("token"))
            if (user != null) {
                call.respondText {
                    AppUtils.generateResponse(
                        true,
                        user.username,
                        RoomControl.generateRoom()
                    )
                }
            } else {
                call.respondText {
                    AppUtils.generateResponse(
                        false,
                        "Autologin failed because token is invalid."
                    )
                }
            }
        }
        post("/room/update") {
            val formParameters = call.receiveParameters()
            val roomCode = formParameters.getOrFail<String>("roomCode")
            if (roomCode.isEmpty()) {
                call.respondText {
                    AppUtils.generateResponse(
                        true,
                        "",
                        RoomControl.generateRoom()
                    )
                }
                return@post
            }
            if (RoomControl.getRoomId(roomCode) != null) {
                call.respondText {
                    AppUtils.generateResponse(
                        true,
                        "",
                        roomCode
                    )
                }
            } else {
                call.respondText {
                    AppUtils.generateResponse(
                        false,
                        "Invalid room code."
                    )
                }
            }
        }
    }
}
