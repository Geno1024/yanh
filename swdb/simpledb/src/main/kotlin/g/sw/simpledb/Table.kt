package g.sw.simpledb

import java.io.RandomAccessFile
import kotlin.reflect.KClass

/**
 * A [g.sw.simpledb.Table] in [g.sw.simpledb] contains 3 files:
 * - Metadata, `[name].gsdb`
 * - Index, `[name].gsdbi`
 * - Pool, `[name].gsdbp`
 *
 * # Naming
 * `gsdb` means Geno's Simple DataBase.
 *
 * # Data structure
 * ## Metadata
 *
 */
class Table<T: Line<T>>(val lineDef: Class<T>, val name: String)
{
    constructor(lineDef: KClass<T>, name: String): this(lineDef.java, name)

    private val metadata = RandomAccessFile("$name.gsdb", "rw")
    private val index = RandomAccessFile("$name.gsdbi", "rw")
    private val pool = RandomAccessFile("$name.gsdbp", "rw")

    /**
     * Create empty table.
     */
    fun init(): Table<T> = apply {
        metadata.write("gsdb0001".toByteArray())
        metadata.write(System.currentTimeMillis().toHexString(HexFormat.Default).padStart(8, '0').hexToByteArray())
        val qn = lineDef.canonicalName.toString().toByteArray()
        metadata.write(qn.size.toHexString().padStart(4, '0').hexToByteArray())
        metadata.write(qn)
        index.setLength(0)
        pool.setLength(0)
    }

    fun add(line: T)
    {
        index.seek(index.length())
        pool.seek(pool.length())
        index.writeLong(pool.filePointer)
        pool.write(line.ser())
    }

}
