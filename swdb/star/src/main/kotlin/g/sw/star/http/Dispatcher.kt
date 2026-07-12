package g.sw.star.http

import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.memberFunctions
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.jvmErasure

class Dispatcher(private val basePackage: String = "g.sw.star") {
    private val classCache = mutableMapOf<String, Class<*>>()

    fun dispatch(className: String, methodName: String, args: Map<String, String>, body: String? = null): Any? {
        val fqn = "$basePackage.$className"
        val clazz = classCache.getOrPut(fqn) { Class.forName(fqn) }
        val kclazz = clazz.kotlin
        val instance = kclazz.primaryConstructor?.call() ?: kclazz.createInstance()

        val function = kclazz.memberFunctions.find { it.name == methodName }
            ?: throw NoSuchMethodException(methodName)

        val params = mutableMapOf<KParameter, Any?>()
        function.parameters.forEach { param ->
            when (param.kind) {
                KParameter.Kind.INSTANCE -> params[param] = instance
                KParameter.Kind.VALUE -> {
                    val name = param.name ?: return@forEach
                    when {
                        args.containsKey(name) -> params[param] = convert(args[name]!!, param.type.jvmErasure)
                        body != null && !args.containsKey(name) -> params[param] = convert(body, param.type.jvmErasure)
                        param.isOptional -> {}
                        else -> throw IllegalArgumentException("Missing parameter: $name")
                    }
                }
                else -> {}
            }
        }

        return function.callBy(params)
    }

    fun resolveClass(name: String): String {
        val candidates = listOf(name, name.replaceFirstChar { it.uppercaseChar() })
        for (candidate in candidates) {
            try {
                Class.forName("$basePackage.$candidate")
                return candidate
            } catch (_: ClassNotFoundException) {
            }
        }
        throw ClassNotFoundException("$basePackage.$name")
    }

    private fun convert(value: String, target: KClass<*>): Any? = when (target) {
        String::class -> value
        Int::class -> value.toInt()
        Long::class -> value.toLong()
        Boolean::class -> value.toBoolean()
        Float::class -> value.toFloat()
        Double::class -> value.toDouble()
        else -> throw IllegalArgumentException("Unsupported parameter type: $target")
    }
}
