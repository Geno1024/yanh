package g.sw.star

import g.sw.simpledb.Table
import g.sw.simpledb.lines.PriceHistory as PriceHistoryRecord
import java.io.File

class PriceHistory {
    private val table by lazy {
        if (File("price_histories.gsdb").exists()) {
            Table(PriceHistoryRecord::class.java, "price_histories")
        } else {
            Table(PriceHistoryRecord::class.java, "price_histories").apply { init() }
        }
    }

    fun add(productId: Int, shopId: Int = 0, price: Double): String {
        val id = table.search { true }.size + 1
        table.add(PriceHistoryRecord(id, productId, shopId, price, System.currentTimeMillis()))
        return "ok"
    }

    fun list(productId: Int, shopId: Int = 0): List<PriceHistoryRecord> {
        return table.search { it.productId == productId && it.shopId == shopId }
    }

    fun latest(productId: Int, shopId: Int = 0): PriceHistoryRecord? {
        return table.search { it.productId == productId && it.shopId == shopId }.maxByOrNull { it.recordTime }
    }
}
