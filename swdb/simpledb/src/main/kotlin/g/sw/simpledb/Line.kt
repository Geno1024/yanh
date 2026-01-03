package g.sw.simpledb

import java.io.ByteArrayOutputStream
import java.io.RandomAccessFile
import kotlin.reflect.KProperty1
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.findAnnotation

/**
 * A [g.sw.simpledb.Line] in [g.sw.simpledb]
 */
interface Line<T : Line<T>>
{
    fun ser(): ByteArray = ByteArrayOutputStream().apply {
        this@Line::class
            .declaredMemberProperties
            .toList()
            .withIndex()
            .sortedBy { it.value.findAnnotation<Sequence>()?.value ?:(it.index * 10) }
            .map { (_, value) ->
                (value as KProperty1<Line<T>, *>).get(this@Line)
            }
            .forEach {
                when (it)
                {
                    is Byte -> {
                        write(it.toInt())
                    }
                    is Short -> {
                        write(it.toInt() ushr 8)
                        write(it.toInt())
                    }
                    is Int -> {
                        write(it ushr 24)
                        write(it ushr 16)
                        write(it ushr 8)
                        write(it)
                    }
                    is Long -> {
                        write((it ushr 56).toInt())
                        write((it ushr 48).toInt())
                        write((it ushr 40).toInt())
                        write((it ushr 32).toInt())
                        write(it.toInt() ushr 24)
                        write(it.toInt() ushr 16)
                        write(it.toInt() ushr 8)
                        write(it.toInt())
                    }
                    is String -> {
                        val length = it.length
                        write(length ushr 24)
                        write(length ushr 16)
                        write(length ushr 8)
                        write(length)
                        write(it.toByteArray())
                    }
                }
            }
    }.toByteArray()

    companion object
    {
        fun <T : Line<T>> read(raf: RandomAccessFile): Line<T>
        {
            return object : Line<T>{}
        }
    }
}
