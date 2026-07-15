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

    fun add(productId: Int, price: Double): String {
        val id = table.search { true }.size + 1
        table.add(PriceHistoryRecord(id, productId, price, System.currentTimeMillis()))
        return "ok"
    }

    fun list(productId: Int): List<PriceHistoryRecord> {
        return table.search { it.productId == productId }
    }

    fun latest(productId: Int): PriceHistoryRecord? {
        val all = table.search { it.productId == productId }
        return all.maxByOrNull { it.recordTime }
    }
}
