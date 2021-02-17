package ast

import parser.KotlinClass
import java.util.*

class ClassesTree(root: KotlinClass) {


    val node: KotlinClass = root

    val children = mutableListOf<ClassesTree>()

    fun add(child: ClassesTree) {
        children.add(child)
    }

    fun find(classFullName: String): ClassesTree? {
        if (node.fullName == classFullName)
            return this

        children.forEach { child ->
            val found = child.find(classFullName)

            if (found != null)
                return found
        }
        return null
    }

    override fun toString(): String {
        val joiner = StringJoiner("", "[ ", " ]")
        if (children.isEmpty()) {
            joiner.add(node.fullName)
        }
        else {
            joiner.add("${node.fullName}: ")
            children.forEach { joiner.add(it.toString()) }
        }
        return joiner.toString()
    }
}