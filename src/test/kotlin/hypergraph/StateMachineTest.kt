package hypergraph

import junit.framework.Assert.fail
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class StateMachineTest {

    @Test
    fun test_StateMachine_with_valid_input() {
        val stateMachine = make_jlm_StateMachine()
        val tokens = listOf("John", "loves", "Mary")
        stateMachine.apply(tokens)
        printHyperGraph(stateMachine)
        assertThat(stateMachine.hasValidEndState()).isTrue()
    }

    @Test
    fun test_StateMachine_with_invalid_input() {
        val stateMachine = make_jlm_StateMachine()
        val badTokens = listOf("Cookiemonster", "eats", "stroopwafels")
        try {
            stateMachine.apply(badTokens)
            printHyperGraph(stateMachine)
        } catch (ex: Exception) {
            assertThat(ex.message == "No rule found that matches token 'Cookiemonster'")
        }
        assertThat(stateMachine.hasValidEndState()).isFalse()
    }

    private fun make_jlm_StateMachine(): StateMachine<String, String> {
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

        return StateMachine(rules, startState)
    }

    @Test
    fun test_TRD509_1_with_valid_input() {
        val stateMachine = make_TRD509_1_StateMachine()

//        val tagml = "[tag>text<tag]"
        val tokens = listOf(
                OpenMarkupToken("tag"),
                TextToken("text"),
                CloseMarkupToken("tag")
        )
        stateMachine.apply(tokens)
        assertThat(stateMachine.hasValidEndState()).isTrue()
        printHyperGraph(stateMachine)
    }

    @Test
    fun test_TRD509_1_with_invalid_input() {
        val stateMachine = make_TRD509_1_StateMachine()

        val badTokens = listOf(
                OpenMarkupToken("tag"),
                TextToken("text"),
                CloseMarkupToken("someothertagname")
        )

        try {
            stateMachine.apply(badTokens)
            printHyperGraph(stateMachine)
            fail()
        } catch (ex: Exception) {
            assertThat(ex.message).isEqualTo("Unexpected token: '<someothertagname]', expected '<tag]'")
        }
        assertThat(stateMachine.hasValidEndState()).isFalse()
    }

    private fun make_TRD509_1_StateMachine(): StateMachine<String, Token> {
        val startState = HyperEdge(listOf("1"), NonTerminal("S"), listOf("2"))

        val rules = mapOf(
                "S" to hyperGraphOf(
                        HyperEdge(listOf("_"), MarkupNonTerminal("M"), listOf("_"))),
                "M" to hyperGraphOf(
                        HyperEdge(listOf("_"), OpenMarkupNonTerminal("OM"), listOf("3")),
                        HyperEdge(listOf("3"), TextNonTerminal("TEXT"), listOf("_"))),
                // the OpenMarkupNonTerminal is a TemplateLabel, which means it should add a rule based on the token
                "TEXT" to hyperGraphOf(
                        HyperEdge(listOf("_"), TextTerminal(), listOf("_"))),
                "OM" to hyperGraphOf(
                        HyperEdge(listOf("_"), MarkupTerminal(), listOf("_")))
        )

        return StateMachine(rules, startState)
    }

    private fun <T> printHyperGraph(stateMachine: StateMachine<String, T>) {
        val toString = stateMachine.hyperGraph
                .sortedBy { it.source.toString() }
                .joinToString("\n  ")
        println("\nhypergraph edges:\n  $toString\n")
    }
}