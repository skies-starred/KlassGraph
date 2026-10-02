package foo.starred.klassgraph.core.data.internal

class BytecodeKlassMetaData(
    val name: String,
    val superName: String?,
    val interfaces: List<String>,
    val annotations: Set<String>
)
