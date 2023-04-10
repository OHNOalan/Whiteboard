package whiteboard.models

import org.jetbrains.exposed.sql.*
import whiteboard.AppSettings
import whiteboard.AppUtils
import whiteboard.DatabaseFactory.dbQuery
import kotlin.random.Random

/**
 * The schema of the user entity that is stored on the database.
 * @param id The unique identifier for user.
 * @param username The username of the user.
 * @param password The password of the user.
 */
data class User(val id: Int, val username: String, val password: String) {
    fun token(): String {
        val randomString = Random.nextInt(0, 2147483647)
        val mainPart = "$randomString|${id}"
        return mainPart + "-" + AppUtils.hashString(AppSettings.SECRET + mainPart + AppSettings.SECRET)
    }
}

/**
 * The schema for the users table.
 * @property id The unique identifier for user.
 * @property username The username of the user.
 * @property password The password of the user.
 * @property primaryKey The primary key of the users table.
 */
object Users : Table() {
    val id = integer("id").autoIncrement()
    val username = varchar("username", 128)
    val password = varchar("password", 128)
    override val primaryKey = PrimaryKey(id)
}

/**
 * Controller for handling requests to the users table.
 */
object UserControl {
    /**
     * Create a user object given row info.
     * @param row The row containing all the info about the User.
     * @return The user object.
     */
    private fun resultToUser(row: ResultRow) = User(
        id = row[Users.id],
        username = row[Users.username],
        password = row[Users.password],
    )

    /**
     * Insert a new user to the database.
     * @param username The username of the user.
     * @param password The password of the user.
     * @return User object if insert is successful.
     */
    private suspend fun insert(username: String, password: String): User? = dbQuery {
        val resultStatement = Users.insert {
            it[Users.username] = username
            it[Users.password] = AppUtils.hashString(password)
        }
        resultStatement.resultedValues?.singleOrNull()?.let(UserControl::resultToUser)
    }

    /**
     * Create a user row in the users table.
     * @param username The username of the user.
     * @param password The password of the user.
     * @return User object if insert is successful.
     */
    suspend fun create(username: String, password: String): User? {
        return insert(username, password)
    }

    /**
     * Get a user object by the user identifier.
     * @param id The unique identifier of the user.
     * @return User object if it exists in database.
     */
    private suspend fun getById(id: Int): User? = dbQuery {
        Users.select { Users.id.eq(id) }.singleOrNull()?.let(UserControl::resultToUser)
    }

    /**
     * Get user object by the username of the user.
     * @param username The username of the user.
     * @return User object if user exists in database.
     */
    suspend fun getByUsername(username: String): User? = dbQuery {
        Users.select { Users.username.eq(username) }.singleOrNull()
            ?.let(UserControl::resultToUser)
    }

    /**
     * Login with username and password.
     * @param username The username of the user.
     * @param password The password of user.
     * @return The user object if login is successful.
     */
    suspend fun login(username: String, password: String): User? = dbQuery {
        Users.select {
            Users.username.eq(username) and Users.password.eq(
                AppUtils.hashString(
                    password
                )
            )
        }.singleOrNull()?.let(UserControl::resultToUser)
    }

    /**
     * Login the user with the user token.
     * @param token The login token stored locally.
     * @return User object if operation is successful.
     */
    suspend fun loginWithToken(token: String): User? {
        val parts = token.split('-')
        if (parts.size != 2) {
            return null
        }
        val mainPart = parts[0]
        val signaturePart = parts[1]
        if (AppUtils.hashString(AppSettings.SECRET + mainPart + AppSettings.SECRET) != signaturePart) {
            return null
        }
        return getById(mainPart.split('|')[1].toInt())
    }
}
