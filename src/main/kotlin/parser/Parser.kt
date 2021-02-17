package parser

import org.jetbrains.kotlin.spec.grammar.tools.KotlinTokensList
import Package
import java.util.*

const val PACKAGE_TOKEN = "PACKAGE"
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
const val COLON_TOKEN = "COLON"
const val DOT_TOKEN = "DOT"
const val COMMA_TOKEN = "COMMA"
const val SEMICOLON_TOKEN = "SEMICOLON"

const val LCURL_TOKEN = "LCURL"
const val RCURL_TOKEN = "RCURL"
const val LPAREN_TOKEN = "LPAREN"
const val RPAREN_TOKEN = "RPAREN"
const val LANGLE_TOKEN = "LANGLE"
const val RANGLE_TOKEN = "RANGLE"

const val VAR_TOKEN = "VAR"
const val VAL_TOKEN = "VAL"

const val AT_TOKEN = "AT_PRE_WS"

class Parser(private val tokens: KotlinTokensList) {

    private lateinit var pack: Package

    var classes = mutableListOf<KotlinClass>()
    private set

    fun parseClasses(): Collection<KotlinClass> {

        var idx = parsePackage(0)

        idx = parseNextClass(idx)
        while (idx != tokens.size) {
            idx = parseNextClass(idx)
        }
        return classes
    }


    /**
     * @return the index of the next token after "class" keywords or token.size in case of lack of class
     */
    private fun parseNextClass(from: Int): Int {
        var idx = from
        while (idx < tokens.size && tokens[idx].type != CLASS_TOKEN)
            idx++

        // found class declaration
        if (idx >= tokens.size)
            return tokens.size  // class not found

        val tokenAfterClassIdx = idx + 1

        var leftIdx = skipSpacesToLeft(idx - 1)
        idx = skipSpacesToRight(idx + 1)


        // before "class" may be 3 word
        val classBuilder = KotlinClass.Builder(tokens[idx].text)
        classBuilder.pack(this.pack)

        // check 3 words before "class" keyword
        for (x in 1..3) {
            if (leftIdx < 0)
                break

            when (tokens[leftIdx].type) {
                FINAL_TOKEN -> classBuilder.inheritanceMode(InheritanceModifiers.FINAL)
                OPEN_TOKEN -> classBuilder.inheritanceMode(InheritanceModifiers.OPEN)
                ABSTRACT_TOKEN -> classBuilder.inheritanceMode(InheritanceModifiers.ABSTRACT)

                PRIVATE_TOKEN -> classBuilder.visibilityMode(VisibilityModifiers.PRIVATE)
                PROTECTED_TOKEN -> classBuilder.visibilityMode(VisibilityModifiers.PROTECTED)
                INTERNAL_TOKEN -> classBuilder.visibilityMode(VisibilityModifiers.INTERNAL)
                PUBLIC_TOKEN -> classBuilder.visibilityMode(VisibilityModifiers.PUBLIC)
            }

            if (leftIdx == 0)
                break
            leftIdx = skipSpacesToLeft(leftIdx - 1)
        }

        idx++

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

        // is there inheritance ?
        if (idx < tokens.size && tokens[idx].type == COLON_TOKEN) {

            idx = skipAnnotationsIfNecessary(idx + 1)

            var userTypePair = getUserType(idx)
            idx = userTypePair.second

            if (idx == tokens.size) {
                classes.add(classBuilder.build())
                return tokenAfterClassIdx
            }

            if (isSpace(idx))
                idx = skipSpacesToRight(idx)

            if (idx < tokens.size && tokens[idx].type == LPAREN_TOKEN) {
                classBuilder.superClass(userTypePair.first)
                while (tokens[idx].type != RPAREN_TOKEN)
                    idx++
                idx++
            }

            if (idx < tokens.size && isSpace(idx))
                idx = skipSpacesToRight(idx)

            while (idx < tokens.size && tokens[idx].type == COMMA_TOKEN) {
                idx++

                if (isSpace(idx))
                    idx = skipSpacesToRight(idx)

                idx = skipAnnotationsIfNecessary(idx)

                userTypePair = getUserType(idx)
                idx = userTypePair.second

                if (idx == tokens.size) {
                    classes.add(classBuilder.build())
                    return tokenAfterClassIdx
                }

                if (isSpace(idx))
                    idx = skipSpacesToRight(idx)

                if (idx < tokens.size && tokens[idx].type == LPAREN_TOKEN) {
                    classBuilder.superClass(userTypePair.first)
                    while (tokens[idx].type != RPAREN_TOKEN)
                        idx++
                    idx++
                }
            }

        }

        if (idx < tokens.size && isSpace(idx))
            idx = skipSpacesToRight(idx)

        if (idx == tokens.size || idx < tokens.size && tokens[idx].type != LCURL_TOKEN) {
            this.classes.add(classBuilder.build())
            return tokenAfterClassIdx
        }

        // now parse class body { }
        // idx = index of LCURL

        classBuilder.addOverriddenMethod(parseOverriddenMethod(idx + 1))

        if (isSpace(idx))
            idx = skipSpacesToRight(idx)

        classBuilder.addProperty(parseProperties(idx + 1))

        this.classes.add(classBuilder.build())

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
                LCURL_TOKEN -> indent++
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
                LCURL_TOKEN -> indent++
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
            tokens[mutIdx].type == WS_TOKEN || tokens[mutIdx].type == NL_TOKEN || tokens[mutIdx].type == SEMICOLON_TOKEN ||
            tokens[mutIdx].type == INSIDE_WS_TOKEN || tokens[mutIdx].type == INSIDE_NL_TOKEN
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
            tokens[mutIdx].type == WS_TOKEN || tokens[mutIdx].type == NL_TOKEN || tokens[mutIdx].type == SEMICOLON_TOKEN ||
            tokens[mutIdx].type == INSIDE_WS_TOKEN || tokens[mutIdx].type == INSIDE_NL_TOKEN
        )
            mutIdx++

        return mutIdx
    }

    private fun isSpace(idx: Int): Boolean = tokens[idx].type == WS_TOKEN || tokens[idx].type == NL_TOKEN ||
            tokens[idx].type == INSIDE_WS_TOKEN || tokens[idx].type == INSIDE_NL_TOKEN || tokens[idx].type == SEMICOLON_TOKEN


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

    /**
     * @return idx of the next token after package declaration
     */
    private fun parsePackage(from: Int): Int {
        var idx = from
        if (isSpace(idx))
            idx = skipSpacesToRight(idx)

        val packageBuilder = Package.Builder()

        if (tokens[idx].type != PACKAGE_TOKEN) {
            this.pack = Package.defaultPackage()
            return idx
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
        return idx
    }
}