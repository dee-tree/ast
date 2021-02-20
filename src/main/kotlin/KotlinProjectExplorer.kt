import ast.ClassesTree
import ast.ClassesTreesBuilder
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

    fun process(): List<ClassesTree> {
        val classes = mutableListOf<KotlinClass>()

        rootFile.walkTopDown().forEach { kotlinFile ->
            if (kotlinFile.extension == "kt") {
                val tokenized = tokenizeKotlinCode(kotlinFile.readText())
                val parser = Parser(tokenized)

                classes.addAll(parser.parseClasses())
            }
        }

        return ClassesTreesBuilder(classes).build()
    }
}