package g.sw.simpledb.lines

import g.sw.simpledb.Line
import g.sw.simpledb.Sequence

data class User(
    @property:Sequence(10) var login: String,
    @property:Sequence(20) var name: String,
) : Line<User>
