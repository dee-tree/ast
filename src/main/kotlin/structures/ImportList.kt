package structures

class ImportList(val imports: MutableCollection<Import> = mutableListOf()) {

    fun add(import: Import) {
        imports.add(import)
    }

    fun resolveImport(name: String, from: Package, classes: Collection<KotlinClass>): FullName {
        imports.forEach { import ->
            // import x as
            if (import.like != null && import.like == name)
                return FullName(import.what!!, import.from)

            // import x
            if (import.what != null && import.what == name)
                return FullName(import.what, import.from)

            // import all
            if (import.what == null) {
                classes.forEach { klass ->
                    if ((import.from.isParent(klass.fullName.pack) || import.from == klass.fullName.pack) && name == klass.fullName.name)
                        return klass.fullName
                }
            }
        }

        val split = name.split(".")

        return if (name.contains('.')) FullName(split.last(), Package(*split.subList(0, split.size - 1).toTypedArray())) else FullName(name, from)
    }
}