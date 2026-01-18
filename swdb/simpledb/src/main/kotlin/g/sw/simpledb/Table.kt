package g.sw.simpledb

import java.io.ByteArrayInputStream
import java.io.File
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
 * This database writes everything immediately in harddisk file, without any log,
 *
 * # Data structure
 * ## Metadata
 *
 * ## Index
 *
 * ## Pool
 *
 */
class Table<T: Line<T>>(val lineDef: Class<T>, val name: String)
{
    constructor(lineDef: KClass<T>, name: String): this(lineDef.java, name)

    private val metadataFile = RandomAccessFile("$name.gsdb", "rw")
    private var indexFile = RandomAccessFile("$name.gsdbi", "rw")
    private var poolFile = RandomAccessFile("$name.gsdbp", "rw")

    /**
     * Create empty table.
     */
    fun init(): Table<T> = apply {
        metadataFile.write("gsdb0001".toByteArray())
        metadataFile.write(System.currentTimeMillis().toHexString(HexFormat.Default).padStart(8, '0').hexToByteArray())
        val qn = lineDef.canonicalName.toString().toByteArray()
        metadataFile.write(qn.size.toHexString().padStart(4, '0').hexToByteArray())
        metadataFile.write(qn)
        indexFile.setLength(0)
        poolFile.setLength(0)
    }

    fun add(line: T) = apply {
        indexFile.seek(indexFile.length())
        poolFile.seek(poolFile.length())
        indexFile.writeLong(poolFile.filePointer)
        val ser = line.ser()
        poolFile.writeInt(ser.size)
        poolFile.write(ser)
    }

    fun remove(vararg index: Long) = apply {
        val tempIndexFile = RandomAccessFile("$name.gsdbit", "rw")
        val tempPoolFile = RandomAccessFile("$name.gsdbpt", "rw")
        LongRange(0, indexFile.length() / 8)
            .filterNot { it !in index }
            .forEach {
                tempIndexFile.writeLong(tempPoolFile.filePointer)
                indexFile.seek(it * 8)
                val thiz = indexFile.readLong()
                tempPoolFile.seek(thiz)
                poolFile.seek(thiz)
                val size = poolFile.readInt()
                val buffer = ByteArray(size)
                poolFile.read(buffer)
                tempPoolFile.writeInt(size)
                tempPoolFile.write(buffer)
            }
        tempIndexFile.close()
        tempPoolFile.close()
        indexFile.close()
        poolFile.close()
        File("$name.gsdbit").renameTo(File("$name.gsdbi"))
        File("$name.gsdbpt").renameTo(File("$name.gsdbp"))
        indexFile = RandomAccessFile("$name.gsdbi", "rw")
        poolFile = RandomAccessFile("$name.gsdbp", "rw")
    }

    operator fun get(index: Long): T
    {
        indexFile.seek(index * 8)
        val thiz = indexFile.readLong()
        poolFile.seek(thiz)
        val buffer = ByteArray(poolFile.readInt())
        poolFile.read(buffer)
        return Line.deser(lineDef.kotlin, ByteArrayInputStream(buffer))
    }

    operator fun plusAssign(line: T)
    {
        add(line)
    }

}
