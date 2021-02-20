import ast.TreesMetrics
import com.google.gson.Gson
import java.io.File

fun main(args: Array<String>) {
    println("Enter path to root folder of Kotlin project to analyze its classes:")
    val kotlinProjectPath = readLine() ?: error("Project path must not be null!")

    println("\nEnter preferred path for output metrics or just press Enter to out in the same directory:")
    val outputPath = readLine() ?: ""

    val rootFile = File(kotlinProjectPath)

    if (!rootFile.isDirectory)
        error("You chose invalid directory!")

    val trees = KotlinProjectExplorer(rootFile).process()

    println("Trees of classes by inheritance:")
    trees.forEach { tree -> println(tree.toStringAsTree(1)) }
    println()

    val outputFileMetrics = File((if (outputPath.isNotEmpty()) outputPath else kotlinProjectPath) + "\\metrics.json")
    if (!outputFileMetrics.parentFile.exists())
        error("You chose invalid directory!")

    val metrics = TreesMetrics(trees)
    outputFileMetrics.writeText(Gson().toJson(metrics))

    println(metrics)
}