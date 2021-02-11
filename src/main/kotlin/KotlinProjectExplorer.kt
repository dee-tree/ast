import lexing.Lexer
import java.io.File


class KotlinProjectExplorer(private val rootFile: File) {

    init {
        if (!rootFile.exists() || !rootFile.isDirectory) {
            throw IllegalArgumentException("Folder (${rootFile.name}) does not exist!")
        }
    }

    fun process() {
        rootFile.walkTopDown().forEach { kotlinFile ->
            if (kotlinFile.extension == "kt") {
                println("file: ${kotlinFile.name}")

                val lexer = Lexer(kotlinFile)
                do {
                    val nextToken = lexer.nextToken()
                    println("next: $nextToken")
                } while (nextToken != null)
            }
        }

    }

}

