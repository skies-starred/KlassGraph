package foo.starred.klassgraph

import foo.starred.klassgraph.core.data.internal.BytecodeKlassMetaData
import foo.starred.klassgraph.core.data.scan.KlassScanResult
import foo.starred.klassgraph.core.scan.base.GenericKlassScanner

object KlassGraph {
    fun scan(pack: String = "", loader: ClassLoader = loader()): KlassScanResult {
        val path = pack.replace('.', '/')
        val metas = LinkedHashMap<String, BytecodeKlassMetaData>()

        for (url in loader.getResources(path)) {
            GenericKlassScanner.get(url, path, metas)
        }

        return KlassScanResult(metas, loader)
    }

    fun scan(context: Class<*>, pack: String = context.packageName): KlassScanResult {
        val loader = context.classLoader ?: loader()
        val path = pack.replace('.', '/')
        val metas = LinkedHashMap<String, BytecodeKlassMetaData>()

        context.protectionDomain?.codeSource?.location?.let {
            GenericKlassScanner.get(it, path, metas)
        }

        for (url in loader.getResources(path)) {
            GenericKlassScanner.get(url, path, metas)
        }

        return KlassScanResult(metas, loader)
    }

    private fun loader(): ClassLoader {
        return Thread.currentThread().contextClassLoader ?: KlassGraph::class.java.classLoader
    }
}
