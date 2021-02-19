import ast.ClassesTreesBuilder
import ast.TreesMetrics
import org.jetbrains.kotlin.spec.grammar.tools.tokenizeKotlinCode
import parser.Parser
import structures.KotlinClass
import java.io.File

class KotlinProjectExplorer(private val rootFile: File) {

    init {
        if (!rootFile.exists() || !rootFile.isDirectory) {
            throw IllegalArgumentException("Folder (${rootFile.name}) does not exist!")
        }
    }

    fun process() {
        val classes = mutableListOf<KotlinClass>()

        rootFile.walkTopDown().forEach { kotlinFile ->
            if (kotlinFile.extension == "kt") {
//                println("file: ${kotlinFile.name}")

//                if (kotlinFile.nameWithoutExtension == "KotlinProjectExplorer") {
                    val tokenized = tokenizeKotlinCode(kotlinFile.readText())

                    val parser = Parser(tokenized)

                    classes.addAll(parser.parseClasses())
//                }
            }
        }

        val trees = ClassesTreesBuilder(classes).build()
//        println(trees)
//
//        val metrics = TreesMetrics(trees)
//        println("abc: ${metrics.abc}")
//        println("max inheritance: ${metrics.maxInheritanceDepth}")
//        println("mean inheritance: ${metrics.meanInheritanceDepth}")
//        println("mean overridden methods: ${metrics.meanOverriddenMethodNum}")
//        println("mean properties: ${metrics.meanPropertiesNum}")
//        println("depths: ${trees.forEach { println(it.inheritanceDepth()) }}}")
    }
}