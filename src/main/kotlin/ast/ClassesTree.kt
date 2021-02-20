package ast

import structures.FullName
import structures.KotlinClass
import java.util.*

/**
 * @author Dmitriy Sokolov
 * Tree of project's classes by their inheritance structure
 */
class ClassesTree(root: KotlinClass) {

    private val node: KotlinClass = root

    private val children = mutableListOf<ClassesTree>()

    fun add(child: ClassesTree) {
        children.add(child)
    }

    fun find(classFullName: FullName): ClassesTree? {
        if (node.fullName == classFullName)
            return this

        children.forEach { child ->
            val found = child.find(classFullName)

            if (found != null)
                return found
        }
        return null
    }

    fun inheritanceDepth(): Int {
        if (children.isEmpty())
            return 0
        return children.maxOf { it.inheritanceDepth() + 1 }
    }


    fun toStringAsTree(indent: Int = 0): String {
        val joiner = StringJoiner("")
        joiner.add("\t".repeat(indent))
        if (children.isEmpty()) {
            joiner.add(node.fullName.toString())
        } else {
            joiner.add("${node.fullName}: ")
            joiner.add(children.joinToString(separator = "\n", prefix = "\n") { it.toStringAsTree(indent + 1) })
        }
        return joiner.toString()
    }

    override fun toString(): String {
        val joiner = StringJoiner("")
        if (children.isEmpty()) {
            joiner.add(node.fullName.toString())
        } else {
            joiner.add("${node.fullName}: ")
            joiner.add("[")
            joiner.add(children.joinToString(separator = ", ") { it.toString() })
            joiner.add("]")
        }
        return joiner.toString()
    }

    fun forEachClass(action: (KotlinClass) -> Unit) {
        action(node)

        children.forEach { it.forEachClass(action) }
    }

    fun classesNum(): Int {
        var count = 0

        forEachClass { count++ }
        return count
    }
}