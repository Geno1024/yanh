package g.sw.star

import g.sw.simpledb.Table
import g.sw.simpledb.lines.Location as LocationRecord
import java.io.File

class Location {
    private val table by lazy {
        if (File("locations.gsdb").exists()) {
            Table(LocationRecord::class.java, "locations")
        } else {
            Table(LocationRecord::class.java, "locations").apply { init() }
        }
    }

    private fun index(id: Int): Long {
        val idx = table.searchIndex { it.id == id }
        if (idx == -1L) throw IllegalArgumentException("Location not found: $id")
        return idx
    }

    fun add(userId: Int, name: String, parentId: Int = 0, level: Int = 0, type: String = ""): String {
        if (name.isBlank()) throw IllegalArgumentException("Name cannot be empty")
        val count = table.search { true }.size
        val loc = LocationRecord(count + 1, userId, name, parentId, level, type, System.currentTimeMillis())
        table.add(loc)
        return "ok"
    }

    fun list(userId: Int): List<LocationRecord> {
        return table.search { it.userId == userId }
    }

    fun listChildren(parentId: Int): List<LocationRecord> {
        return table.search { it.parentId == parentId }
    }

    fun get(id: Int): LocationRecord? {
        val idx = table.searchIndex { it.id == id }
        return if (idx == -1L) null else table[idx]
    }

    fun update(id: Int, name: String = "", parentId: String = "", level: String = "", type: String = ""): String {
        val loc = get(id) ?: throw IllegalArgumentException("Location not found: $id")
        val updated = loc.copy(
            name = name.ifBlank { loc.name },
            parentId = parentId.ifBlank { loc.parentId.toString() }.toIntOrNull() ?: loc.parentId,
            level = level.ifBlank { loc.level.toString() }.toIntOrNull() ?: loc.level,
            type = type.ifBlank { loc.type },
        )
        table[index(id)] = updated
        return "ok"
    }

    fun delete(id: Int): String {
        table.remove(index(id))
        return "ok"
    }
}
