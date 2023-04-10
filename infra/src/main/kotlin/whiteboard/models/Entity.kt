package whiteboard.models

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import whiteboard.DatabaseFactory.dbQuery

/**
 * The data class for an entity.
 * @param id The unique identifier for the entity.
 * @param roomId The room identifier of the entity.
 * @param descriptor The descriptor of the entity.
 * @param previousDescriptor Only used for a modify action.
 * The previous descriptor of the entity.
 * @param type The type of the entity.
 * @param timestamp The creation time of the entity.
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
 * The schema for the entities table.
 * @property id The unique identifier for the entity.
 * @property roomId The room identifier of the entity.
 * @property descriptor The descriptor of the entity.
 * @property type The type of the entity.
 * @property timestamp The creation time of the entity.
 * @property primaryKey The primary key of the table row.
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
 * The entity controller for processing requests and updating the entities table.
 */
object EntityControl {
    /**
     * Create an entity object given row the row info.
     * @param row The row containing all the info about the entity.
     * @return The entity object.
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
     * Create an entity.
     * @param id The unique identifier for the entity.
     * @param roomId The room identifier of the entity.
     * @param descriptor The descriptor of the entity.
     * @param type The type of the entity.
     * @param timestamp The creation time of the entity.
     * @return Entity if creating an entity is successful
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
     * Load a list of entities.
     * @param roomId The room identifier of the entities to load.
     * @return List of entities for that room.
     */
    suspend fun load(roomId: Int): List<Entity> = dbQuery {
        Entities
            .select { Entities.roomId eq roomId }
            .map(EntityControl::resultToEntity)
    }

    /**
     * Delete an entity.
     * @param id The id of the entity.
     * @return True if deleting the entity is successful.
     */
    suspend fun delete(id: String): Boolean = dbQuery {
        Entities.deleteWhere { Entities.id.eq(id) } > 0
    }

    /**
     * Modify an entity.
     * @param id The id of entity.
     * @return True if modifying the entity is successful.
     */
    suspend fun modify(id: String, descriptor: String): Boolean = dbQuery {
        Entities.update({ Entities.id eq id }) {
            it[Entities.descriptor] = descriptor
        } > 0
    }
}

