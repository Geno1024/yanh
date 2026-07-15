package g.sw.simpledb.lines

import g.sw.simpledb.Line
import g.sw.simpledb.Sequence

data class Location(
    @property:Sequence(0) var id: Int,
    @property:Sequence(10) var userId: Int,
    @property:Sequence(20) var name: String,
    @property:Sequence(30) var parentId: Int,
    @property:Sequence(40) var level: Int,
    @property:Sequence(50) var type: String,
    @property:Sequence(60) var createTime: Long,
) : Line<Location>
