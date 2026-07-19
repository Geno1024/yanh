package g.sw.simpledb.lines

import g.sw.simpledb.Line
import g.sw.simpledb.Sequence

data class PriceHistory(
    @property:Sequence(0) var id: Int,
    @property:Sequence(10) var productId: Int,
    @property:Sequence(20) var shopId: Int,
    @property:Sequence(30) var price: Double,
    @property:Sequence(40) var recordTime: Long,
) : Line<PriceHistory>
