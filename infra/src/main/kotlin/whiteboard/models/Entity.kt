package whiteboard.models

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import whiteboard.DatabaseFactory.dbQuery
import whiteboard.models.Entities.descriptor
import whiteboard.models.Entities.id
import whiteboard.models.Entities.primaryKey
import whiteboard.models.Entities.roomId
import whiteboard.models.Entities.timestamp
import whiteboard.models.Entities.type

/**
 * The class of Entities for database
 * @param id The unique identifier for entity.
 * @param roomId The RoomID of drawing.
 * @param descriptor The descriptor of entity.
 * @param previousDescriptor Only used for a modify action. Used for undo/redo
 * to track the previous state of the item.
 * @param type The type of entity.
 * @param timestamp The time of entities.
 */
@Serializable
data class Entity(
    val id: String,
    val roomId: Int,
    val descriptor: String,
    val previousDescriptor: String?,
    val type: String,
    val timestamp: Long
)

/**
 * The Object of Entities for database
 * @property id The unique identifier for entity.
 * @property roomId The RoomID of drawing.
 * @property descriptor The descriptor of entity.
 * @property previousDescriptor Only used for a modify action. Used for undo/redo
 * to track the previous state of the item.
 * @property type The type of entity.
 * @property timestamp The time of entities.
 * @property primaryKey The primary key of Entity.
 */
object Entities : Table() {
    val id = varchar("id", 256)
    val roomId = integer("roomId")
    val descriptor = text("descriptor")
    val type = text("type")
    val timestamp = long("timestamp")
    override val primaryKey = PrimaryKey(id)
}

/**
 * Entity Controller for handling various request
 */
object EntityControl {
    /**
     * Create Entity Object given row Info
     * @param row The row containing all info about Entity
     * @return Entity Object
     */
    private fun resultToEntity(row: ResultRow) = Entity(
        id = row[Entities.id],
        roomId = row[Entities.roomId],
        descriptor = row[Entities.descriptor],
        previousDescriptor = null,
        type = row[Entities.type],
        timestamp = row[Entities.timestamp]
    )

    /**
     * Create Entity
     * @param id The unique identifier for entity.
     * @param roomId The RoomID of drawing.
     * @param descriptor The descriptor of entity.
     * @param type The type of entity.
     * @param timestamp The time of entities.
     * @return Entity if creating Entity succeed
     */
    suspend fun create(
        id: String,
        roomId: Int,
        descriptor: String,
        type: String,
        timestamp: Long
    ): Entity? = dbQuery {
        val resultStatement = Entities.insert {
            it[Entities.id] = id
            it[Entities.roomId] = roomId
            it[Entities.descriptor] = descriptor
            it[Entities.type] = type
            it[Entities.timestamp] = timestamp
        }
        resultStatement.resultedValues?.singleOrNull()
            ?.let(EntityControl::resultToEntity)
    }

    /**
     * Load Entities
     * @param roomId The RoomID of drawing.
     * @return list of entity if searching RoomId return not null
     */
    suspend fun load(roomId: Int): List<Entity> = dbQuery {
        Entities
            .select { Entities.roomId eq roomId }
            .map(EntityControl::resultToEntity)
    }

    /**
     * Delete Entity
     * @param id The id of entity.
     * @return true if deleting entity succeed
     */
    suspend fun delete(id: String): Boolean = dbQuery {
        Entities.deleteWhere { Entities.id.eq(id) } > 0
    }

    /**
     * Modify Entity
     * @param id The id of entity.
     * @return true if modifying entity succeed
     */
    suspend fun modify(id: String, descriptor: String): Boolean = dbQuery {
        Entities.update({ Entities.id eq id }) {
            it[Entities.descriptor] = descriptor
        } > 0
    }
}

