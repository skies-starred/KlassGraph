package foo.starred.klassgraph.core.scan.impl

import foo.starred.klassgraph.core.data.internal.BytecodeKlassMetaData
import foo.starred.klassgraph.core.scan.helper.BytecodeKlassReader
import java.util.jar.JarFile

object JarKlassScanner {
    fun get(jar: JarFile, path: String, classes: MutableMap<String, BytecodeKlassMetaData>) {
        val prefix = if (path.isEmpty() || path.endsWith('/')) path else "$path/"

        for (entry in jar.entries()) {
            if (entry.isDirectory) continue

            val name = entry.name
            if (!name.startsWith(prefix)) continue
            if (!name.endsWith(".class")) continue

            jar.getInputStream(entry).use { stream ->
                val meta = BytecodeKlassReader.get(stream) ?: return@use
                classes[meta.name] = meta
            }
        }
    }
}
