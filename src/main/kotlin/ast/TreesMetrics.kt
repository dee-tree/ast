package ast

import ABCMetrics
import kotlin.math.roundToInt

data class TreesMetrics(@Transient private val trees: Collection<ClassesTree>) {

    val maxInheritanceDepth: Int = trees.maxOf { it.inheritanceDepth() }

    val meanInheritanceDepth: Int = (trees.fold(0) { acc, classesTree -> acc + classesTree.inheritanceDepth() }
        .toDouble() / trees.size).roundToInt()

    @Transient
    private val classesNum: Int = trees.fold(0) { acc, classesTree -> acc + classesTree.classesNum() }

    val meanOverriddenMethodNum: Int =
        (trees.fold(0) { acc, classesTree ->
            var overriddenMethodsNum = 0
            classesTree.forEachClass { overriddenMethodsNum += it.overriddenMethodsCount }
            acc + overriddenMethodsNum
        }.toDouble() / classesNum).roundToInt()

    val meanPropertiesNum: Int = (trees.fold(0) { acc, classesTree ->
        var propertiesNum = 0
        classesTree.forEachClass { propertiesNum += it.propertiesCount }
        acc + propertiesNum
    }.toDouble() / classesNum).roundToInt()

//    val abc: Int = (trees.fold(0) { acc, classesTree ->
//        var abc = 0
//        classesTree.forEachClass { abc += it.abc.eval() }
//        acc + abc
//    })

    val abc: ABCMetrics = (trees.fold(ABCMetrics()) { acc, classesTree ->
        var abc = ABCMetrics()
        classesTree.forEachClass { abc += it.abc }
        acc + abc
    })
}