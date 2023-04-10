package whiteboard.plugins

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.testing.*
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.After
import org.junit.Before
import org.junit.Test
import whiteboard.models.Entities
import whiteboard.models.Rooms
import whiteboard.models.Users

class ConfigureEndpointsTest {
    private val driverClassName = "org.h2.Driver"
    private val jdbcURL = "jdbc:h2:file:./build/db7"
    private lateinit var database: Database

    @Before
    fun setup() {
        database = Database.connect(jdbcURL, driverClassName)
        transaction(database) {
            SchemaUtils.create(Users)
            SchemaUtils.create(Entities)
            SchemaUtils.create(Rooms)
        }
    }

    @After
    fun cleanup() {
        transaction(database) {
            SchemaUtils.drop(Users)
            SchemaUtils.drop(Entities)
            SchemaUtils.drop(Rooms)
        }
    }

    @Test
    fun get() = testApplication {
        val mockBody =
            "{\"success\":true,\"message\":\"Server online!\",\"roomCode\":\"\"}"
        application {
            configureEndpoints()
        }
        client.get("/").apply {
            assertEquals(HttpStatusCode.OK, this.status)
            assertEquals(mockBody, this.body())
        }
    }

    @Test
    fun createUserSuccess() = testApplication {
        val mockBody = "\"success\":true,.*,\"roomCode\":\".*\"".toRegex()
        application {
            configureEndpoints()
        }
        client.post("/user/create") {
            header(HttpHeaders.ContentType, ContentType.Application.FormUrlEncoded)
            setBody(
                listOf(
                    "username" to "qwerty",
                    "password" to "12345678"
                ).formUrlEncode()
            )
        }.apply {
            assertEquals(HttpStatusCode.OK, this.status)
            assertTrue(this.body<String>().contains(mockBody))
        }
    }

    @Test
    fun createUserTooShortUsername() = testApplication {
        val mockBody =
            "{\"success\":false,\"message\":\"Username length must be greater than 5 characters.\",\"roomCode\":\"\"}"
        application {
            configureEndpoints()
        }
        client.post("/user/create") {
            header(HttpHeaders.ContentType, ContentType.Application.FormUrlEncoded)
            setBody(
                listOf(
                    "username" to "qwer",
                    "password" to "123456"
                ).formUrlEncode()
            )
        }.apply {
            assertEquals(HttpStatusCode.OK, this.status)
            assertEquals(mockBody, this.body())
        }
    }

    @Test
    fun createUserTooLongUsername() = testApplication {
        val mockBody =
            "{\"success\":false,\"message\":\"Username length must be less than or equal to 16 characters.\",\"roomCode\":\"\"}"
        application {
            configureEndpoints()
        }
        client.post("/user/create") {
            header(HttpHeaders.ContentType, ContentType.Application.FormUrlEncoded)
            setBody(
                listOf(
                    "username" to "asdfghjklqwertyuiop",
                    "password" to "123456"
                ).formUrlEncode()
            )
        }.apply {
            assertEquals(HttpStatusCode.OK, this.status)
            assertEquals(mockBody, this.body())
        }
    }

    @Test
    fun createUserTooShortPassword() = testApplication {
        val mockBody =
            "{\"success\":false,\"message\":\"Password length must be greater than 5 characters.\",\"roomCode\":\"\"}"
        application {
            configureEndpoints()
        }
        client.post("/user/create") {
            header(HttpHeaders.ContentType, ContentType.Application.FormUrlEncoded)
            setBody(
                listOf(
                    "username" to "qwerty",
                    "password" to "1234"
                ).formUrlEncode()
            )
        }.apply {
            assertEquals(HttpStatusCode.OK, this.status)
            assertEquals(mockBody, this.body())
        }
    }

    @Test
    fun createDuplicateUser() = testApplication {
        val mockBody =
            "{\"success\":false,\"message\":\"A user with this username already exists.\",\"roomCode\":\"\"}"
        application {
            configureEndpoints()
        }
        client.post("/user/create") {
            header(HttpHeaders.ContentType, ContentType.Application.FormUrlEncoded)
            setBody(
                listOf(
                    "username" to "qwerty",
                    "password" to "123456"
                ).formUrlEncode()
            )
        }

        client.post("/user/create") {
            header(HttpHeaders.ContentType, ContentType.Application.FormUrlEncoded)
            setBody(
                listOf(
                    "username" to "qwerty",
                    "password" to "12345678"
                ).formUrlEncode()
            )
        }.apply {
            assertEquals(HttpStatusCode.OK, this.status)
            assertEquals(mockBody, this.body())
        }
    }

    @Test
    fun testUserLoginSuccess() = testApplication {
        val mockBody = "\"success\":true,\"message\":.*,\"roomCode\":\".*\"".toRegex()
        application {
            configureEndpoints()
        }

        // create user
        client.post("/user/create") {
            header(HttpHeaders.ContentType, ContentType.Application.FormUrlEncoded)
            setBody(
                listOf(
                    "username" to "qwerty",
                    "password" to "123456"
                ).formUrlEncode()
            )
        }

        client.post("/user/login") {
            header(HttpHeaders.ContentType, ContentType.Application.FormUrlEncoded)
            setBody(
                listOf(
                    "username" to "qwerty",
                    "password" to "123456"
                ).formUrlEncode()
            )
        }.apply {
            assertEquals(HttpStatusCode.OK, this.status)
            assertTrue(this.body<String>().contains(mockBody))
        }
    }

    @Test
    fun testUserLoginFail() = testApplication {
        val mockBody =
            "{\"success\":false,\"message\":\"Incorrect username or password.\",\"roomCode\":\"\"}"
        application {
            configureEndpoints()
        }

        client.post("/user/login") {
            header(HttpHeaders.ContentType, ContentType.Application.FormUrlEncoded)
            setBody(
                listOf(
                    "username" to "qwerty",
                    "password" to "123456"
                ).formUrlEncode()
            )
        }.apply {
            assertEquals(HttpStatusCode.OK, this.status)
            assertEquals(mockBody, this.body())
        }
    }

    @Test
    fun testUserAutoLoginFail() = testApplication {
        val mockBody =
            "{\"success\":false,\"message\":\"Autologin failed because token is invalid.\",\"roomCode\":\"\"}"
        application {
            configureEndpoints()
        }

        client.post("/user/autologin") {
            header(HttpHeaders.ContentType, ContentType.Application.FormUrlEncoded)
            setBody(listOf("token" to "qwerty").formUrlEncode())
        }.apply {
            assertEquals(HttpStatusCode.OK, this.status)
            assertEquals(mockBody, this.body())
        }
    }

    @Test
    fun roomUpdateInvalid() = testApplication {
        val mockBody =
            "{\"success\":false,\"message\":\"Invalid room code.\",\"roomCode\":\"\"}"
        application {
            configureEndpoints()
        }
        client.post("/room/update") {
            header(HttpHeaders.ContentType, ContentType.Application.FormUrlEncoded)
            setBody(listOf("roomCode" to "ABCD").formUrlEncode())
        }.apply {
            // room code is too short
            assertEquals(HttpStatusCode.OK, this.status)
            assertEquals(mockBody, this.body())
        }
    }

    @Test
    fun roomUpdateEmpty() = testApplication {
        val mockBody = "\"success\":true,\"message\":\"\",\"roomCode\":\".*\"".toRegex()
        application {
            configureEndpoints()
        }
        client.post("/room/update") {
            header(HttpHeaders.ContentType, ContentType.Application.FormUrlEncoded)
            setBody(listOf("roomCode" to "").formUrlEncode())
        }.apply {
            // randomly generate new room code
            assertEquals(HttpStatusCode.OK, this.status)
            assertTrue(this.body<String>().contains(mockBody))
        }
    }
}