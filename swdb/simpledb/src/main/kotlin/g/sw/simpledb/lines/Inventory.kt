package g.sw.simpledb.lines

import g.sw.simpledb.Line
import g.sw.simpledb.Sequence

data class Inventory(
    @property:Sequence(0) var id: Int,
    @property:Sequence(10) var productId: Int,
    @property:Sequence(20) var locationId: Int,
    @property:Sequence(30) var quantity: Int,
    @property:Sequence(40) var createTime: Long,
) : Line<Inventory>
