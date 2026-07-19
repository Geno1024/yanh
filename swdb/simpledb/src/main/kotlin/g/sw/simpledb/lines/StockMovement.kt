package g.sw.simpledb.lines

import g.sw.simpledb.Line
import g.sw.simpledb.Sequence

data class StockMovement(
    @property:Sequence(0) var id: Int,
    @property:Sequence(10) var productId: Int,
    @property:Sequence(20) var inventoryId: Int,
    @property:Sequence(30) var delta: Int,
    @property:Sequence(40) var type: String,
    @property:Sequence(50) var userId: Int,
    @property:Sequence(60) var note: String,
    @property:Sequence(70) var createTime: Long,
) : Line<StockMovement>
