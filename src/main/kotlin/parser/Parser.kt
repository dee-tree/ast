package parser

import org.jetbrains.kotlin.spec.grammar.tools.KotlinTokensList
import structures.*
import java.util.*

const val PACKAGE_TOKEN = "PACKAGE"
const val IMPORT_TOKEN = "IMPORT"
const val AS_TOKEN = "AS"
const val CLASS_TOKEN = "CLASS"
const val FUN_TOKEN = "FUN"
const val CONSTRUCTOR_TOKEN = "CONSTRUCTOR"

const val OPEN_TOKEN = "OPEN"
const val FINAL_TOKEN = "FINAL"
const val ABSTRACT_TOKEN = "ABSTRACT"

const val PRIVATE_TOKEN = "PRIVATE"
const val PROTECTED_TOKEN = "PROTECTED"
const val INTERNAL_TOKEN = "INTERNAL"
const val PUBLIC_TOKEN = "PUBLIC"

const val OVERRIDE_TOKEN = "OVERRIDE"
const val IDENTIFIER_TOKEN = "Identifier"
const val WS_TOKEN = "WS"
const val INSIDE_WS_TOKEN = "Inside_WS"
const val NL_TOKEN = "NL"
const val INSIDE_NL_TOKEN = "Inside_NL"
const val DELIMITED_COMMENT_TOKEN = "DelimitedComment"
const val LINE_COMMENT_TOKEN = "LineComment"
const val COLON_TOKEN = "COLON"
const val COLONCOLON_TOKEN = "COLONCOLON"
const val DOT_TOKEN = "DOT"
const val COMMA_TOKEN = "COMMA"
const val SEMICOLON_TOKEN = "SEMICOLON"

const val LCURL_TOKEN = "LCURL"
const val LINE_STR_EXPR_START_TOKEN = "LineStrExprStart"
const val RCURL_TOKEN = "RCURL"
const val LPAREN_TOKEN = "LPAREN"
const val RPAREN_TOKEN = "RPAREN"
const val LANGLE_TOKEN = "LANGLE"
const val RANGLE_TOKEN = "RANGLE"

const val VAR_TOKEN = "VAR"
const val VAL_TOKEN = "VAL"

const val MULT_ASSIGNMENT_TOKEN = "MULT_ASSIGNMENT"
const val ADD_ASSIGNMENT_TOKEN = "ADD_ASSIGNMENT"
const val SUB_ASSIGNMENT_TOKEN = "SUB_ASSIGNMENT"
const val DIV_ASSIGNMENT_TOKEN = "DIV_ASSIGNMENT"
const val MOD_ASSIGNMENT_TOKEN = "MOD_ASSIGNMENT"
const val ASSIGNMENT_TOKEN = "ASSIGNMENT"
const val EXCL_EQ_TOKEN = "EXCL_EQ"
const val INCR_TOKEN = "INCR"
const val DECR_TOKEN = "DECR"
const val LE_TOKEN = "LE"
const val GE_TOKEN = "GE"
const val EQEQ_TOKEN = "EQEQ"

const val IF_TOKEN = "IF"
const val ELSE_TOKEN = "ELSE"
const val TRY_TOKEN = "TRY"
const val CATCH_TOKEN = "CATCH"
const val EXCL_NO_WS_TOKEN = "EXCL_NO_WS"

const val AT_TOKEN = "AT_PRE_WS"

/**
 * @author Dmitriy Sokolov
 * Parses classes of the file and general metrics of them from list of tokens: <tt>tokens</tt>
 */
class Parser(private val tokens: KotlinTokensList) {

    private lateinit var pack: Package

    private var imports = ImportList()

    var classes = mutableListOf<KotlinClass>()
    private set

    fun parseClasses(): Collection<KotlinClass> {

        parsePackage()
        parseImports()

        var idx = parseNextClass(0)
        while (idx != tokens.size) {
            idx = parseNextClass(idx)
        }
        return classes
    }

    /**
     * @param idx is index of 'class' keyword
     * @return true if 'class' keyword is met in callable reference, not in class declaration
     */
    private fun isCallableReference(idx: Int): Boolean {
        if (idx > 0) {
            if (isSpace(idx - 1)) {
                val beforeSpaces = skipSpacesToLeft(idx - 1)
                return if (beforeSpaces > 0) tokens[beforeSpaces].type == COLONCOLON_TOKEN else false
            } else {
                return tokens[idx - 1].type == COLONCOLON_TOKEN
            }
        }
        return false
    }

    /**
     * @return index of the "class" keyword in declaration
     */
    private fun findClassDeclaration(from: Int): Int {
        var idx = from - 1
        do {
            idx++
            while (idx < tokens.size && tokens[idx].type != CLASS_TOKEN)
                idx++

            if (idx >= tokens.size)
                return tokens.size  // class not found

        } while (isCallableReference(idx))

        return idx
    }

    /**
     * @param from index of 'class' keyword
     * @param builder builder, where write modifiers
     */
    private fun parseClassModifiers(from: Int, builder: KotlinClass.Builder) {
        var idx = skipSpacesToLeft(from - 1)

        // check 3 words before "class" keyword
        for (x in 1..3) {
            if (idx < 0)
                break

            when (tokens[idx].type) {
                FINAL_TOKEN -> builder.inheritanceMode(InheritanceModifiers.FINAL)
                OPEN_TOKEN -> builder.inheritanceMode(InheritanceModifiers.OPEN)
                ABSTRACT_TOKEN -> builder.inheritanceMode(InheritanceModifiers.ABSTRACT)

                PRIVATE_TOKEN -> builder.visibilityMode(VisibilityModifiers.PRIVATE)
                PROTECTED_TOKEN -> builder.visibilityMode(VisibilityModifiers.PROTECTED)
                INTERNAL_TOKEN -> builder.visibilityMode(VisibilityModifiers.INTERNAL)
                PUBLIC_TOKEN -> builder.visibilityMode(VisibilityModifiers.PUBLIC)
            }

            if (idx == 0)
                break
            idx = skipSpacesToLeft(idx - 1)
        }
    }

    /**
     * @param from is index of the first word in superclass/interfaces
     * @param builder class builder where superclass should be written
     * @return index of the next token
     */
    private fun parseSuperClass(from: Int, builder: KotlinClass.Builder): Int {
        var idx = from - 1
        do {
            idx++

            if (isSpace(idx))
                idx = skipSpacesToRight(idx)

            idx = skipAnnotationsIfNecessary(idx)

            val userTypePair = getUserType(idx)
            idx = userTypePair.second

            if (idx == tokens.size) {
                return idx
            }

            if (isSpace(idx))
                idx = skipSpacesToRight(idx)

            if (idx < tokens.size && tokens[idx].type == LPAREN_TOKEN) {
                builder.superClass((userTypePair.first))
                while (tokens[idx].type != RPAREN_TOKEN)
                    idx++
                idx++
            }

            if (idx == tokens.size)
                return tokens.size
        } while (tokens[idx].type == COMMA_TOKEN)
        return idx
    }

    /**
     * @return the index of the next token after "class" keywords or token.size in case of lack of class
     */
    private fun parseNextClass(from: Int): Int {
        var idx = from

        idx = findClassDeclaration(idx)

        if (idx >= tokens.size)
            return tokens.size  // class not found

        idx = skipSpacesToRight(idx + 1)

        val classBuilder = KotlinClass.Builder(className = tokens[idx].text, importList = imports)
        idx++
        if (idx == tokens.size) {
            classes.add(classBuilder.build())
            return tokens.size
        } else {
            if (skipSpacesToRight(idx) == tokens.size)
                return tokens.size
        }
        val tokenAfterClassIdx = idx

        classBuilder.pack(this.pack)

        parseClassModifiers(tokenAfterClassIdx - 1, classBuilder)

        if (isSpace(idx))
            idx = skipSpacesToRight(idx)

        idx = skipTypeModifierIfNecessary(idx)
        idx = skipConstructorPartBeforeArgumentsIfNecessary(idx)
        if (tokens[idx].type == LPAREN_TOKEN) {
            classBuilder.addProperty(parsePropertiesInsideConstructor(idx + 1))
            idx = skipSimpleConstructorIfNecessary(idx)
        }

        if (idx < tokens.size && isSpace(idx))
            idx = skipSpacesToRight(idx)

        // is there inheritance checking
        if (idx < tokens.size && tokens[idx].type == COLON_TOKEN) {
            idx = skipSpacesToRight(idx + 1)
            idx = parseSuperClass(idx, classBuilder)
        }

        if (idx < tokens.size && isSpace(idx))
            idx = skipSpacesToRight(idx)

        if (idx == tokens.size || idx < tokens.size && tokens[idx].type != LCURL_TOKEN) {
            val klass = classBuilder.build()
            this.classes.add(klass)
            return tokenAfterClassIdx
        }

        // now parse class body { }
        // idx = index of LCURL

        classBuilder.addOverriddenMethod(parseOverriddenMethod(idx + 1))

        if (isSpace(idx))
            idx = skipSpacesToRight(idx)

        classBuilder.addProperty(parseProperties(idx + 1))

        val klass = classBuilder.build()
        parseABC(idx + 1, klass)
        this.classes.add(klass)

        return tokenAfterClassIdx
    }


    /**
     * @param from is the index of the token after of LCURL of body class
     * @return count of overriden methods of this class
     */
    private fun parseOverriddenMethod(from: Int): Int {
        // indent: 0 is class level
        // indent > 0 are other declarations inside class
        // indent = 0 is outside class declaration

        var overriddenMethodsCount = 0
        var indent = 0
        var idx = from

        var foundOverride = false

        while (indent >= 0) {
            if (idx >= tokens.size)
                return overriddenMethodsCount

            when (tokens[idx].type) {
                LCURL_TOKEN, LINE_STR_EXPR_START_TOKEN -> indent++
                RCURL_TOKEN -> indent--

                OVERRIDE_TOKEN -> {
                    if (indent == 0) {
                        foundOverride = true
                    }
                }
                FUN_TOKEN -> if (foundOverride) {
                    overriddenMethodsCount++
                    foundOverride = false
                }
                VAL_TOKEN, VAR_TOKEN -> foundOverride = false
            }
            idx++
        }
        return overriddenMethodsCount
    }

    /**
     * @param from is the index of the token after of LCURL of body class
     * @return count of properties of this class
     */
    private fun parseProperties(from: Int): Int {
        // indent: 0 is class level
        // indent > 0 are other declarations inside class
        // indent < 0 is outside class declaration

        var propertiesCount = 0

        var indent = 0
        var idx = from

        while (indent >= 0) {
            if (idx >= tokens.size)
                return propertiesCount
            when (tokens[idx].type) {
                LCURL_TOKEN, LINE_STR_EXPR_START_TOKEN -> indent++
                RCURL_TOKEN -> indent--

                VAR_TOKEN, VAL_TOKEN -> {
                    if (indent == 0) {
                        propertiesCount++
                    }
                }
            }
            idx++


        }
        return propertiesCount
    }

    /**
     * @param from is index of LPAREN
     * @return count of properties inside constructor
     */
    private fun parsePropertiesInsideConstructor(from: Int): Int {
        var propertiesCount = 0
        var idx = from

        while (tokens[idx].type != RPAREN_TOKEN) {
            when (tokens[idx].type) {
                VAR_TOKEN, VAL_TOKEN -> propertiesCount++
            }
            idx++
        }

        return propertiesCount
    }


    private fun skipTypeModifierIfNecessary(idx: Int): Int {
        var mutIdx = idx
        if (isSpace(idx))
            mutIdx = skipSpacesToRight(idx)

        if (tokens[mutIdx].type == LANGLE_TOKEN) {
            while (tokens[mutIdx].type != RANGLE_TOKEN)
                mutIdx++

            mutIdx++
        }
        return mutIdx
    }

    private fun skipAnnotationsIfNecessary(idx: Int): Int {
        var mutIdx = idx
        if (isSpace(idx))
            mutIdx = skipSpacesToRight(idx)

        while (tokens[mutIdx].type == AT_TOKEN) {
            mutIdx = skipAnnotation(mutIdx)
            mutIdx = skipSpacesToRight(mutIdx)
        }

        return mutIdx
    }

    /**
     * @return the next token after identifier in annotation
     */
    private fun skipAnnotation(idx: Int): Int {
        var mutIdx = idx
        if (isSpace(idx))
            mutIdx = skipSpacesToRight(idx)

        // let order is following: AT -> Identifier
        if (tokens[mutIdx].type == AT_TOKEN) {
            mutIdx += 2

            if (isSpace(mutIdx))
                mutIdx = skipSpacesToRight(mutIdx)

            if (tokens[mutIdx].type == LPAREN_TOKEN) {
                while (tokens[mutIdx].type != RPAREN_TOKEN)
                    mutIdx++

                mutIdx++
            }
        }


        // if annotation not found, return current index
        return mutIdx
    }

    private fun skipSimpleConstructorIfNecessary(from: Int): Int {
        // kind: (val x: Any, var y: Any...)
        var mutIdx = from
        if (mutIdx < tokens.size && isSpace(mutIdx))
            mutIdx = skipSpacesToRight(mutIdx)

        if (mutIdx < tokens.size && tokens[mutIdx].type == LPAREN_TOKEN) {
            while (tokens[mutIdx].type != RPAREN_TOKEN)
                mutIdx++

            mutIdx++
        }
        return mutIdx
    }


    private fun skipConstructorPartBeforeArgumentsIfNecessary(idx: Int): Int {
        var mutIdx = idx

        mutIdx = skipAnnotationsIfNecessary(mutIdx)

        if (isSpace(mutIdx))
            mutIdx = skipSpacesToRight(mutIdx)

        if (tokens[mutIdx].type == PRIVATE_TOKEN ||
            tokens[mutIdx].type == INTERNAL_TOKEN ||
            tokens[mutIdx].type == PROTECTED_TOKEN ||
            tokens[mutIdx].type == PUBLIC_TOKEN) {
            if (isSpace(mutIdx + 1))
                mutIdx = skipSpacesToRight(mutIdx + 1)
        }

        if (tokens[mutIdx].type == CONSTRUCTOR_TOKEN) {
            mutIdx = skipSpacesToRight(mutIdx + 1)
        }

        return mutIdx
    }

    /** right -> left
     * @return idx of the first non WS and non NL token
     */
    private fun skipSpacesToLeft(idx: Int): Int {
        var mutIdx = idx
        while (mutIdx >= 0 &&
            (tokens[mutIdx].type == WS_TOKEN || tokens[mutIdx].type == NL_TOKEN || tokens[mutIdx].type == SEMICOLON_TOKEN ||
                    tokens[mutIdx].type == INSIDE_WS_TOKEN || tokens[mutIdx].type == INSIDE_NL_TOKEN ||
                    tokens[mutIdx].type == DELIMITED_COMMENT_TOKEN ||
                    tokens[mutIdx].type == LINE_COMMENT_TOKEN)
        )
            mutIdx--

        return mutIdx
    }

    /** left -> right
     * @return idx of the first non WS and non NL token
     */
    private fun skipSpacesToRight(idx: Int): Int {
        var mutIdx = idx
        while (mutIdx < tokens.size &&
            (tokens[mutIdx].type == WS_TOKEN || tokens[mutIdx].type == NL_TOKEN || tokens[mutIdx].type == SEMICOLON_TOKEN ||
                    tokens[mutIdx].type == INSIDE_WS_TOKEN || tokens[mutIdx].type == INSIDE_NL_TOKEN ||
                    tokens[mutIdx].type == DELIMITED_COMMENT_TOKEN || tokens[mutIdx].type == LINE_COMMENT_TOKEN)
        ) {
            mutIdx++
        }

        return mutIdx
    }

    private fun isSpace(idx: Int): Boolean = tokens[idx].type == WS_TOKEN || tokens[idx].type == NL_TOKEN ||
            tokens[idx].type == INSIDE_WS_TOKEN || tokens[idx].type == INSIDE_NL_TOKEN ||
            tokens[idx].type == SEMICOLON_TOKEN || tokens[idx].type == DELIMITED_COMMENT_TOKEN ||
            tokens[idx].type == LINE_COMMENT_TOKEN


    /**
     * @return Pair <userType, next token index>
     */
    private fun getUserType(from: Int): Pair<String, Int> {

        val joiner = StringJoiner("")

        var idx = from
        if (isSpace(idx))
            idx = skipSpacesToRight(idx)

        joiner.add(tokens[idx].text)
        idx++

        if (idx < tokens.size && isSpace(idx))
            idx = skipSpacesToRight(idx)

        while (idx < tokens.size && tokens[idx].type == DOT_TOKEN) {
            joiner.add(".")
            idx++

            if (isSpace(idx))
                idx = skipSpacesToRight(idx)

            joiner.add(tokens[idx].text)
            idx++

            if (idx < tokens.size && isSpace(idx))
                idx = skipSpacesToRight(idx)
        }

        return joiner.toString() to idx
    }

    private fun parsePackage() {
        var idx = 0
        if (isSpace(idx))
            idx = skipSpacesToRight(idx)

        val packageBuilder = Package.Builder()

        if (tokens[idx].type != PACKAGE_TOKEN) {
            this.pack = Package.defaultPackage()
            return
        }

        idx = skipSpacesToRight(idx + 1)

        packageBuilder.addDomainLevel(tokens[idx].text)

        idx++
        if (isSpace(idx))
            idx = skipSpacesToRight(idx)

        while (tokens[idx].type == DOT_TOKEN) {
            idx++
            if (isSpace(idx))
                idx = skipSpacesToRight(idx)

            packageBuilder.addDomainLevel(tokens[idx].text)
            idx++

            if (isSpace(idx))
                idx = skipSpacesToRight(idx)
        }

        this.pack = packageBuilder.build()
    }

    private fun parseImports() {
        var idx = 0

        while (idx < tokens.size && tokens[idx].type != IMPORT_TOKEN)
            idx++

        // found IMPORT_TOKEN
        if (idx < tokens.size) {

            do {

                idx = skipSpacesToRight(idx + 1)

                val what = getUserType(idx)
                idx = what.second

                val domain = what.first.split(".")
                val import: Import
//             import all
                if (what.first.last() == '*') {
                    import = Import.importAll(Package(*domain.subList(0, domain.size - 1).toTypedArray()))
                } else {
                    idx = skipSpacesToRight(idx)
                    // import x as y
                    if (tokens[idx].type == AS_TOKEN) {
                        idx = skipSpacesToRight(idx + 1)

                        val asWhat = getUserType(idx)
                        idx = asWhat.second

                        import = Import.import(
                            Package(*domain.subList(0, domain.size - 1).toTypedArray()),
                            domain.last(),
                            asWhat.first
                        )
                    } else {
                        // import x
                        import =
                            Import.import(Package(*domain.subList(0, domain.size - 1).toTypedArray()), domain.last())
                    }
                }

                imports.add(import)
                idx = skipSpacesToRight(idx)
            } while (tokens[idx].type == IMPORT_TOKEN)
        }
    }

    private fun parseABC(from: Int, kotlinClass: KotlinClass) {
        var indent = 0
        var idx = from

        while (idx < tokens.size && indent >= 0) {
            when (tokens[idx].type) {

                LCURL_TOKEN, LINE_STR_EXPR_START_TOKEN -> indent++
                RCURL_TOKEN -> indent--

                MULT_ASSIGNMENT_TOKEN,
                ADD_ASSIGNMENT_TOKEN,
                SUB_ASSIGNMENT_TOKEN,
                DIV_ASSIGNMENT_TOKEN,
                MOD_ASSIGNMENT_TOKEN,
                ASSIGNMENT_TOKEN,
                INCR_TOKEN,
                DECR_TOKEN,
                -> {
                    kotlinClass.abc.foundAssignment()
                }

                LPAREN_TOKEN -> {
                    if (tokens[skipSpacesToLeft(idx - 1)].type == IDENTIFIER_TOKEN)
                        kotlinClass.abc.foundBranch()
                }

                EXCL_EQ_TOKEN,
                LANGLE_TOKEN,
                RANGLE_TOKEN,
                LE_TOKEN,
                GE_TOKEN,
                EQEQ_TOKEN, 
                IF_TOKEN,
                ELSE_TOKEN,
                TRY_TOKEN,
                CATCH_TOKEN,
                EXCL_NO_WS_TOKEN
                -> {
                    kotlinClass.abc.foundCondition()
                }
            }
            idx++
        }
    }
}