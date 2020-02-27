package hypergraph

class StateMachine<N, T>(private val rules: Map<String, HyperGraph<N>>, private val startState: HyperEdge<N>) {
    val hyperGraph = mutableHyperGraphOf(startState)
    private var inStartState = true
    private var nonTerminals = listOf<String>()

    fun apply(tokens: List<T>) {
        if (inStartState) {
            replaceHyperEdge(startState.label as NonTerminalEdgeLabel)
            inStartState = false
        }
        for (t in tokens) {
            val relevantNonTerminals = rules
                    .filter { entry ->
                        entry.key in nonTerminals &&
                        entry.value.any { it.label.matchesToken(t) }
                    }
                    .map { NonTerminal(it.key) }
            when (val size = relevantNonTerminals.size) {
                0    -> error("No rule found that matches token '$t'")
                1    -> replaceHyperEdge(relevantNonTerminals[0], t)
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

    private inline fun replaceHyperEdge(label: NonTerminalEdgeLabel, token: T? = null) {
        val hyperEdgeToReplace = hyperGraph.findHyperEdgeByLabel(label)

        val hyperGraphToReplaceWith = rules[label.name] ?: error("No applicable rule found")

        val iterSourceExternalNodes = hyperEdgeToReplace.source.toList().iterator()
        val iterTargetExternalNodes = hyperEdgeToReplace.target.toList().iterator()

        val newHyperEdges = hyperGraphToReplaceWith.map { edge ->
            val newSourceNodes = edge.source.map {
                if (it == "_") iterSourceExternalNodes.next()
                else it
            }

            val newEdgeLabel = edge.label
            if (token != null && newEdgeLabel is TerminalEdgeLabel<*>) {
                (newEdgeLabel as TerminalEdgeLabel<T>).applyToken(token)
            }

            val newTargetNodes = edge.target.map {
                if (it == "_") iterTargetExternalNodes.next()
                else it
            }

            HyperEdge(newSourceNodes, newEdgeLabel, newTargetNodes)
        }

        hyperGraph.deleteHyperEdgeInHyperGraph(hyperEdgeToReplace)
        hyperGraph.addAll(newHyperEdges)
        nonTerminals = hyperGraph.filter { it.label is NonTerminalEdgeLabel }
                .map { (it.label as NonTerminalEdgeLabel).name }
                .toList()
        println("result: $hyperGraph")
        println("nonTerminals: $nonTerminals")
    }

    private fun <N> MutableHyperGraph<N>.deleteHyperEdgeInHyperGraph(hyperEdgeToReplace: HyperEdge<N>) {
        val index = this.indexOfFirst { it == hyperEdgeToReplace }
        this.removeAt(index)
    }

    private fun <N> HyperGraph<N>.findHyperEdgeByLabel(label: NonTerminalEdgeLabel): HyperEdge<N> =
            this.filter { it.label is NonTerminalEdgeLabel }
                    .first { (it.label as NonTerminalEdgeLabel).name == label.name }
}