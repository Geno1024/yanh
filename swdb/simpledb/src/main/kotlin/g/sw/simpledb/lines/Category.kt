package g.sw.simpledb.lines

import g.sw.simpledb.Line
import g.sw.simpledb.Sequence

data class Category(
    @property:Sequence(0) var id: Int,
    @property:Sequence(10) var name: String,
    @property:Sequence(20) var parentId: Int,
    @property:Sequence(30) var createTime: Long,
) : Line<Category>
