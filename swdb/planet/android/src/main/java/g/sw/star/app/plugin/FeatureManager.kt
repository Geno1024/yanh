package g.sw.star.app.plugin

import android.content.Context
import android.content.SharedPreferences
import dalvik.system.DexClassLoader
import java.io.File

class FeatureManager(private val context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("features", Context.MODE_PRIVATE)

    private val builtinFeatures = mutableListOf<Feature>()

    private var loadedPlugins: List<Feature> = emptyList()

    fun registerBuiltin(feature: Feature) {
        builtinFeatures.add(feature)
    }

    fun loadPlugins() {
        val pluginsDir = File(context.filesDir, "plugins").also { it.mkdirs() }
        loadedPlugins = pluginsDir.listFiles { f -> f.extension == "jar" || f.extension == "dex" }
            ?.mapNotNull { f -> loadPlugin(f) }
            ?: emptyList()
    }

    private fun loadPlugin(file: File): Feature? {
        return try {
            val loader = DexClassLoader(
                file.absolutePath,
                context.cacheDir.absolutePath,
                null,
                context.classLoader
            )
            loader.loadClass("g.sw.star.app.plugin.PluginEntry")
                .getDeclaredConstructor()
                .newInstance() as? Feature
        } catch (_: Exception) {
            null
        }
    }

    fun getEnabledFeatures(): List<Feature> {
        return (builtinFeatures + loadedPlugins).filter {
            prefs.getBoolean(it.name, true)
        }
    }

    fun allFeatures(): List<Feature> = builtinFeatures + loadedPlugins

    fun isEnabled(name: String): Boolean = prefs.getBoolean(name, true)

    fun setEnabled(name: String, enabled: Boolean) {
        prefs.edit().putBoolean(name, enabled).apply()
    }
}
