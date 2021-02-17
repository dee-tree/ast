import kotlin.math.sqrt

data class ABCMetrics(
    var assignments: Int = 0,
    var branches: Int = 0,
    var conditions: Int = 0
) {

    fun foundAssignment() {
        assignments++
    }

    fun foundBranch() {
        branches++
    }

    fun foundCondition() {
        conditions++
    }

    fun eval() = sqrt((assignments * assignments + branches * branches + conditions * conditions).toDouble())
}