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

    fun removeLast() = apply {
        indexFile.seek(indexFile.length() - 8)
        val lastPtr = indexFile.readLong()
        indexFile.setLength(indexFile.length() - 8)
        poolFile.seek(lastPtr)
        val lastLength = poolFile.readInt()
        poolFile.setLength(lastPtr + lastLength)
    }

    fun remove(index: Long) = apply {
        if (index == indexFile.length() / 8 - 1) {
            removeLast()
        } else {
            val tempIndexFile = RandomAccessFile("$name.gsdbit", "rw")
            val tempPoolFile = RandomAccessFile("$name.gsdbpt", "rw")
            // Which one should remove?
            indexFile.seek(index * 8L)
            val removeIndexPtr = indexFile.readLong()
            // Where is it?
            poolFile.seek(removeIndexPtr)
            val removeLength = poolFile.readInt()
            // Keep the remaining of pool to temp pool.
            poolFile.seek(removeIndexPtr + removeLength)
            val buffer = ByteArray(4096)
            while (true)
            {
                val length = poolFile.read(buffer)
                if (length == -1) break
                tempPoolFile.write(buffer, 0, length)
            }
            // Write back from temp pool to pool, starting from where to remove.
            poolFile.seek(removeIndexPtr)
            tempPoolFile.seek(0)
            while (true)
            {
                val length = tempPoolFile.read(buffer)
                if (length == -1) break
                poolFile.write(buffer, 0, length)
            }
            // Keep the remaining of index to temp index.
            indexFile.seek((index + 1) * 8L)
            while (true)
            {
                val newIndex = indexFile.readLong() - removeLength
                tempIndexFile.writeLong(newIndex)
                if (indexFile.filePointer == indexFile.length()) break
            }
            // Write back from temp index to index, starting from where to move.
            indexFile.seek(index * 8L)
            tempIndexFile.seek(0)
            while (true)
            {
                val length = tempIndexFile.read(buffer)
                if (length == -1) break
                indexFile.write(buffer, 0, length)
            }
            // Post cleaning.
            tempPoolFile.close()
            tempIndexFile.close()
            File("$name.gsdbpt").delete()
            File("$name.gsdbit").delete()
        }
    }

    operator fun get(index: Long): T
    {
        if (index >= indexFile.length() / 8) throw IndexOutOfBoundsException("Reading index $index with db size ${indexFile.length() / 8}.")
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

    fun setLast(value: T): Table<T> = apply {
        indexFile.seek(indexFile.length() - 8)
        poolFile.seek(indexFile.readLong())
        val ser = value.ser()
        poolFile.writeInt(ser.size)
        poolFile.write(ser)
    }

    operator fun set(index: Long, value: T): Table<T> = apply {
        if (index == indexFile.length() / 8 - 1) {
            setLast(value)
        } else {
            val ser = value.ser()
            val serLength = ser.size
            indexFile.seek(index * 8L)
            val setPoolPos = indexFile.readLong()
            poolFile.seek(setPoolPos)
            val toSetPoolLength = poolFile.readInt()
            if (toSetPoolLength == serLength) {
                poolFile.seek(setPoolPos + 4)
                poolFile.write(ser)
            } else {
                val tempIndexFile = RandomAccessFile("$name.gsdbit", "rw")
                val tempPoolFile = RandomAccessFile("$name.gsdbpt", "rw")
                tempPoolFile.writeInt(ser.size)
                tempPoolFile.write(ser)
                val nextPoolPos = indexFile.readLong()
                poolFile.seek(nextPoolPos)
                val buffer = ByteArray(4096)
                while (true) {
                    val length = poolFile.read(buffer)
                    if (length == -1) break
                    tempPoolFile.write(buffer, 0, length)
                }
                poolFile.seek(setPoolPos)
                tempPoolFile.seek(0)
                while (true) {
                    val length = tempPoolFile.read(buffer)
                    if (length == -1) break
                    poolFile.write(buffer, 0, length)
                }
                indexFile.seek((index + 1) * 8L)
                val diff = serLength - toSetPoolLength
                while (true) {
                    val newIndex = indexFile.readLong() + diff
                    tempIndexFile.writeLong(newIndex)
                    if (indexFile.filePointer == indexFile.length()) break
                }
                indexFile.seek((index + 1) * 8L)
                tempIndexFile.seek(0)
                while (true)
                {
                    val length = tempIndexFile.read(buffer)
                    if (length == -1) break
                    indexFile.write(buffer, 0, length)
                }
                tempPoolFile.close()
                tempIndexFile.close()
                File("$name.gsdbpt").delete()
                File("$name.gsdbit").delete()
            }
        }
    }
}
