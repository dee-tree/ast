package structures

const val SEPARATOR = "."

/**
 * @author Dmitriy Sokolov
 * Representation of project's package
 */
class Package(vararg val domain: String) {

    fun isParent(supposedChild: Package): Boolean {
        if (this.domain.size >= supposedChild.domain.size)
            return false

        for (i in this.domain.indices) {
            if (this.domain[i] != supposedChild.domain[i])
                return false
        }
        return true
    }

    fun isChild(supposedParent: Package): Boolean {
        if (this.domain.size <= supposedParent.domain.size)
            return false

        for (i in this.domain.indices) {
            if (this.domain[i] != supposedParent.domain[i])
                return false
        }
        return true
    }

    override fun equals(other: Any?): Boolean {
        if (other == null)
            return false
        if (other.javaClass != this.javaClass)
            return false

        if (this.domain.size != (other as Package).domain.size)
            return false

        for (i in this.domain.indices) {
            if (this.domain[i] != other.domain[i])
                return false
        }
        return true
    }

    override fun toString(): String {
        return domain.joinToString(separator = SEPARATOR)
    }

    override fun hashCode(): Int {
        return domain.contentHashCode()
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