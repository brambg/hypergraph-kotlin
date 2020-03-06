package hypergraph

import java.util.concurrent.atomic.AtomicLong

class StateMachine(private val rules: Map<String, HyperGraph<String>>, private val startState: HyperEdge<String>) {
    private val nodeCounter = AtomicLong(1)
    val hyperGraph = mutableHyperGraphOf(fixNodeLabelsInHE(startState, nodeCounter))

    private var nonTerminalsInGraph = listOf(startState.label as NonTerminalEdgeLabel)

    fun apply(tokens: List<Token>) {
        for (token in tokens) {
            println("nonTerminals: $nonTerminalsInGraph")
            println("token: $token")
            val nonTerminalsWithMatchingRuleRHSs = nonTerminalsInGraph
                    .map { it to rules[it.name] }
                    .filter {
                        it.second != null && it.second!!.any { rule ->
                            rule.label is RuleEdgeLabel &&
                                    rule.label.tokenMatchPredicate(token, it.first)
                        }
                    }
            when (val size = nonTerminalsWithMatchingRuleRHSs.size) {
                0 -> error("Unexpected token: '$token'")
                1 -> replaceHyperEdge(
                        nonTerminalsWithMatchingRuleRHSs[0].first,
                        nonTerminalsWithMatchingRuleRHSs[0].second!!,
                        token
                )
                else -> error("$size rules were found, that should not be possible!")
            }
            println("result: ${hyperGraph.sortedBy(HyperEdge<String>::toString).joinToString("\n        ")}\n")
        }
    }

    fun hasValidEndState(): Boolean = hyperGraph.none { it.label is NonTerminalEdgeLabel }

    fun reset() {
        hyperGraph.clear()
        hyperGraph.add(startState)
    }

    private fun replaceHyperEdge(hyperEdgeLabel: NonTerminalEdgeLabel, hyperGraphToReplaceWith: HyperGraph<String>, token: Token) {
        val hyperEdgeToReplace = hyperGraph.findHyperEdgeByLabel(hyperEdgeLabel)

        val iterSourceExternalNodes = hyperEdgeToReplace.source.toList().iterator()
        val iterTargetExternalNodes = hyperEdgeToReplace.target.toList().iterator()

        val nodeLabelMap = mutableMapOf<String, String>()
        val newHyperEdges = hyperGraphToReplaceWith.map { edge ->
            val newSourceNodes = edge.source.map {
                if (it == "_") iterSourceExternalNodes.next()
                else fixNodeLabel(it, nodeCounter, nodeLabelMap)
            }

            val newEdgeLabel = when (edge.label) {
                is RuleEdgeLabel -> edge.label.edgeLabelMaker(token)
                else -> edge.label
            }

            val newTargetNodes = edge.target.map {
                if (it == "_") iterTargetExternalNodes.next()
                else fixNodeLabel(it, nodeCounter, nodeLabelMap)
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

    private fun fixNodeLabelsInHE(
            hyperEdge: HyperEdge<String>,
            nodeCounter: AtomicLong
    ): HyperEdge<String> {
        val nodeLabelMap: MutableMap<String, String> = mutableMapOf()
        val fixedSource = fixNodeLabels(hyperEdge.source, nodeCounter, nodeLabelMap)
        val fixedTarget = fixNodeLabels(hyperEdge.target, nodeCounter, nodeLabelMap)
        return HyperEdge(fixedSource, hyperEdge.label, fixedTarget)
    }

    private fun fixNodeLabels(nodeLabels: List<String>, nodeCounter: AtomicLong, nodeLabelMap: MutableMap<String, String>): List<String> {
        return nodeLabels.map {
            fixNodeLabel(it, nodeCounter, nodeLabelMap)
        }
    }

    private fun fixNodeLabel(label: String, nodeCounter: AtomicLong, nodeLabelMap: MutableMap<String, String>): String {
        return when (label) {
            "_" -> "_"
            in nodeLabelMap.keys -> nodeLabelMap[label]!!
            else -> {
                val fixed = nodeCounter.getAndIncrement().toString()
                nodeLabelMap[label] = fixed
                fixed
            }
        }
    }

}
