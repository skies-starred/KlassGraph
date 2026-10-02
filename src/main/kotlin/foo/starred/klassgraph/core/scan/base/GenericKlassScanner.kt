package foo.starred.klassgraph.core.scan.base

import foo.starred.klassgraph.core.data.internal.BytecodeKlassMetaData
import foo.starred.klassgraph.core.scan.impl.DirectoryKlassScanner
import foo.starred.klassgraph.core.scan.impl.JarKlassScanner
import java.io.File
import java.net.JarURLConnection
import java.net.URL
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.util.jar.JarFile

object GenericKlassScanner {
    fun get(url: URL, path: String, classes: MutableMap<String, BytecodeKlassMetaData>) {
        try {
            when (url.protocol) {
                "file" -> file(url, path, classes)
                "jar" -> jar(url, path, classes)
            }
        } catch (_: Throwable) {
        }
    }

    private fun file(url: URL, path: String, classes: MutableMap<String, BytecodeKlassMetaData>) {
        val file = runCatching { File(url.toURI()) }.getOrElse { File(url.path) }

        if (file.isDirectory) {
            DirectoryKlassScanner.get(file, path, classes)
            return
        }

        if (file.extension.equals("jar", ignoreCase = true)) {
            JarFile(file).use { JarKlassScanner.get(it, path, classes) }
        }
    }

    private fun jar(url: URL, path: String, classes: MutableMap<String, BytecodeKlassMetaData>) {
        val connection = url.openConnection()
        if (connection is JarURLConnection) {
            connection.useCaches = false
            connection.jarFile.use { JarKlassScanner.get(it, path, classes) }
            return
        }

        val url = url.path
        val separator = url.indexOf("!/")
        if (separator == -1) return

        val jar = url.substring(0, separator).removePrefix("file:")
        val decoded = URLDecoder.decode(jar, StandardCharsets.UTF_8.name())
        JarFile(File(decoded)).use { JarKlassScanner.get(it, path, classes) }
    }
}
