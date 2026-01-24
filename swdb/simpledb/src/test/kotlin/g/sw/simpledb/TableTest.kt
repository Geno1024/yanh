package g.sw.simpledb

import java.io.File
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

class TableTest
{
    data class User(
        @property:Sequence(1) val name: String,
        @property:Sequence(2) val age: Int
    ) : Line<User>

    @Test
    fun testAddAndQuery()
    {
        val table = Table(User::class, "add-test")
        table.init()
        table.add(User("User0", 0))
        table.add(User("User1", 1))
        assertEquals("User0", table[0].name, "table[0].name")
        assertEquals(0, table[0].age, "table[0].age")
        assertEquals("User1", table[1].name, "table[1].name")
        assertEquals(1, table[1].age, "table[1].age")
    }

    @AfterTest
    fun cleanup()
    {
        File(".").listFiles { _, string ->
            string.contains("-test.gsdb")
        }?.forEach(File::delete)
    }
}
