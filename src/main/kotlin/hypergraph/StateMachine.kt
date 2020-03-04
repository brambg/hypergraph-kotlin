package hypergraph

class StateMachine<N>(private val rules: Map<String, HyperGraph<N>>, private val startState: HyperEdge<N>) {
    val hyperGraph = mutableHyperGraphOf(startState)
    var nonTerminals = listOf<String>(startState.label)

    fun apply(tokens: List<N>) {
        for (t in tokens) {
            val applicableRules = rules
                    .filter { entry -> entry.key in nonTerminals && entry.value.any { it.label == t } }
                    .map { it.key }.toList()
            when (val size = applicableRules.size) {
                0    -> error("No rule found that matches token '$t'")
                1    -> replaceHyperEdge(applicableRules[0])
                else -> error("$size rules were found, that should not be possible!")
            }
        }
    }

    fun hasValidEndState(): Boolean = hyperGraph.none { it.label.isAllCaps() }

    fun reset() {
        hyperGraph.clear()
        hyperGraph.add(startState)
    }

    private fun replaceHyperEdge(label: String) {
        val hyperEdgeToReplace = hyperGraph.findHyperEdgeByLabel(label)

        val hyperGraphToReplaceWith = rules[label] ?: error("No applicable rule found")

        val iterSourceExternalNodes = hyperEdgeToReplace.source.toList().iterator()
        val iterTargetExternalNodes = hyperEdgeToReplace.target.toList().iterator()

        val copyHyperEdges = hyperGraphToReplaceWith.map { edge ->
            val copySourceNodes = edge.source.map {
                if (it == "_") iterSourceExternalNodes.next()
                else it
            }.toList()
            val copyTargetNodes = edge.target.map {
                if (it == "_") iterTargetExternalNodes.next()
                else it
            }.toList()
            HyperEdge(edge.label, copySourceNodes, copyTargetNodes)
        }

        deleteHyperEdgeInHyperGraph(hyperGraph, hyperEdgeToReplace)
        hyperGraph.addAll(copyHyperEdges)
        nonTerminals = hyperGraph.filter { it.label.isAllCaps() }.map { it.label }.toList()
        println("result: $hyperGraph")
        println("nonTerminals: $nonTerminals")
    }

    private fun <N> deleteHyperEdgeInHyperGraph(hyperGraph: MutableHyperGraph<N>, hyperEdgeToReplace: HyperEdge<N>) {
        val index = hyperGraph.indexOfFirst { it == hyperEdgeToReplace }
        hyperGraph.removeAt(index)
    }

    private fun <N> HyperGraph<N>.findHyperEdgeByLabel(label: String): HyperEdge<N> =
            this.first { it.label == label }
}