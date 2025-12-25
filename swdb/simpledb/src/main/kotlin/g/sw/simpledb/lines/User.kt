package g.sw.simpledb.lines

import g.sw.simpledb.Line

data class User(
    var id: Long = 0,
    var username: String = "",
) : Line
