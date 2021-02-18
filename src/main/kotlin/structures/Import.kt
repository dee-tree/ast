package structures

class Import private constructor(val from: Package, val what: String?, val like: String?) {

    companion object {
        fun importAll(from: Package) = Import(from, null, null)

        fun import(from: Package, what: String) = Import(from, what, null)
        fun import(from: Package, what: String, like: String) = Import(from, what, like)
    }

    override fun toString(): String {
        return from.toString() + "." + (what ?: "*") + (if (like != null) " as $like" else "")
    }
}