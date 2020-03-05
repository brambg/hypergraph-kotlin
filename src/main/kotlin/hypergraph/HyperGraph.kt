package hypergraph

data class HyperEdge<N>(
        val source: List<N>,
        val label: EdgeLabel,
        val target: List<N>
) {
    override fun toString(): String =
            "(${source.joinToString()})--[$label]->(${target.joinToString()})"
}

typealias HyperGraph<N> = List<HyperEdge<N>>

fun <N> hyperGraphOf(vararg edges: HyperEdge<N>) = listOf(*edges)

typealias MutableHyperGraph<N> = MutableList<HyperEdge<N>>

fun <N> mutableHyperGraphOf(vararg edges: HyperEdge<N>) = mutableListOf(*edges)
