package g.sw.star

import g.sw.simpledb.Table
import g.sw.simpledb.lines.Product as ProductRecord
import java.io.File

class Product {
    private val table by lazy {
        if (File("products.gsdb").exists()) {
            Table(ProductRecord::class.java, "products")
        } else {
            Table(ProductRecord::class.java, "products").apply { init() }
        }
    }

    private fun index(id: Int): Long {
        val idx = table.searchIndex { it.id == id }
        if (idx == -1L) throw IllegalArgumentException("Product not found: $id")
        return idx
    }

    fun add(name: String, type: String = "", barcode: String = "", unit: String = "", description: String = "", expiryDays: Int = 0): String {
        if (name.isBlank()) throw IllegalArgumentException("Name cannot be empty")
        val id = table.search { true }.size + 1
        table.add(ProductRecord(id, name, type, barcode, unit, description, expiryDays, System.currentTimeMillis()))
        return "ok"
    }

    fun list(): List<ProductRecord> {
        return table.search { true }
    }

    fun get(id: Int): ProductRecord? {
        val idx = table.searchIndex { it.id == id }
        return if (idx == -1L) null else table[idx]
    }

    fun search(keyword: String): List<ProductRecord> {
        return table.search { it.name.contains(keyword) || it.barcode.contains(keyword) }
    }

    fun update(id: Int, name: String = "", type: String = "", barcode: String = "", unit: String = "", description: String = "", expiryDays: String = ""): String {
        val prod = get(id) ?: throw IllegalArgumentException("Product not found: $id")
        val updated = prod.copy(
            name = name.ifBlank { prod.name },
            type = type.ifBlank { prod.type },
            barcode = barcode.ifBlank { prod.barcode },
            unit = unit.ifBlank { prod.unit },
            description = description.ifBlank { prod.description },
            expiryDays = expiryDays.ifBlank { prod.expiryDays.toString() }.toIntOrNull() ?: prod.expiryDays,
        )
        table[index(id)] = updated
        return "ok"
    }

    fun delete(id: Int): String {
        table.remove(index(id))
        return "ok"
    }
}
