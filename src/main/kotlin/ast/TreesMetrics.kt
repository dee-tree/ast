package ast

import kotlin.math.roundToInt

class TreesMetrics(private val trees: Collection<ClassesTree>) {

    val maxInheritanceDepth: Int
        get() = trees.maxOf { it.inheritanceDepth() }

    val meanInheritanceDepth: Int
        get() = (trees.fold(0) {acc, classesTree -> acc + classesTree.inheritanceDepth() }.toDouble() / trees.size).roundToInt()

    private val classesNum: Int
        get() = trees.fold(0) { acc, classesTree -> acc + classesTree.classesNum() }

    val meanOverriddenMethodNum: Int
        get() =
            (trees.fold(0) { acc, classesTree ->
                var overriddenMethodsNum = 0
                classesTree.forEachClass { overriddenMethodsNum += it.overriddenMethodsCount }
                acc + overriddenMethodsNum
            }.toDouble() / classesNum).roundToInt()

    val meanPropertiesNum: Int
        get() = (trees.fold(0) { acc, classesTree ->
            var propertiesNum = 0
            classesTree.forEachClass { propertiesNum += it.propertiesCount }
            acc + propertiesNum
        }.toDouble() / classesNum).roundToInt()
}