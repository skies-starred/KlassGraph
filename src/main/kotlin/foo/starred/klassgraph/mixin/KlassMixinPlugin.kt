package foo.starred.klassgraph.mixin

import net.fabricmc.loader.api.FabricLoader
import org.objectweb.asm.tree.ClassNode
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin
import org.spongepowered.asm.mixin.extensibility.IMixinInfo
import java.net.JarURLConnection
import java.nio.file.Files
import java.nio.file.Path
import java.util.TreeSet

open class KlassMixinPlugin(
    private var pack: String = ""
) : IMixinConfigPlugin {
    private var self: String = ""

    override fun onLoad(mixinPackage: String) {
        if (pack.isEmpty()) pack = mixinPackage
        self = javaClass.simpleName + ".class"
    }

    override fun getRefMapperConfig(): String? {
        return null
    }

    override fun shouldApplyMixin(targetClassName: String, mixinClassName: String): Boolean {
        return true
    }

    override fun acceptTargets(myTargets: Set<String>, otherTargets: Set<String>) {
    }

    override fun getMixins(): List<String> {
        val mixins = TreeSet<String>()
        val path = pack.replace('.', '/')

        try {
            for (url in javaClass.classLoader.getResources(path)) {
                when (url.protocol) {
                    "file" -> directory(Path.of(url.toURI()), mixins)
                    "jar" -> jar(url.openConnection() as JarURLConnection, path, mixins)
                }
            }
        } catch (_: Throwable) {
        }

        return ArrayList(mixins)
    }

    override fun preApply(targetClassName: String, targetClass: ClassNode, mixinClassName: String, mixinInfo: IMixinInfo) {
    }

    override fun postApply(targetClassName: String, targetClass: ClassNode, mixinClassName: String, mixinInfo: IMixinInfo) {
    }

    open fun valid(path: String): Boolean {
        if (!path.endsWith(".class")) return false
        if (path.contains("$")) return false
        if (path.endsWith(self)) return false

        val id = path.replace('\\', '/').replace('.', '/')
        val i0 = id.indexOf("compat/")
        return i0 == -1 || loaded(id.substring(i0 + 7).split("/")[0])
    }

    open fun loaded(id: String): Boolean {
        return try {
            FabricLoader.getInstance().isModLoaded(id)
        } catch (_: Throwable) {
            true
        }
    }

    private fun directory(dir: Path, mixins: MutableSet<String>) {
        if (!Files.isDirectory(dir)) return

        Files.walk(dir).use { stream ->
            stream.filter { Files.isRegularFile(it) }.forEach {
                val path = dir.relativize(it).toString()
                if (!valid(path)) return@forEach

                mixins.add(name(path))
            }
        }
    }

    private fun jar(connection: JarURLConnection, path: String, mixins: MutableSet<String>) {
        connection.useCaches = false

        connection.jarFile.use { jar ->
            for (entry in jar.entries()) {
                val name = entry.name
                if (!name.startsWith("$path/")) continue
                if (!valid(name)) continue

                mixins.add(name.substring(path.length + 1, name.length - 6).replace('/', '.'))
            }
        }
    }

    private fun name(path: String): String {
        return path.substring(0, path.length - 6).replace('\\', '/').replace('/', '.')
    }
}
