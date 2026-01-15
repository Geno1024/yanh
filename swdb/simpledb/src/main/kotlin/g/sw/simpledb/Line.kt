package g.sw.simpledb

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.RandomAccessFile
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.jvmErasure

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
                    is Float -> {
                        val bits = it.toBits()
                        write(bits ushr 24)
                        write(bits ushr 16)
                        write(bits ushr 8)
                        write(bits)
                    }
                    is Double -> {
                        val bits = it.toBits()
                        write((bits ushr 56).toInt())
                        write((bits ushr 48).toInt())
                        write((bits ushr 40).toInt())
                        write((bits ushr 32).toInt())
                        write(bits.toInt() ushr 24)
                        write(bits.toInt() ushr 16)
                        write(bits.toInt() ushr 8)
                        write(bits.toInt())
                    }
                    is String -> {
                        val length = it.length
                        write(length ushr 24)
                        write(length ushr 16)
                        write(length ushr 8)
                        write(length)
                        write(it.toByteArray(Charsets.UTF_8))
                    }
                }
            }
    }.toByteArray()

    companion object
    {
        fun <T : Line<T>> deser(clazz: KClass<T>, bais: ByteArrayInputStream): T = clazz.primaryConstructor!!
            .parameters
            .withIndex()
            .sortedBy { it.value.findAnnotation<Sequence>()?.value ?: (it.index * 10) }
            .associate { (_, parameter) ->
                parameter to when (parameter.type.jvmErasure)
                {
                    Byte::class -> {
                        bais.read().toByte()
                    }
                    Short::class -> {
                        (
                            (bais.read() shl 8) +
                                bais.read()
                            ).toShort()
                    }
                    Int::class -> {
                        (bais.read() shl 24) +
                            (bais.read() shl 16) +
                            (bais.read() shl 8) +
                            bais.read()
                    }
                    Long::class -> {
                        (
                            (bais.read().toLong() shl 56) +
                                (bais.read().toLong() shl 48) +
                                (bais.read().toLong() shl 40) +
                                (bais.read().toLong() shl 32) +
                                (bais.read() shl 24) +
                                (bais.read() shl 16) +
                                (bais.read() shl 8) +
                                (bais.read())
                            )
                    }
                    Float::class -> {
                        Float.fromBits(
                            (bais.read() shl 24) +
                                (bais.read() shl 16) +
                                (bais.read() shl 8) +
                                bais.read()
                        )
                    }
                    Double::class -> {
                        Double.fromBits(
                            (bais.read().toLong() shl 56) +
                                (bais.read().toLong() shl 48) +
                                (bais.read().toLong() shl 40) +
                                (bais.read().toLong() shl 32) +
                                (bais.read() shl 24) +
                                (bais.read() shl 16) +
                                (bais.read() shl 8) +
                                (bais.read())
                        )
                    }
                    String::class -> {
                        val length = (bais.read() shl 24) + (bais.read() shl 16) + (bais.read() shl 8) + bais.read()
                        val buf = ByteArray(length)
                        bais.read(buf)
                        buf.toString(Charsets.UTF_8)
                    }
                    else -> {
                    }
                }
            }.let {
                clazz.primaryConstructor!!.callBy(it)
            }

        inline fun <reified T : Line<T>> deser(bais: ByteArrayInputStream): T = deser(T::class, bais)

        fun <T : Line<T>> read(raf: RandomAccessFile): Line<T>
        {
            return object : Line<T>{}
        }
    }
}
