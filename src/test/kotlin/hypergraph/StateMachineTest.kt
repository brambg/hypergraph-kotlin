package hypergraph

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class StateMachineTest {

    @Test
    fun testStateMachine() {
        val tokens = listOf("John", "loves", "Mary")
        val startState = HyperEdge(listOf("1"), NonTerminal("S"), listOf("2"))

        val rules = mapOf(
                "S" to hyperGraphOf(
                        HyperEdge(listOf("_"), NonTerminal("OBJECT"), listOf("_"))),
                "OBJECT" to hyperGraphOf(
                        HyperEdge(listOf("_"), Terminal("John"), listOf("3")),
                        HyperEdge(listOf("3"), NonTerminal("VERB"), listOf("_"))),
                "VERB" to hyperGraphOf(
                        HyperEdge(listOf("_"), Terminal("loves"), listOf("4")),
                        HyperEdge(listOf("4"), NonTerminal("SUBJECT"), listOf("_"))),
                "SUBJECT" to hyperGraphOf(
                        HyperEdge(listOf("_"), Terminal("Mary"), listOf("_")))
        )

        val stateMachine = StateMachine(rules, startState)

        println(stateMachine.hyperGraph)
        stateMachine.apply(tokens)
        assertThat(stateMachine.hasValidEndState()).isTrue()

        stateMachine.reset()
        val badTokens = listOf("Cookiemonster", "eats", "stroopwafels")
        try {
            stateMachine.apply(badTokens)
        } catch (ex: Exception) {
            assertThat(ex.message == "No rule found that matches token 'Cookiemonster'")
        }
        assertThat(stateMachine.hasValidEndState()).isFalse()
    }

    @Test
    fun testTRD509_1() {
//        val tagml = "[tag>text<tag]"
        val tokens = listOf("[tag>", "text", "<tag]")
        val startState = HyperEdge(listOf("1"), NonTerminal("S"), listOf("2"))
        println(startState)

        val rules = mapOf(
                "S" to hyperGraphOf(
                        HyperEdge(listOf("_"), NonTerminal("[TAG>"), listOf("_"))),
                "[TAG>" to hyperGraphOf(
                        HyperEdge(listOf("_"), NonTerminal("<TAG]"), listOf("3")),
                        HyperEdge(listOf("3"), NonTerminal("TEXT"), listOf("_"))),
                "TEXT" to hyperGraphOf(
                        HyperEdge(listOf("_"), Terminal("text"), listOf("4"))),
                "<TAG]" to hyperGraphOf(
                        HyperEdge(listOf("_"), Terminal("tag"), listOf("_")))
        )

        val stateMachine = StateMachine(rules, startState)

        println(stateMachine.hyperGraph)
        stateMachine.apply(tokens)
        assertThat(stateMachine.hasValidEndState()).isTrue()
    }
}