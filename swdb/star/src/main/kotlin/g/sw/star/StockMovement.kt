package g.sw.star

import g.sw.simpledb.Table
import g.sw.simpledb.lines.StockMovement as StockMovementRecord
import java.io.File

class StockMovement {
    private val table by lazy {
        if (File("stock_movements.gsdb").exists()) {
            Table(StockMovementRecord::class.java, "stock_movements")
        } else {
            Table(StockMovementRecord::class.java, "stock_movements").apply { init() }
        }
    }

    fun add(productId: Int, inventoryId: Int, delta: Int, type: String, userId: Int = 0, note: String = ""): String {
        val id = table.search { true }.size + 1
        table.add(StockMovementRecord(id, productId, inventoryId, delta, type, userId, note, System.currentTimeMillis()))
        return "ok"
    }

    fun list(productId: Int): List<StockMovementRecord> {
        return table.search { it.productId == productId }
    }

    fun listByInventory(inventoryId: Int): List<StockMovementRecord> {
        return table.search { it.inventoryId == inventoryId }
    }

    fun listAll(): List<StockMovementRecord> {
        return table.search { true }
    }
}
