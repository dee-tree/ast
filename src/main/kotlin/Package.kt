public const val SEPARATOR = "."
open class Package(vararg val domain: String) {

    override fun toString(): String {
        return domain.joinToString(separator = SEPARATOR)
    }

    companion object {
        fun defaultPackage() = Package()
    }

    class Builder {
        private var domain = mutableListOf<String>()

        fun addDomainLevel(part: String) = apply { this.domain.add(part) }
        fun build() = Package(*domain.toTypedArray())
    }
}