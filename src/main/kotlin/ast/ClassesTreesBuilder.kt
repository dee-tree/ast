package ast

import structures.KotlinClass
import structures.FullName

class ClassesTreesBuilder(private var classes: Collection<KotlinClass>) {

    fun build(): List<ClassesTree> {
        val trees = mutableListOf<ClassesTree>()

        val set = classes.toMutableSet()

        val removeSet = mutableSetOf<KotlinClass>()

        set.forEach { klass ->
            if (klass.superClass == null || !isSuperClassInProject(klass)) {
                trees.add(ClassesTree(klass))
                removeSet.add(klass)
            }
        }
        set.removeAll(removeSet)
        removeSet.clear()

        while (set.isNotEmpty()) {
            set.forEach { klass ->
                val found = findInTrees(klass.superClass!!, trees)

                found?.let {
                    it.add(ClassesTree(klass))
                    removeSet.add(klass)
                }
            }
            set.removeAll(removeSet)
            removeSet.clear()
        }
        return trees
    }

    private fun isSuperClassInProject(klass: KotlinClass): Boolean {
        classes.forEach {

            if (klass.superClass == it.fullName.toString())
                return true
        }
        return false
    }

    private fun findInTrees(classFullName: String, trees: Collection<ClassesTree>): ClassesTree? {
        trees.forEach { tree ->
            val found = tree.find(classFullName)
            if (found != null)
                return found
        }

        return null
    }
}