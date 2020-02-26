package hypergraph

data class HyperEdge<N>(
        val label: String,
        val source: List<N>,
        val target: List<N>
)

typealias HyperGraph<N> = List<HyperEdge<N>>

fun <N> hyperGraphOf(vararg edges: HyperEdge<N>) = listOf(*edges)

typealias MutableHyperGraph<N> = MutableList<HyperEdge<N>>

fun <N> mutableHyperGraphOf(vararg edges: HyperEdge<N>) = mutableListOf(*edges)

fun String.isAllCaps(): Boolean = this.toUpperCase() == this

class StateMachine<N>(private val rules: Map<String, HyperGraph<N>>, private val startState: HyperEdge<N>) {
    val hyperGraph = mutableHyperGraphOf(startState)
    var inStartState = true

    fun apply(tokens: List<N>) {
        if (inStartState) {
            heReplace(startState.label)
            inStartState = false
        }
        for (t in tokens) {
            val applicableRules = rules.filter {
                it.value.any { hyperEdge -> hyperEdge.label == t }
            }
            when (val size = applicableRules.size) {
                0    -> error("No rule found that matches token '$t'")
                1    -> heReplace(applicableRules.iterator().next().key)
                else -> error("$size rules were found, that should not be possible!")
            }
        }
    }

    fun hasValidEndState(): Boolean = hyperGraph.none { it.label.isAllCaps() }

    fun reset() {
        hyperGraph.clear()
        hyperGraph.add(startState)
        inStartState = true
    }

    private fun heReplace(label: String) {
        val hyperEdgeToReplace = findHyperEdgeInHyperGraphByLabel(hyperGraph, label)

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
        println("result: $hyperGraph")
    }
}

private fun <N> deleteHyperEdgeInHyperGraph(hyperGraph: MutableHyperGraph<N>, hyperEdgeToReplace: HyperEdge<N>) {
    val index = hyperGraph.indexOfFirst { it == hyperEdgeToReplace }
    hyperGraph.removeAt(index)
}

private fun <N> findHyperEdgeInHyperGraphByLabel(hyperGraph: HyperGraph<N>, label: String): HyperEdge<N> {
    return hyperGraph.first { it.label == label }
}
