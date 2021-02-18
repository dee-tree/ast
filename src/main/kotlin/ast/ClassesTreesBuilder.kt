package ast

import structures.FullName
import structures.KotlinClass

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
                val found = findInTrees(klass.importList.resolveImport(klass.superClass!!, klass.fullName.pack, classes), trees)

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
            if (klass.superClass != null &&
                it.importList.resolveImport(klass.superClass, it.fullName.pack, classes)
                    .toString() == it.fullName.toString()
            ) return true
        }
        return false
    }

    private fun findInTrees(classFullName: FullName, trees: Collection<ClassesTree>): ClassesTree? {
        trees.forEach { tree ->
            val found = tree.find(classFullName)
            if (found != null)
                return found
        }
        return null
    }
}