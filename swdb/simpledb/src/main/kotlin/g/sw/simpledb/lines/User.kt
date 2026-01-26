package g.sw.simpledb.lines

import g.sw.simpledb.Line
import g.sw.simpledb.Sequence

data class User(
    @property:Sequence(0) var id: Int,
    @property:Sequence(10) var login: String,
    @property:Sequence(20) var name: String,
    @property:Sequence(30) var password: String,
    @property:Sequence(40) var passkey: String,
    @property:Sequence(50) var gender: String,
    @property:Sequence(60) var birthday: String,
    @property:Sequence(70) var type: String,
    @property:Sequence(80) var role: String,
    @property:Sequence(90) var registerTime: Long
) : Line<User>
