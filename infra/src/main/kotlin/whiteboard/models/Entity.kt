package whiteboard.models

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import whiteboard.DatabaseFactory.dbQuery


@Serializable
data class Entity(
    val id: String,
    val roomId: Int,
    val descriptor: String,
    val previousDescriptor: String?,
    val type: String,
    val timestamp: Long
)

object Entities : Table() {
    val id = varchar("id", 256)
    val roomId = integer("roomId")
    val descriptor = text("descriptor")
    val type = text("type")
    val timestamp = long("timestamp")
    override val primaryKey = PrimaryKey(id)
}

object EntityControl {
    private fun resultToEntity(row: ResultRow) = Entity(
        id = row[Entities.id],
        roomId = row[Entities.roomId],
        descriptor = row[Entities.descriptor],
        previousDescriptor = null,
        type = row[Entities.type],
        timestamp = row[Entities.timestamp]
    )

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

    suspend fun load(roomId: Int): List<Entity> = dbQuery {
        Entities
            .select { Entities.roomId eq roomId }
            .map(EntityControl::resultToEntity)
    }

    suspend fun delete(id: String): Boolean = dbQuery {
        Entities.deleteWhere { Entities.id.eq(id) } > 0
    }

    suspend fun modify(id: String, descriptor: String): Boolean = dbQuery {
        Entities.update({ Entities.id eq id }) {
            it[Entities.descriptor] = descriptor
        } > 0
    }
}

