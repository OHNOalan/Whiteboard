package whiteboard.models

import org.jetbrains.exposed.sql.*
import whiteboard.AppUtils
import whiteboard.DatabaseFactory.dbQuery
import kotlin.random.Random


data class User(val id: Int, val username: String, val password: String) {
    fun token(): String {
        val randomString = Random.nextInt(0, 2147483647)
        val mainPart = "$randomString|${id}"
        return mainPart + "-" + AppUtils.hashString(AppUtils.getSecret() + mainPart + AppUtils.getSecret())
    }
}

object Users : Table() {
    val id = integer("id").autoIncrement()
    val username = varchar("username", 128)
    val password = varchar("password", 128)
    override val primaryKey = PrimaryKey(id)
}

object UserControl {
    private fun resultToUser(row: ResultRow) = User(
        id = row[Users.id],
        username = row[Users.username],
        password = row[Users.password],
    )

    private suspend fun insert(username: String, password: String): User? = dbQuery {
        val resultStatement = Users.insert {
            it[Users.username] = username
            it[Users.password] = AppUtils.hashString(password)
        }
        resultStatement.resultedValues?.singleOrNull()?.let(UserControl::resultToUser)
    }

    suspend fun create(username: String, password: String): User? {
        return insert(username, password)
    }

    private suspend fun getById(id: Int): User? = dbQuery {
        Users.select { Users.id.eq(id) }.singleOrNull()
            ?.let(UserControl::resultToUser)
    }

    suspend fun getByUsername(username: String): User? = dbQuery {
        Users.select { Users.username.eq(username) }.singleOrNull()
            ?.let(UserControl::resultToUser)
    }

    suspend fun login(username: String, password: String): User? = dbQuery {
        Users.select {
            Users.username.eq(username) and Users.password.eq(
                AppUtils.hashString(
                    password
                )
            )
        }.singleOrNull()
            ?.let(UserControl::resultToUser)
    }

    suspend fun loginWithToken(token: String): User? {
        val parts = token.split('-')
        if (parts.size != 2) {
            return null
        }
        val mainPart = parts[0]
        val signaturePart = parts[1]
        if (AppUtils.hashString(AppUtils.getSecret() + mainPart + AppUtils.getSecret()) != signaturePart) {
            return null
        }
        return getById(mainPart.split('|')[1].toInt())
    }
}