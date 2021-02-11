package lexing

data class Token(val type: TokenTypes, val value: String? = null) {
}

enum class TokenTypes {
    IDENTIFIER,
    ABSTRACT,
    FINAL, OPEN, OVERRIDE,
    PRIVATE, PROTECTED, INTERNAL, PUBLIC,
    CLASS,
    SEMICOLON,
    LPAREN, RPAREN, // ( )
    LCURL, RCURL, // { }
    FUN
}
