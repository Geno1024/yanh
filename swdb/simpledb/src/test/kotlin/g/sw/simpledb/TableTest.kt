package g.sw.simpledb

import java.io.File
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

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

    @Test
    fun testDelete()
    {
        val table = Table(User::class, "delete-test")
        table.init()
        table.add(User("User0", 0))
        table.add(User("User1", 1))
        table.add(User("User2", 2))
        table.add(User("User3", 3))
        table.removeLast()
        assertFailsWith(IndexOutOfBoundsException::class) {
            table[3]
        }
        table.remove(0)
        assertEquals("User1", table[0].name, "table[0].name")
        assertEquals(1, table[0].age, "table[0].age")
        table.remove(1)
        assertEquals("User1", table[0].name, "table[0].name")
        assertEquals(1, table[0].age, "table[0].age")
    }

    @Test
    fun testModify()
    {
        val table = Table(User::class, "modify-test")
        table.init()
        table.add(User("User0", 0))
        table.add(User("User1", 1))
        table.add(User("User2", 2))
        table.add(User("User3", 3))
        table.add(User("User4", 4))
        table[2] = User("User6", 50)
        assertEquals("User6", table[2].name, "table[2].name")
        assertEquals(50, table[2].age, "table[2].age")
        assertEquals("User3", table[3].name, "table[3].age")
        assertEquals(3, table[3].age, "table[3].age")

        table[2] = User("User611", 50)
        assertEquals("User611", table[2].name, "table[2].name")
        assertEquals(50, table[2].age, "table[2].age")
        assertEquals("User3", table[3].name, "table[3].age")
        assertEquals(3, table[3].age, "table[3].age")
    }

    @Test
    fun testSearch()
    {
        val table = Table(User::class, "search-test")
        table.init()
        table.add(User("User0", 0))
        table.add(User("User1", 1))
        table.add(User("User2", 2))
        table.add(User("User3", 1))
        assertContentEquals(
            listOf(User("User1", 1), User("User3", 1)),
            table.search { it.age == 1 },
            "table[?].age == 1"
        )
        assertContentEquals(
            listOf(),
            table.search { it.age == 3 },
            "table[?].age == 3"
        )
    }

    @AfterTest
    fun cleanup()
    {
        File(".").listFiles { _, string ->
            string.contains("-test.gsdb")
        }?.forEach(File::delete)
    }
}
