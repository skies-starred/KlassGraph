package foo.starred.klassgraph.core.scan.helper

import foo.starred.klassgraph.core.data.internal.BytecodeKlassMetaData
import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Opcodes
import java.io.InputStream

object BytecodeKlassReader {
    fun get(stream: InputStream): BytecodeKlassMetaData? {
        return try {
            val reader = ClassReader(stream)
            val visitor = MetaVisitor()

            reader.accept(visitor, ClassReader.SKIP_CODE or ClassReader.SKIP_DEBUG or ClassReader.SKIP_FRAMES)
            BytecodeKlassMetaData(visitor.name, visitor.super0, visitor.interfaces, visitor.annotations)
        } catch (_: Throwable) {
            null
        }
    }

    private class MetaVisitor : ClassVisitor(Opcodes.ASM9) {
        val interfaces = ArrayList<String>()
        val annotations = HashSet<String>()

        var name: String = ""
        var super0: String? = null

        override fun visit(version: Int, access: Int, name: String, sig: String?, super0: String?, interfaces: Array<String>?) {
            this.name = name.replace('/', '.')
            this.super0 = super0?.replace('/', '.')
            interfaces?.mapTo(this.interfaces) { it.replace('/', '.') }
        }

        override fun visitAnnotation(descriptor: String, visible: Boolean): AnnotationVisitor? {
            annotations.add(descriptor)
            return null
        }
    }
}
