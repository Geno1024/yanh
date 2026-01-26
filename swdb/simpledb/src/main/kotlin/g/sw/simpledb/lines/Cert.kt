package g.sw.simpledb.lines

import g.sw.simpledb.Line
import g.sw.simpledb.Sequence

data class Cert(
    @property:Sequence(0) var id: Int,
    @property:Sequence(10) var userId: Int,
    @property:Sequence(20) var name: String,
    @property:Sequence(30) var level: String,
    @property:Sequence(40) var assigner: String,
    @property:Sequence(50) var assignTime: Long,
    @property:Sequence(60) var storeLocation: String,
    @property:Sequence(70) var validUntil: Long
) : Line<Cert>
