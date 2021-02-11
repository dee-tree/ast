package lexing

import java.io.File
import java.util.*

class Lexer(kotlinFile: File) {

    private val identifierRegex = Regex(IDENTIFIER)

    private var tokenizer = StringTokenizer(kotlinFile.readText(), ";\u0020\u0009\u000C\r\n$ASTERISK$SLASH\"\'\\", true)

    fun nextToken(): Token? {
        var token: Token? = null
        do {
            if (tokenizer.hasMoreTokens()) {
                val rawToken = tokenizer.nextToken()

                if (rawToken == SLASH) {
                    if (tokenizer.hasMoreElements()) {

                        // second char of comment
                        when (tokenizer.nextToken()) {
                            SLASH -> { skipSingleLineComm(); continue }
                            ASTERISK -> { skipMultilineComm(); continue }

                            else -> throw TokenizationException("Unexpected char in comment")
                        }
                    } else throw TokenizationException("Unexpected EOF. Error parsing comment")
                } else if (rawToken == "\'") {
                    skipCharacter()
                    continue
                } else if (rawToken == "\"") {
                    skipString()
                    continue
                }


                token = getToken(rawToken)

            } else {
                break
            }
        } while (token == null)

        return token
    }

    private fun skipCharacter() {
        var token: String?

        do {
            if (tokenizer.hasMoreElements()) {
                token = tokenizer.nextToken()
                if (token == "\\")
                    tokenizer.nextToken()
            } else {
                break
            }
        } while (token != "\'")
    }

    private fun skipString() {
        var token: String?

        do {
            if (tokenizer.hasMoreElements()) {
                token = tokenizer.nextToken()
                if (token == "\\")
                    tokenizer.nextToken()
            } else {
                break
            }
        } while (token != "\"")
    }


    private fun skipSingleLineComm() {
        var token: String?

        do {
            if (tokenizer.hasMoreElements()) {
                token = tokenizer.nextToken()
            } else {
                break
            }
        } while (token != "\n" && token != "\r")
    }

    private fun skipMultilineComm() {
        var token: String?

        do {
            do {
                if (tokenizer.hasMoreElements()) {
                    token = tokenizer.nextToken()
                } else {
                    break
                }
            } while (token != "*")

            if (!tokenizer.hasMoreElements())
                throw TokenizationException("Unexpected EOF. Error on closing multiline comment")
            token = tokenizer.nextToken()

        } while (token != "/")
    }

    private fun getToken(token: String): Token? {
        val result = when (token) {
            CLASS -> Token(TokenTypes.CLASS)
            PRIVATE -> Token(TokenTypes.PRIVATE)
            PROTECTED -> Token(TokenTypes.PROTECTED)
            INTERNAL -> Token(TokenTypes.INTERNAL)
            PUBLIC -> Token(TokenTypes.PUBLIC)
            FINAL -> Token(TokenTypes.FINAL)
            OPEN -> Token(TokenTypes.OPEN)
            OVERRIDE -> Token(TokenTypes.OVERRIDE)
            LCURL -> Token(TokenTypes.LCURL)
            RCURL -> Token(TokenTypes.RCURL)
            IDENTIFIER -> Token(TokenTypes.IDENTIFIER, token)
            FUN -> Token(TokenTypes.FUN)

            else -> null
        }

        if (result == null && identifierRegex.matches(token))
            return Token(TokenTypes.IDENTIFIER, token)

        return result
    }

}

const val LETTER = """[a-zA-Z]"""
const val DIGIT = """[0-9]"""
const val IDENTIFIER = """($LETTER|_)($LETTER|$DIGIT|_)*"""


const val CLASS = "class"
const val PRIVATE = "private"
const val PROTECTED = "protected"
const val INTERNAL = "internal"
const val PUBLIC = "public"
const val FINAL = "final"
const val OPEN = "open"
const val OVERRIDE = "override"

const val FUN = "fun"

const val ASTERISK = "*"
const val SLASH = "/"

const val LCURL = "{"
const val RCURL = "}"