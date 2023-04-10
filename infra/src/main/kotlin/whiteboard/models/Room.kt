package whiteboard.models

import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import whiteboard.AppUtils
import whiteboard.DatabaseFactory.dbQuery

/**
 * The data class for the room object.
 * @param id The unique identifier for the room.
 * @param roomCode The room code.
 */
data class Room(
    val id: Int,
    val roomCode: String
)

/**
 * The schema for the rooms table in the database.
 * @property id The unique identifier for the room.
 * @property roomCode The room code.
 * @property primaryKey The primary key of the room.
 */
object Rooms : Table() {
    val id = integer("id").autoIncrement()
    val roomCode = varchar("roomCode", 8)
    override val primaryKey = PrimaryKey(id)
}

/**
 * The room controller for processing requests and updating the rooms table.
 */
object RoomControl {
    /**
     * Create a room object given a row info.
     * @param row The row containing all the info about room.
     * @return The room object.
     */
    private fun resultToEntity(row: ResultRow) = Room(
        id = row[Rooms.id],
        roomCode = row[Rooms.roomCode],
    )

    /**
     * Creates a room and returns the room code.
     * @return The room code.
     */
    suspend fun generateRoom(): String {
        var roomCode: String
        do {
            roomCode = AppUtils.getRandomString(8)
        } while (load(roomCode) != null)
        create(roomCode)
        return roomCode
    }

    /**
     * Get the room ID given a room code.
     * @return ID if such room code exists.
     */
    suspend fun getRoomId(roomCode: String): Int? {
        return load(roomCode)?.id
    }

    /**
     * Create a room with a room code.
     * @param roomCode The code of the created room.
     * @return Room object if creating the room is successful.
     */
    private suspend fun create(
        roomCode: String,
    ): Room? = dbQuery {
        val resultStatement = Rooms.insert {
            it[Rooms.roomCode] = roomCode
        }
        resultStatement.resultedValues?.singleOrNull()
            ?.let(RoomControl::resultToEntity)
    }

    /**
     * Load a room with the room code.
     * @param roomCode The room code.
     * @return Room object if room code exists.
     */
    private suspend fun load(roomCode: String): Room? = dbQuery {
        Rooms.select { Rooms.roomCode eq roomCode }.firstOrNull()
            ?.let { resultToEntity(it) }
    }
}

