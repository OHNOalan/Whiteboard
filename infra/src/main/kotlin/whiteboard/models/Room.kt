package whiteboard.models

import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import whiteboard.AppUtils
import whiteboard.DatabaseFactory.dbQuery
import whiteboard.models.Users.id
import whiteboard.models.Users.password
import whiteboard.models.Users.primaryKey
import whiteboard.models.Users.username

/**
 * The class of Room Entity for database
 * @param id The unique identifier for Room.
 * @param roomCode The Room access code.
 */
data class Room(
    val id: Int,
    val roomCode: String
)

/**
 * The Object of Room Entity for database
 * @property id The unique identifier for Room.
 * @property roomCode The username of Room.
 * @property primaryKey The primary key of Entity.
 */
object Rooms : Table() {
    val id = integer("id").autoIncrement()
    val roomCode = varchar("roomCode", 8)
    override val primaryKey = PrimaryKey(id)
}

object RoomControl {
    /**
     * Create Room Object given row Info
     * @param row The row containing all info about Room
     * @return Room Object
     */
    private fun resultToEntity(row: ResultRow) = Room(
        id = row[Rooms.id],
        roomCode = row[Rooms.roomCode],
    )

    /**
     * Generate Room Object with random RoomCode
     * @return roomCode if creating room succeed
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
     * Get Room ID given RoomCode
     * @return ID if searching RoomCode return not null
     */
    suspend fun getRoomId(roomCode: String): Int? {
        return load(roomCode)?.id
    }

    /**
     * Create Room with roomCode
     * @param roomCode The code of created room
     * @return Room Entity if creating Room succeed
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
     * Load Room with roomCode
     * @param roomCode The code of loaded room
     * @return Room Entity if searching Room succeed
     */
    private suspend fun load(roomCode: String): Room? = dbQuery {
        Rooms.select { Rooms.roomCode eq roomCode }.firstOrNull()
            ?.let { resultToEntity(it) }
    }
}

