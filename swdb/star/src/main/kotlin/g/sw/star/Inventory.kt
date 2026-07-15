package g.sw.star

import g.sw.simpledb.Table
import g.sw.simpledb.lines.Inventory as InventoryRecord
import java.io.File

class Inventory {
    private val table by lazy {
        if (File("inventories.gsdb").exists()) {
            Table(InventoryRecord::class.java, "inventories")
        } else {
            Table(InventoryRecord::class.java, "inventories").apply { init() }
        }
    }

    private fun index(id: Int): Long {
        val idx = table.searchIndex { it.id == id }
        if (idx == -1L) throw IllegalArgumentException("Inventory not found: $id")
        return idx
    }

    fun add(productId: Int, locationId: Int, quantity: Int = 0): String {
        val id = table.search { true }.size + 1
        table.add(InventoryRecord(id, productId, locationId, quantity, System.currentTimeMillis()))
        return "ok"
    }

    fun list(): List<InventoryRecord> {
        return table.search { true }
    }

    fun listByLocation(locationId: Int): List<InventoryRecord> {
        return table.search { it.locationId == locationId }
    }

    fun listByProduct(productId: Int): List<InventoryRecord> {
        return table.search { it.productId == productId }
    }

    fun get(id: Int): InventoryRecord? {
        val idx = table.searchIndex { it.id == id }
        return if (idx == -1L) null else table[idx]
    }

    fun move(id: Int, locationId: Int): String {
        val inv = get(id) ?: throw IllegalArgumentException("Inventory not found: $id")
        table[index(id)] = inv.copy(locationId = locationId)
        return "ok"
    }

    fun adjust(id: Int, delta: Int): String {
        val inv = get(id) ?: throw IllegalArgumentException("Inventory not found: $id")
        val updated = inv.copy(quantity = inv.quantity + delta)
        table[index(id)] = updated
        return "ok"
    }

    fun delete(id: Int): String {
        table.remove(index(id))
        return "ok"
    }
}
