package structures

data class FullName(val name: String, val pack: Package = Package.defaultPackage()) {

    override fun toString(): String = if (pack == Package.defaultPackage()) name else "$pack.$name"
}