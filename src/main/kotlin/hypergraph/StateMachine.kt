package hypergraph

class StateMachine<N, T>(private val rules: Map<String, HyperGraph<N>>, private val startState: HyperEdge<N>) {
    val hyperGraph = mutableHyperGraphOf(startState)

    private var nonTerminalsInGraph = listOf(startState.label as NonTerminalEdgeLabel)

    fun apply(tokens: List<T>) {
        val nonTerminalEdgeIds = nonTerminalsInGraph.map { it.name }
        for (t in tokens) {
            val relevantNonTerminals = rules
                    .filter { rule ->
                        rule.key in nonTerminalEdgeIds &&
                        rule.value.any { (it.label as RuleEdgeLabel).tokenMatchPredicate(t, e) }
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
    }

    private inline fun replaceHyperEdge(label: NonTerminalEdgeLabel) {
        val hyperEdgeToReplace = hyperGraph.findHyperEdgeByLabel(label)

        val hyperGraphToReplaceWith = rules[label.name] ?: error("No applicable rule found")

        replaceHyperEdge(hyperEdgeToReplace, hyperGraphToReplaceWith)
    }

    private fun replaceHyperEdge(hyperEdgeToReplace: HyperEdge<N>, hyperGraphToReplaceWith: HyperGraph<N>) {
        val iterSourceExternalNodes = hyperEdgeToReplace.source.toList().iterator()
        val iterTargetExternalNodes = hyperEdgeToReplace.target.toList().iterator()

        val newHyperEdges = hyperGraphToReplaceWith.map { edge ->
            val newSourceNodes = edge.source.map {
                if (it == "_") iterSourceExternalNodes.next()
                else it
            }

            var newEdgeLabel = edge.label
            //            if (token != null && newEdgeLabel is LabelTemplate<*>) {
            //                (newEdgeLabel as LabelTemplate<T>).applyToken(token)
            //            }
            //            if (token != null && newEdgeLabel is RuleEdgeLabel) {
            //                newEdgeLabel = newEdgeLabel.edgeLabelMaker(token as Token)
            //            }

            val newTargetNodes = edge.target.map {
                if (it == "_") iterTargetExternalNodes.next()
                else it
            }

            HyperEdge(newSourceNodes, newEdgeLabel, newTargetNodes)
        }

        hyperGraph.deleteHyperEdgeInHyperGraph(hyperEdgeToReplace)
        hyperGraph.addAll(newHyperEdges)
        nonTerminalsInGraph = hyperGraph.filter { it.label is NonTerminalEdgeLabel }
                .map { it.label as NonTerminalEdgeLabel }
                .toList()
        println("result: $hyperGraph")
        println("nonTerminals: $nonTerminalsInGraph")
    }

    private fun <N> MutableHyperGraph<N>.deleteHyperEdgeInHyperGraph(hyperEdgeToReplace: HyperEdge<N>) {
        val index = this.indexOfFirst { it == hyperEdgeToReplace }
        this.removeAt(index)
    }

    private fun <N> HyperGraph<N>.findHyperEdgeByLabel(label: NonTerminalEdgeLabel): HyperEdge<N> =
            this.filter { it.label is NonTerminalEdgeLabel }
                    .first { (it.label as NonTerminalEdgeLabel).name == label.name }
}

/// rules : edgeId -> listOf(HyperEdge)
// rules document the he replacement rules, so the lhs should map to he in the hg, and the rhs should map to a
// replacementhg
// problem: we need parameterized rules -> the edges in the rhs of the rules should map to he, using information from
// the token, and the he from the hg
// the he in the rhs, when nonterminal, have a variable to link to the lhs of some other rules
//