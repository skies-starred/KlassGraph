@file:Suppress("UNUSED", "UNCHECKED_CAST")

package foo.starred.klassgraph.core.data.scan

import foo.starred.klassgraph.core.data.internal.BytecodeKlassMetaData
import foo.starred.klassgraph.core.scan.helper.BytecodeKlassReader

class KlassScanResult(
    private val metas: Map<String, BytecodeKlassMetaData>,
    private val loader: ClassLoader
) : AutoCloseable {
    private val externals = HashMap<String, BytecodeKlassMetaData?>()

    inline fun <reified T : Annotation> annotated(): List<Class<*>> {
        return annotated(T::class.java)
    }

    fun <T : Annotation> annotated(klass: Class<T>): List<Class<*>> {
        val desc = "L" + klass.name.replace('.', '/') + ";"
        val list = ArrayList<Class<*>>()

        for ((name, meta) in metas) {
            if (!meta.annotations.contains(desc)) continue

            try {
                list.add(Class.forName(name, false, loader))
            } catch (_: Throwable) {
            }
        }

        return list
    }

    inline fun <reified T : Any> subtypes(): List<Class<out T>> {
        return subtypes(T::class.java)
    }

    fun <T : Any> subtypes(klass: Class<T>): List<Class<out T>> {
        val target = klass.name
        val list = ArrayList<Class<out T>>()

        for (name in metas.keys) {
            if (name == target) continue
            if (!typed(name, target)) continue

            try {
                val klass0 = Class.forName(name, false, loader)
                if (!klass.isAssignableFrom(klass0)) continue

                list.add(klass0 as Class<out T>)
            } catch (_: Throwable) {
            }
        }

        return list
    }

    private fun meta(name: String): BytecodeKlassMetaData? {
        if (name == "java.lang.Object") return null

        metas[name]?.let {
            return it
        }

        externals[name]?.let {
            return it
        }

        return loader.getResourceAsStream(name.replace('.', '/') + ".class")?.use { BytecodeKlassReader.get(it) }?.also { externals[name] = it }
    }

    private fun typed(name: String, target: String): Boolean {
        if (name == target) return true
        val meta = meta(name) ?: return false
        return chain(meta, target)
    }

    private fun chain(meta: BytecodeKlassMetaData, target: String): Boolean {
        return meta.superName != null && typed(meta.superName, target) || meta.interfaces.any { typed(it, target) }
    }

    override fun close() {
    }
}
