package hypergraph

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.util.concurrent.atomic.AtomicLong
import kotlin.test.fail

class StateMachineTest {

//    @Nested
//    inner class JLM {
//        @Test
//        fun test_StateMachine_with_valid_input() {
//            val stateMachine = make_jlm_StateMachine()
//            val tokens = listOf("John", "loves", "Mary")
//            stateMachine.apply(tokens)
//            printHyperGraph(stateMachine)
//            assertThat(stateMachine.hasValidEndState()).isTrue()
//        }
//
//        @Test
//        fun test_StateMachine_with_invalid_input() {
//            val stateMachine = make_jlm_StateMachine()
//            val badTokens = listOf("Cookiemonster", "eats", "stroopwafels")
//            try {
//                stateMachine.apply(badTokens)
//                printHyperGraph(stateMachine)
//            } catch (ex: Exception) {
//                assertThat(ex.message == "No rule found that matches token 'Cookiemonster'")
//            }
//            assertThat(stateMachine.hasValidEndState()).isFalse()
//        }
//
//        private fun make_jlm_StateMachine(): StateMachine<String, String> {
//            val startState = HyperEdge(listOf("1"), NonTerminal("S"), listOf("2"))
//
//            val rules = mapOf(
////                // (_)-[OBJECT]-(_)
////                "S" to hyperGraphOf(
////                        HyperEdge(listOf("_"), NonTerminal("OBJECT"), listOf("_"))),
//                    // (_)-<John>-(x), (x)-[VERB]-(_)
//                    "S" to hyperGraphOf(
//                            HyperEdge(listOf("_"), Terminal("John"), listOf("3")),
//                            HyperEdge(listOf("3"), NonTerminal("VERB"), listOf("_"))),
//                    // (_)-<loves>-(x), (x)-[SUBJECT]-(_)
//                    "VERB" to hyperGraphOf(
//                            HyperEdge(listOf("_"), Terminal("loves"), listOf("4")),
//                            HyperEdge(listOf("4"), NonTerminal("SUBJECT"), listOf("_"))),
//                    // (_)-<Mary>-(_)
//                    "SUBJECT" to hyperGraphOf(
//                            HyperEdge(listOf("_"), Terminal("Mary"), listOf("_")))
//            )
//
//            return StateMachine(rules, startState)
//        }
//    }

    @Nested
    inner class TRD509a {
        /* [tag>text<tag] */

        @Test
        fun test_TRD509_1_with_valid_input() {
            val stateMachine = make_TRD509_1_StateMachine()

            val tokens = listOf(
                    OpenMarkupToken("tag"),
                    TextToken("text"),
                    CloseMarkupToken("tag")
            )
            with(stateMachine) {
                apply(tokens)
                assertThat(hasValidEndState()).isTrue()
                assertThat(hyperGraph.map { it.toString() })
                        .containsExactlyInAnyOrder(
                                "(1)--[[tag]]->(3)",
                                "(3)--[\"text\"]->(2)"
                        )
                printHyperGraph(this)
            }
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

        @Test
        fun test_TRD509_1_with_invalid_input_order() {
            val stateMachine = make_TRD509_1_StateMachine()

            val badTokens = listOf(
                    OpenMarkupToken("tag"),
                    CloseMarkupToken("tag"),
                    TextToken("text")
            )

            try {
                stateMachine.apply(badTokens)
                printHyperGraph(stateMachine)
                fail()
            } catch (ex: Exception) {
                assertThat(ex.message).isEqualTo("Unexpected token: '<tag]', expected text")
            }
            assertThat(stateMachine.hasValidEndState()).isFalse()
        }

        private fun make_TRD509_1_StateMachine(): StateMachine {
            val startState = HyperEdge(listOf("a"), NonTerminal("S"), listOf("b"))

            val rules = mapOf(
                    // (_)-[OM(t)]-(x) (x)-[TEXT]-(_)
                    "S" to hyperGraphOf(
                            HyperEdge(
                                    listOf("_"),
                                    openMarkupNonTerminalRuleEdgeLabel("OM"),
                                    listOf("x")
                            ),
                            HyperEdge(
                                    listOf("x"),
                                    NonTerminal("TEXT"),
                                    listOf("_")
                            )),
                    // (_)-<text(*)>-(_)
                    "TEXT" to hyperGraphOf(
                            HyperEdge(
                                    listOf("_"),
                                    textTerminalRuleEdgeLabel(),
                                    listOf("_")
                            )),

                    // OM(t) -> (_)-<Markup(t)>-(_)
                    "OM" to hyperGraphOf(
                            HyperEdge(
                                    listOf("_"),
                                    markupTerminalRuleEdgeLabel(),
                                    listOf("_")
                            ))
            )

            return StateMachine(rules, startState)
        }
    }

    private fun markupTerminalRuleEdgeLabel(): RuleEdgeLabel {
        return RuleEdgeLabel(
                { token, edgeLabel ->
                    token is CloseMarkupToken && edgeLabel is
                            OpenMarkupNonTerminal && edgeLabel.tagName == token.tagName
                },
                { token -> MarkupTerminal((token as MarkupToken).tagName) }
        )
    }

    private fun textTerminalRuleEdgeLabel(): RuleEdgeLabel {
        return RuleEdgeLabel(
                { token, _ -> token is TextToken },
                { token -> TextTerminal((token as TextToken).content) }
        )
    }

    private fun openMarkupNonTerminalRuleEdgeLabel(nonTerminal: String): RuleEdgeLabel {
        return RuleEdgeLabel(
                { token, _ -> token is OpenMarkupToken },
                { token -> OpenMarkupNonTerminal(nonTerminal, (token as MarkupToken).tagName) }
        )
    }

    @Nested
    inner class TRD509b {
        /* [tag>[color>Green<color][food>Eggs<food][food>Ham<food]<tag] */

        @Test
        fun test_TRD509_2_with_valid_input() {
            val stateMachine = make_TRD509_2_StateMachine()

            val tokens = listOf(
                    OpenMarkupToken("tag"),
                    OpenMarkupToken("color"),
                    TextToken("Green"),
                    CloseMarkupToken("color"),
                    OpenMarkupToken("food"),
                    TextToken("Eggs"),
                    CloseMarkupToken("food"),
                    OpenMarkupToken("food"),
                    TextToken("Ham"),
                    CloseMarkupToken("food"),
                    CloseMarkupToken("tag")
            )
            stateMachine.apply(tokens)
            assertThat(stateMachine.hasValidEndState()).isTrue()
            printHyperGraph(stateMachine)
        }

        @Test
        fun test_TRD509_2_with_invalid_input() {
            val stateMachine = make_TRD509_2_StateMachine()

            val badTokens = listOf(
                    OpenMarkupToken("tag"),
                    OpenMarkupToken("color"),
                    TextToken("Green"),
                    CloseMarkupToken("tag")
            )
            stateMachine.apply(badTokens)
            try {
                stateMachine.apply(badTokens)
                printHyperGraph(stateMachine)
                fail()
            } catch (ex: Exception) {
                assertThat(ex.message).isEqualTo("Unexpected token: '<tag]', expected '<color]'")
            }
            assertThat(stateMachine.hasValidEndState()).isFalse()
        }

        private fun make_TRD509_2_StateMachine(): StateMachine {
            val startState = HyperEdge(listOf("a"), NonTerminal("S"), listOf("z"))

            val rules = mapOf(
                    "S" to hyperGraphOf(
                            HyperEdge(
                                    listOf("_"),
                                    openMarkupNonTerminalRuleEdgeLabel("OM"),
                                    listOf("x")
                            ),
                            HyperEdge(
                                    listOf("x"),
                                    NonTerminal("M"),
                                    listOf("_")
                            )),
                    "M" to hyperGraphOf(
                            HyperEdge(
                                    listOf("_"),
                                    openMarkupNonTerminalRuleEdgeLabel("OM"),
                                    listOf("x")
                            ),
                            HyperEdge(
                                    listOf("x"),
                                    NonTerminal("TEXT"),
                                    listOf("_")
                            )),
                    "TEXT" to hyperGraphOf(
                            HyperEdge(
                                    listOf("_"),
                                    textTerminalRuleEdgeLabel(),
                                    listOf("_")
                            )),

                    "OM" to hyperGraphOf(
                            HyperEdge(
                                    listOf("_"),
                                    markupTerminalRuleEdgeLabel(),
                                    listOf("_")
                            ))
            )

            return StateMachine(rules, startState)
        }
    }
//
//    @Nested
//    inner class TRD509c {
//        /* [tag>[color>Green<color] [food>Eggs<food] and [food>Ham<food]<tag] */
//
//        @Test
//        fun test_TRD509_3_with_valid_input() {
//            val stateMachine = make_TRD509_3_StateMachine()
//
//            val tokens = listOf(
//                    OpenMarkupToken("tag"),
//                    OpenMarkupToken("color"),
//                    TextToken("Green"),
//                    CloseMarkupToken("color"),
//                    TextToken(" "),
//                    OpenMarkupToken("food"),
//                    TextToken("Eggs"),
//                    CloseMarkupToken("food"),
//                    TextToken(" and "),
//                    OpenMarkupToken("food"),
//                    TextToken("Ham"),
//                    CloseMarkupToken("food"),
//                    CloseMarkupToken("tag")
//            )
//            stateMachine.apply(tokens)
//            assertThat(stateMachine.hasValidEndState()).isTrue()
//            printHyperGraph(stateMachine)
//        }
//
//        @Test
//        fun test_TRD509_3_with_invalid_input() {
//            val stateMachine = make_TRD509_3_StateMachine()
//
//            val badTokens = listOf(
//                    OpenMarkupToken("tag"),
//                    OpenMarkupToken("color"),
//                    TextToken("Green"),
//                    CloseMarkupToken("tag")
//            )
//            stateMachine.apply(badTokens)
//            try {
//                stateMachine.apply(badTokens)
//                printHyperGraph(stateMachine)
//                fail()
//            } catch (ex: Exception) {
//                assertThat(ex.message).isEqualTo("Unexpected token: '<tag]', expected '<color]'")
//            }
//            assertThat(stateMachine.hasValidEndState()).isFalse()
//        }
//
//        private fun make_TRD509_3_StateMachine(): StateMachine<String, Token> {
//            val startState = HyperEdge(listOf("1"), NonTerminal("S"), listOf("2"))
//
//            val rules = mapOf(
////               "S" to hyperGraphOf(
////                        HyperEdge(listOf("_"), OpenMarkupNonTerminal("OM", MarkupToken::tagName), listOf("3")),
////                        HyperEdge(listOf("3"), TextNonTerminal("TEXT"), listOf("_"))),
//                    "TEXT" to hyperGraphOf(
//                            HyperEdge(listOf("_"), TextTerminal(), listOf("_")))//,
////                "OM" to hyperGraphOf(
////                        HyperEdge(listOf("_"), MarkupTerminal(MarkupToken::tagName), listOf("_")))
//            )
//
//            return StateMachine(rules, startState)
//        }
//    }
//
//    @Nested
//    inner class TRD509d {
//        /* [tag|+A,+B>[a|A>Cookiemonster [b|B>likes<a] cookies<b]<tag] */
//        @Test
//        fun test_TRD509_4_with_valid_input() {
//            val stateMachine = make_TRD509_4_StateMachine()
//
//            val tokens = listOf(
//                    OpenMarkupToken("tag"/*, listOf("+A","+B")*/),
//                    OpenMarkupToken("a"/*, listOf("A")*/),
//                    TextToken("Cookiemonster "),
//                    OpenMarkupToken("b"/*, listOf("B")*/),
//                    TextToken("likes"),
//                    CloseMarkupToken("a"),
//                    TextToken(" cookies"),
//                    OpenMarkupToken("food"),
//                    CloseMarkupToken("b"),
//                    CloseMarkupToken("tag")
//            )
//            stateMachine.apply(tokens)
//            assertThat(stateMachine.hasValidEndState()).isTrue()
//            stateMachine.hasValidEndState() `should be equal to` true
//            printHyperGraph(stateMachine)
//        }
//
//        @Test
//        fun test_TRD509_4_with_invalid_input() {
//            val stateMachine = make_TRD509_4_StateMachine()
//
//            val badTokens = listOf(
//                    OpenMarkupToken("tag"),
//                    OpenMarkupToken("a"),
//                    TextToken("Green"),
//                    CloseMarkupToken("tag")
//            )
//            stateMachine.apply(badTokens)
//            try {
//                stateMachine.apply(badTokens)
//                printHyperGraph(stateMachine)
//                fail()
//            } catch (ex: Exception) {
//                assertThat(ex.message).isEqualTo("Unexpected token: '<tag]', expected '<a]'")
//            }
//            assertThat(stateMachine.hasValidEndState()).isFalse()
//        }
//
//        private fun make_TRD509_4_StateMachine(): StateMachine<String, Token> {
//            val startState = HyperEdge(listOf("1"), NonTerminal("S"), listOf("2"))
//
//            val rules = mapOf(
////                "S" to hyperGraphOf(
////                        HyperEdge(listOf("_"), OpenMarkupNonTerminal("OM", MarkupToken::tagName), listOf("3")),
////                        HyperEdge(listOf("3"), TextNonTerminal("TEXT"), listOf("_"))),
////                    "TEXT" to hyperGraphOf(
////                            HyperEdge(listOf("_"), TextTerminal(), listOf("_")))//,
////                "OM" to hyperGraphOf(
////                        HyperEdge(listOf("_"), MarkupTerminal(MarkupToken::tagName), listOf("_")))
//            )
//
//            return StateMachine(rules, startState)
//        }
//    }

    private fun printHyperGraph(stateMachine: StateMachine) {
        val toString = stateMachine.hyperGraph
                .sortedBy { it.source.toString() }
                .joinToString("\n  ")
        println("\nhypergraph edges:\n  $toString\n")
    }

}