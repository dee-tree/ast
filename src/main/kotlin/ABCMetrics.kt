import kotlin.math.roundToInt
import kotlin.math.sqrt

class ABCMetrics(
    assignmentsField: Int = 0,
    branchesField: Int = 0,
    conditionsField: Int = 0
) {
    var assignments = assignmentsField
        private set

    var branches = branchesField
        private set

    var conditions = conditionsField
        private set


    val scalar get() = sqrt((assignments * assignments + branches * branches + conditions * conditions).toDouble()).roundToInt()

    operator fun plus(other: ABCMetrics): ABCMetrics = ABCMetrics(
        assignments + other.assignments,
        branches + other.branches,
        conditions + other.conditions
    )

    fun foundAssignment() {
        assignments++
    }

    fun foundBranch() {
        branches++
    }

    fun foundCondition() {
        conditions++
    }
}