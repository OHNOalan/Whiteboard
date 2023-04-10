package whiteboard.plugins

import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.*
import whiteboard.AppUtils
import whiteboard.models.RoomControl
import whiteboard.models.UserControl

/**
 * Endpoints for handling user and room requests.
 */
fun Application.configureEndpoints() {
    routing {
        /**
         * The default endpoint that signals that the server is online.
         */
        get("/") {
            call.respondText { AppUtils.generateResponse(true, "Server online!") }
        }
        /**
         * Endpoint for creating a user account. Takes username and password and returns
         * the user token and room code if registration is successful.
         */
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
        /**
         * Endpoint for logging a user in. Takes a username and password. Will return the
         * token for the user and the room code if log in is successful.
         */
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
        /**
         * Endpoint for logging a user in via a saved user token. Takes the user token
         * and returns the username for the user and the room code on success.
         */
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
        /**
         * Validates a given room code or create a new room. Will return the room code on
         * success.
         */
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
