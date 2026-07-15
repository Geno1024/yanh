package g.sw.simpledb.lines

import g.sw.simpledb.Line
import g.sw.simpledb.Sequence

data class Product(
    @property:Sequence(0) var id: Int,
    @property:Sequence(10) var name: String,
    @property:Sequence(20) var type: String,
    @property:Sequence(30) var barcode: String,
    @property:Sequence(40) var unit: String,
    @property:Sequence(50) var description: String,
    @property:Sequence(60) var expiryDays: Int,
    @property:Sequence(70) var createTime: Long,
) : Line<Product>
