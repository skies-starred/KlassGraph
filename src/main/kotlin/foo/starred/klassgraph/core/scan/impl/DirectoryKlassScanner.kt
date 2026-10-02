package foo.starred.klassgraph.core.scan.impl

import foo.starred.klassgraph.core.data.internal.BytecodeKlassMetaData
import foo.starred.klassgraph.core.scan.helper.BytecodeKlassReader
import java.io.File

object DirectoryKlassScanner {
    fun get(dir: File, path: String, classes: MutableMap<String, BytecodeKlassMetaData>) {
        val target = if (path.isEmpty() || dir.path.replace('\\', '/').endsWith(path)) dir else File(dir, path)

        if (!target.exists()) return
        if (!target.isDirectory) return

        target.walkTopDown()
            .filter { it.isFile && it.extension == "class" }
            .forEach { file ->
                file.inputStream().use { stream ->
                    val meta = BytecodeKlassReader.get(stream) ?: return@forEach
                    classes[meta.name] = meta
                }
            }
    }
}
