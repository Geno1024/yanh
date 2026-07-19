package g.sw.star

import g.sw.simpledb.Table
import g.sw.simpledb.lines.Category as CategoryRecord
import java.io.File

class Category {
    private val table by lazy {
        if (File("categories.gsdb").exists()) {
            Table(CategoryRecord::class.java, "categories")
        } else {
            Table(CategoryRecord::class.java, "categories").apply { init() }
        }
    }

    private fun index(id: Int): Long {
        val idx = table.searchIndex { it.id == id }
        if (idx == -1L) throw IllegalArgumentException("Category not found: $id")
        return idx
    }

    fun add(name: String, parentId: Int = 0): String {
        if (name.isBlank()) throw IllegalArgumentException("Name cannot be empty")
        val id = table.search { true }.size + 1
        table.add(CategoryRecord(id, name, parentId, System.currentTimeMillis()))
        return "ok"
    }

    fun list(): List<CategoryRecord> {
        return table.search { true }
    }

    fun listChildren(parentId: Int): List<CategoryRecord> {
        return table.search { it.parentId == parentId }
    }

    fun get(id: Int): CategoryRecord? {
        val idx = table.searchIndex { it.id == id }
        return if (idx == -1L) null else table[idx]
    }

    fun update(id: Int, name: String = "", parentId: String = ""): String {
        val cat = get(id) ?: throw IllegalArgumentException("Category not found: $id")
        val updated = cat.copy(
            name = name.ifBlank { cat.name },
            parentId = parentId.ifBlank { cat.parentId.toString() }.toIntOrNull() ?: cat.parentId,
        )
        table[index(id)] = updated
        return "ok"
    }

    fun delete(id: Int): String {
        table.remove(index(id))
        return "ok"
    }
}
