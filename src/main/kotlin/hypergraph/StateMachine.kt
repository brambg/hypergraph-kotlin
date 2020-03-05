package hypergraph

import java.util.concurrent.atomic.AtomicLong

class StateMachine<N, T>(private val rules: Map<String, HyperGraph<N>>, private val startState: HyperEdge<N>) {
    val hyperGraph = mutableHyperGraphOf(startState)
    val nodeCounter = AtomicLong(1);

    private var nonTerminalsInGraph = listOf(startState.label as NonTerminalEdgeLabel)

    fun apply(tokens: List<T>) {
        for (token in tokens) {
            println("nonTerminals: $nonTerminalsInGraph")
            println("token: $token")
            val nonTerminalsWithMatchingRuleRHSs = nonTerminalsInGraph
                    .map { it to rules[it.name] }
                    .filter {
                        it.second != null && it.second!!.any { rule ->
                            rule.label is RuleEdgeLabel &&
                            rule.label.tokenMatchPredicate(token as Token, it.first)
                        }
                    }
            when (val size = nonTerminalsWithMatchingRuleRHSs.size) {
                0    -> error("Unexpected token: '$token'")
                1    -> replaceHyperEdge(
                        nonTerminalsWithMatchingRuleRHSs[0].first,
                        nonTerminalsWithMatchingRuleRHSs[0].second!!,
                        token as Token
                )
                else -> error("$size rules were found, that should not be possible!")
            }
            println("result: ${hyperGraph.sortedBy(HyperEdge<N>::toString).joinToString("\n        ")}\n")
        }
    }

    fun hasValidEndState(): Boolean = hyperGraph.none { it.label is NonTerminalEdgeLabel }

    fun reset() {
        hyperGraph.clear()
        hyperGraph.add(startState)
    }

    private fun replaceHyperEdge(hyperEdgeLabel: NonTerminalEdgeLabel, hyperGraphToReplaceWith: HyperGraph<N>, token: Token) {
        val hyperEdgeToReplace = hyperGraph.findHyperEdgeByLabel(hyperEdgeLabel)

        val iterSourceExternalNodes = hyperEdgeToReplace.source.toList().iterator()
        val iterTargetExternalNodes = hyperEdgeToReplace.target.toList().iterator()

        val newHyperEdges = hyperGraphToReplaceWith.map { edge ->
            val newSourceNodes = edge.source.map {
                if (it == "_") iterSourceExternalNodes.next()
                else it // TODO: map nodeId from rule to nodeId from graph
            }

            val newEdgeLabel = when (edge.label) {
                is RuleEdgeLabel -> edge.label.edgeLabelMaker(token)
                else             -> edge.label
            }

            val newTargetNodes = edge.target.map {
                if (it == "_") iterTargetExternalNodes.next()
                else it // TODO: map nodeId from rule to nodeId from graph
            }

            HyperEdge(newSourceNodes, newEdgeLabel, newTargetNodes)
        }

        hyperGraph.deleteHyperEdgeInHyperGraph(hyperEdgeToReplace)
        hyperGraph.addAll(newHyperEdges)
        nonTerminalsInGraph = hyperGraph
                .filter { it.label is NonTerminalEdgeLabel }
                .map { it.label as NonTerminalEdgeLabel }
    }

    private fun <N> MutableHyperGraph<N>.deleteHyperEdgeInHyperGraph(hyperEdgeToReplace: HyperEdge<N>) {
        val index = this.indexOfFirst { it == hyperEdgeToReplace }
        this.removeAt(index)
    }

    private fun <N> HyperGraph<N>.findHyperEdgeByLabel(label: NonTerminalEdgeLabel): HyperEdge<N> {
        val edges = this.filter { it.label is NonTerminalEdgeLabel && it.label == label }
        if (edges.size == 1) {
            return edges[0]
        } else {
            error("${edges.size} edges found with label $label, this is a TODO!?")
        }
    }
}

/// rules : edgeId -> listOf(HyperEdge)
// rules document the he replacement rules, so the lhs should map to he in the hg, and the rhs should map to a
// replacementhg
// problem: we need parameterized rules -> the edges in the rhs of the rules should map to he, using information from
// the token, and the he from the hg
// the he in the rhs, when nonterminal, have a variable to link to the lhs of some other rules
//