package whiteboard.models

import org.jetbrains.exposed.sql.*
import whiteboard.AppSettings
import whiteboard.AppUtils
import whiteboard.DatabaseFactory.dbQuery
import kotlin.random.Random

/**
 * The class of User Entity for database
 * @param id The unique identifier for user.
 * @param username of user.
 * @param password of user.
 */
data class User(val id: Int, val username: String, val password: String) {
    fun token(): String {
        val randomString = Random.nextInt(0, 2147483647)
        val mainPart = "$randomString|${id}"
        return mainPart + "-" + AppUtils.hashString(AppSettings.SECRET + mainPart + AppSettings.SECRET)
    }
}

/**
 * The Object of User Entity for database
 * @property id The unique identifier for user.
 * @property username The username of user.
 * @property password The password of user.
 * @property primaryKey The primary key of Entity.
 */
object Users : Table() {
    val id = integer("id").autoIncrement()
    val username = varchar("username", 128)
    val password = varchar("password", 128)
    override val primaryKey = PrimaryKey(id)
}

/**
 * User Controller for handling various request
 */
object UserControl {
    /**
     * Create User Object given row Info
     * @param row The row containing all info about User
     * @return User Object
     */
    private fun resultToUser(row: ResultRow) = User(
        id = row[Users.id],
        username = row[Users.username],
        password = row[Users.password],
    )

    /**
     * Insert User Entity to Database
     * @param username The username of inserted User
     * @param password The password of inserted User
     * @return User Entity if inserting user Entity succeed
     */
    private suspend fun insert(username: String, password: String): User? = dbQuery {
        val resultStatement = Users.insert {
            it[Users.username] = username
            it[Users.password] = AppUtils.hashString(password)
        }
        resultStatement.resultedValues?.singleOrNull()?.let(UserControl::resultToUser)
    }

    /**
     * Create User Entity
     * @param username The username of inserted User
     * @param password The password of inserted User
     * @return User Entity if creating User succeed
     */
    suspend fun create(username: String, password: String): User? {
        return insert(username, password)
    }

    /**
     * Get UserInfo by ID
     * @param id The unique identifier of User
     * @return User Entity if searching id return not null
     */
    private suspend fun getById(id: Int): User? = dbQuery {
        Users.select { Users.id.eq(id) }.singleOrNull()?.let(UserControl::resultToUser)
    }

    /**
     * Get UserInfo by Username
     * @param username The username of User
     * @return User Entity if searching username return not null
     */
    suspend fun getByUsername(username: String): User? = dbQuery {
        Users.select { Users.username.eq(username) }.singleOrNull()
            ?.let(UserControl::resultToUser)
    }

    /**
     * Login with username and password
     * @param username The username of User
     * @param password The password of User
     * @return User Entity if login succeed
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
     * Login with Token
     * @param token The login token stored locally
     * @return User Entity if login succeed
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
