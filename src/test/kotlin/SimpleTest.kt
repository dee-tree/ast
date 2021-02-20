import ast.ClassesTreesBuilder
import ast.TreesMetrics
import org.jetbrains.kotlin.spec.grammar.tools.tokenizeKotlinCode
import org.junit.jupiter.api.Test
import parser.Parser
import kotlin.test.assertEquals

class SimpleTest {

    @Test
    fun test1() {

        val kotlinCode = """
            public open class A(val a: Any);
            
            private class B1 : A(0)
            private final class B2:A(0)
            
            open class C(var a: Any): A(a);
            
            class D : C(0) {
            fun abc() {}
            }
        """.trimIndent()

        val metrics = TreesMetrics(ClassesTreesBuilder(Parser(tokenizeKotlinCode(kotlinCode)).parseClasses()).build())

        assertEquals(2, metrics.maxInheritanceDepth)
        assertEquals(2, metrics.meanInheritanceDepth)
        assertEquals(0, metrics.meanPropertiesNum)
        assertEquals(0, metrics.meanOverriddenMethodNum)
    }

    @Test
    fun test2() {

        val kotlinCode = """
            
            // comment hello
            package a.b.c.package
            
            public open class A(val a: Any);
            
            private class B1 : A(0)
            private final class B2:A(0)
            
            open class C(var a: Any): A(a);
            
            class D : C(0) {
            fun abc() {}
            }
        """.trimIndent()

        val metrics = TreesMetrics(ClassesTreesBuilder(Parser(tokenizeKotlinCode(kotlinCode)).parseClasses()).build())

        assertEquals(2, metrics.maxInheritanceDepth)
        assertEquals(2, metrics.meanInheritanceDepth)
        assertEquals(0, metrics.meanPropertiesNum)
        assertEquals(0, metrics.meanOverriddenMethodNum)
    }
}