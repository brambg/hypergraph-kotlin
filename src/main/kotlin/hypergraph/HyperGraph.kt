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
