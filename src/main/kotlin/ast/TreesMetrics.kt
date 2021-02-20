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

    val abc: ABCMetrics = (trees.fold(ABCMetrics()) { acc, classesTree ->
        var abc = ABCMetrics()
        classesTree.forEachClass { abc += it.abc }
        acc + abc
    })

    override fun toString(): String =
        "Metrics of chosen project:\n" +
                " Max classes inheritance depth: $maxInheritanceDepth\n" +
                " Mean classes inheritance depth: $meanInheritanceDepth\n" +
                " Mean classes' properties number: $meanPropertiesNum\n" +
                " Mean classes' overridden methods number: $meanOverriddenMethodNum\n" +
                " ABC metrics:\n" +
                "\t Assignments: ${abc.assignments}\n" +
                "\t Branches: ${abc.branches}\n" +
                "\t Conditions: ${abc.conditions}\n"
}