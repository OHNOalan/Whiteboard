package whiteboard.models

import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import whiteboard.AppUtils
import whiteboard.DatabaseFactory.dbQuery

data class Room(
    val id: Int,
    val roomCode: String
)

object Rooms : Table() {
    val id = integer("id").autoIncrement()
    val roomCode = varchar("roomCode", 8)
    override val primaryKey = PrimaryKey(id)
}

object RoomControl {
    private fun resultToEntity(row: ResultRow) = Room(
        id = row[Rooms.id],
        roomCode = row[Rooms.roomCode],
    )

    suspend fun generateRoom(): String {
        var roomCode: String
        do {
            roomCode = AppUtils.getRandomString(8)
        } while (load(roomCode) != null)
        create(roomCode)
        return roomCode
    }

    suspend fun getRoomId(roomCode: String): Int? {
        return load(roomCode)?.id
    }

    private suspend fun create(
        roomCode: String,
    ): Room? = dbQuery {
        val resultStatement = Rooms.insert {
            it[Rooms.roomCode] = roomCode
        }
        resultStatement.resultedValues?.singleOrNull()
            ?.let(RoomControl::resultToEntity)
    }

    private suspend fun load(roomCode: String): Room? = dbQuery {
        Rooms.select { Rooms.roomCode eq roomCode }.firstOrNull()
            ?.let { resultToEntity(it) }
    }
}

