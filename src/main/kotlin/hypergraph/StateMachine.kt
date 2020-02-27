package hypergraph

class StateMachine<N>(private val rules: Map<String, HyperGraph<N>>, private val startState: HyperEdge<N>) {
    val hyperGraph = mutableHyperGraphOf(startState)
    private var inStartState = true
    private var nonTerminals = listOf<String>()

    fun apply(tokens: List<N>) {
        if (inStartState) {
            replaceHyperEdge(startState.label as NonTerminalEdgeLabel)
            inStartState = false
        }
        for (t in tokens) {
            val applicableRules = rules
                    .filter { entry -> entry.key in nonTerminals && entry.value.any { it.label.matchesToken(t) } }
                    .map { NonTerminal(it.key) }
            when (val size = applicableRules.size) {
                0    -> error("No rule found that matches token '$t'")
                1    -> replaceHyperEdge(applicableRules[0])
                else -> error("$size rules were found, that should not be possible!")
            }
        }
    }

    fun hasValidEndState(): Boolean = hyperGraph.none { it.label is NonTerminalEdgeLabel }

    fun reset() {
        hyperGraph.clear()
        hyperGraph.add(startState)
        inStartState = true
    }

    private fun replaceHyperEdge(label: NonTerminalEdgeLabel) {
        val hyperEdgeToReplace = hyperGraph.findHyperEdgeByLabel(label)

        val hyperGraphToReplaceWith = rules[label.name] ?: error("No applicable rule found")

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
            HyperEdge(copySourceNodes, edge.label, copyTargetNodes)
        }

        deleteHyperEdgeInHyperGraph(hyperGraph, hyperEdgeToReplace)
        hyperGraph.addAll(copyHyperEdges)
        nonTerminals = hyperGraph.filter { it.label is NonTerminalEdgeLabel }
                .map { (it.label as NonTerminalEdgeLabel).name }
                .toList()
        println("result: $hyperGraph")
        println("nonTerminals: $nonTerminals")
    }

    private fun <N> deleteHyperEdgeInHyperGraph(hyperGraph: MutableHyperGraph<N>, hyperEdgeToReplace: HyperEdge<N>) {
        val index = hyperGraph.indexOfFirst { it == hyperEdgeToReplace }
        hyperGraph.removeAt(index)
    }

    private fun <N> HyperGraph<N>.findHyperEdgeByLabel(label: NonTerminalEdgeLabel): HyperEdge<N> =
            this.filter { it.label is NonTerminalEdgeLabel }
                    .first { (it.label as NonTerminalEdgeLabel).name == label.name }
}