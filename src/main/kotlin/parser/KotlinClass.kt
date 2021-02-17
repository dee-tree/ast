package parser

import ABCMetrics
import Package

data class
KotlinClass(
    val name: String,
    val visibilityMode: VisibilityModifiers,
    val inheritanceMode: InheritanceModifiers,
    val pack: Package,
    val superClass: String?,
    val propertiesCount: Int,
    val overridenMethodsCount: Int
) {

    var abc: ABCMetrics = ABCMetrics()
    private set

    data class Builder(
        private var className: String,
        private var visibilityMode: VisibilityModifiers = VisibilityModifiers.PUBLIC,
        private var inheritanceMode: InheritanceModifiers = InheritanceModifiers.FINAL,
        private var superClass: String? = null,
        private var pack: Package = Package.defaultPackage(),
        private var propertiesCount: Int = 0,
        private var overriddenMethodsCount: Int = 0

        ) {
        fun visibilityMode(visibility: VisibilityModifiers) = apply { this.visibilityMode = visibility }
        fun inheritanceMode(inheritance: InheritanceModifiers) = apply { this.inheritanceMode = inheritance }
        fun pack(pack: Package) = apply { this.pack = pack }
        fun superClass(superClass: String) = apply { this.superClass = superClass }
        fun addProperty(count: Int = 1) = apply { this.propertiesCount += count }
        fun addOverriddenMethod(count: Int = 1) = apply { this.overriddenMethodsCount += count }
        fun build() = KotlinClass(className, visibilityMode, inheritanceMode, pack, superClass, propertiesCount, overriddenMethodsCount)
    }
}

enum class VisibilityModifiers {
    PRIVATE, PROTECTED, INTERNAL, PUBLIC
}

enum class InheritanceModifiers {
    FINAL, OPEN, ABSTRACT
}