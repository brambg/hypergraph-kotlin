package hypergraph

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class HyperGraphTest {

    @Test
    fun testStateMachine() {
        val tokens = listOf("John", "loves", "Mary")
        val startState = HyperEdge("S", listOf("1"), listOf("2"))

        val rules = mapOf(
                "S" to hyperGraphOf(
                        HyperEdge("JOHN", listOf("_"), listOf("_"))),
                "JOHN" to hyperGraphOf(
                        HyperEdge("John", listOf("_"), listOf("3")),
                        HyperEdge("LOVES", listOf("3"), listOf("_"))),
                "LOVES" to hyperGraphOf(
                        HyperEdge("loves", listOf("_"), listOf("4")),
                        HyperEdge("MARY", listOf("4"), listOf("_"))),
                "MARY" to hyperGraphOf(
                        HyperEdge("Mary", listOf("_"), listOf("_")))
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
}