package parser

data class KotlinClass(
    val name: String,
    val visibilityMode: VisibilityModifiers,
    val inheritanceMode: InheritanceModifiers,
    val methods: Collection<KotlinMethod>
) {
}

enum class VisibilityModifiers {
    PRIVATE, PROTECTED, INTERNAL, PUBLIC
}

enum class InheritanceModifiers {
    FINAL, OPEN
}