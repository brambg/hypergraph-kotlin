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

data class StateMachine<N>(
        val hyperGraph: MutableHyperGraph<N>,
        val rules: Map<String, HyperGraph<N>>
) {
    fun isInValidEndState(): Boolean = hyperGraph.none { it.label.isAllCaps() }
}

internal fun <N> heReplace(stateMachine: StateMachine<N>, label: String) {
    val hyperEdgeToReplace = findHyperEdgeInHyperGraphByLabel(stateMachine.hyperGraph, label)

    val hyperGraphToReplaceWith = stateMachine.rules[label] ?: error("No applicable rule found")

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

    deleteHyperEdgeInHyperGraph(stateMachine.hyperGraph, hyperEdgeToReplace)
    stateMachine.hyperGraph.addAll(copyHyperEdges)
    println("result: ${stateMachine.hyperGraph}")
}

private fun <N> deleteHyperEdgeInHyperGraph(hyperGraph: MutableHyperGraph<N>, hyperEdgeToReplace: HyperEdge<N>) {
    val index = hyperGraph.indexOfFirst { it == hyperEdgeToReplace }
    hyperGraph.removeAt(index)
}

private fun <N> findHyperEdgeInHyperGraphByLabel(hyperGraph: HyperGraph<N>, label: String): HyperEdge<N> {
    return hyperGraph.first { it.label == label }
}
