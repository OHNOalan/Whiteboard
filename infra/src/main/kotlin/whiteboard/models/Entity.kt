package whiteboard

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import whiteboard.DatabaseFactory.dbQuery

data class Entity(val id: String, val roomId: Int, val descriptor: String, val timestamp: Long) {
}

object Entities : Table() {
    val id = varchar("id", 256)
    val roomId = integer("roomId")
    val descriptor = text("descriptor")
    val timestamp = long("timestamp")
    override val primaryKey = PrimaryKey(id)
}

object EntityControl {
    private fun resultToEntity(row: ResultRow) = Entity(
        id = row[Entities.id],
        roomId = row[Entities.roomId],
        descriptor = row[Entities.descriptor],
        timestamp = row[Entities.timestamp]
    )

    suspend fun create(id: String, roomId: Int, descriptor: String, timestamp: Long): Entity? = dbQuery {
        val resultStatement = Entities.insert {
            it[Entities.id] = id
            it[Entities.roomId] = roomId
            it[Entities.descriptor] = descriptor
            it[Entities.timestamp] = timestamp
        }
        resultStatement.resultedValues?.singleOrNull()?.let(::resultToEntity)
    }

    suspend fun load(roomId: Int): String = dbQuery {
        val result = Entities
            .select { Entities.roomId eq roomId }
            .map(::resultToEntity)
        var output = ""
        for (item in result) {
            output += "~" + item.id + ":" + item.descriptor + ":" + item.timestamp
        }
        output
    }

    suspend fun delete(id: String): Boolean = dbQuery {
        Entities.deleteWhere { Entities.id.eq(id) } > 0
    }
}