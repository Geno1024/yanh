package g.sw.simpledb

import java.io.RandomAccessFile

class Table<T : Line>(val line: Class<T>, val fileNamePrefix: String = line.simpleName?:"ANONYMOUS")
{
    private val meta: RandomAccessFile = RandomAccessFile("$fileNamePrefix.gsdb", "rw")
    private val bitmap: RandomAccessFile = RandomAccessFile("$fileNamePrefix-bitmap.gsdb", "rw")
    private val pool: RandomAccessFile = RandomAccessFile("$fileNamePrefix-pool.gsdb", "rw")

    fun init(): Table<T> = apply {
        meta.write("GSDB0001".toByteArray(Charsets.UTF_8))
        meta.write(System.currentTimeMillis().toHexString(HexFormat.Default).padStart(8, '0').hexToByteArray())
        val qn = line.canonicalName.toString().toByteArray()
        meta.write(qn.size.toHexString().padStart(4, '0').hexToByteArray())
        meta.write(qn)
        bitmap.setLength(0)
        pool.setLength(0)
    }

    companion object
    {
        inline fun <reified T : Line> getDefaultTable(): Table<T> = Table(T::class.java, T::class.simpleName?:"ANONYMOUS")
        inline fun <reified T : Line> getTable(fileNamePrefix: String): Table<T> = Table(T::class.java, fileNamePrefix)
    }

    override fun toString(): String = "Table<${line.canonicalName}>(${fileNamePrefix})"
}
