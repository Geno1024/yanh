package g.sw.simpledb.lines

import g.sw.simpledb.Line
import g.sw.simpledb.Sequence

data class User(
    @param:Sequence(10) var login: String,
    @param:Sequence(20) var name: String,
) : Line<User>
{
}
