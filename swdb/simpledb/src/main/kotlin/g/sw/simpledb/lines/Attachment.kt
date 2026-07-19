package g.sw.simpledb.lines

import g.sw.simpledb.Line
import g.sw.simpledb.Sequence

data class Attachment(
    @property:Sequence(0) var id: Int,
    @property:Sequence(10) var entityType: String,
    @property:Sequence(20) var entityId: Int,
    @property:Sequence(30) var fileName: String,
    @property:Sequence(40) var filePath: String,
    @property:Sequence(50) var mimeType: String,
    @property:Sequence(60) var fileSize: Long,
    @property:Sequence(70) var createTime: Long,
) : Line<Attachment>
