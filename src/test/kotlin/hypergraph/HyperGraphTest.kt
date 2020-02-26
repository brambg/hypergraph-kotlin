package hypergraph

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class HyperGraphTest {

    @Test
    fun test() {
        val tokens = listOf("John", "loves", "Mary")
        val hg = mutableHyperGraphOf(HyperEdge("S", listOf("1"), listOf("2")))

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

        val stateMachine = StateMachine(hg, rules)

        println(stateMachine.hyperGraph)
        heReplace(stateMachine, "S")
        heReplace(stateMachine, "JOHN")
        heReplace(stateMachine, "LOVES")
        heReplace(stateMachine, "MARY")
        assertThat(stateMachine.isInValidEndState()).isTrue()
    }
}