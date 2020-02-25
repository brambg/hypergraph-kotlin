package hypergraph

import hypergraph.HyperEdgeReplacementParser.HyperEdge
import hypergraph.HyperEdgeReplacementParser.HyperGraph
import hypergraph.HyperEdgeReplacementParser.StateMachine
import hypergraph.HyperEdgeReplacementParser.heReplace

object HyperEdgeReplacementParser {

    data class HyperEdge<N>(
            val label: String,
            val source: List<N>,
            val target: List<N>
    )

    data class HyperGraph<N>(
            val edges: MutableList<HyperEdge<N>>
    )

    data class StateMachine<N>(
            val hyperGraph: HyperGraph<N>,
            val rules: Map<String, HyperGraph<N>>
    )

    internal fun <N> heReplace(stateMachine: StateMachine<N>, label: String) {
        val hyperEdgeToReplace = findHyperEdgeInHyperGraphByLabel(stateMachine.hyperGraph, label)

        val hyperGraphToReplaceWith = stateMachine.rules[label] ?: error("No applicable rule found")

        val iterSourceExternalNodes = hyperEdgeToReplace.source.toList().iterator()
        val iterTargetExternalNodes = hyperEdgeToReplace.target.toList().iterator()

        val copyHyperEdges = hyperGraphToReplaceWith.edges.map { edge ->
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
        stateMachine.hyperGraph.edges.addAll(copyHyperEdges)
        println("result: ${stateMachine.hyperGraph}")
    }

    private fun <N> deleteHyperEdgeInHyperGraph(hyperGraph: HyperGraph<N>, hyperEdgeToReplace: HyperEdge<N>) {
        val index = hyperGraph.edges.indexOfFirst { it == hyperEdgeToReplace }
        hyperGraph.edges.removeAt(index)
    }

    private fun <N> findHyperEdgeInHyperGraphByLabel(hyperGraph: HyperGraph<N>, label: String): HyperEdge<N> {
        return hyperGraph.edges.first { it.label == label }
    }
}

fun main() {
    val tokens = listOf("John", "loves", "Mary")
    val hg = HyperGraph(mutableListOf(HyperEdge("S", listOf("1"), listOf("2"))))

    val rules = mapOf(
            "S" to HyperGraph(mutableListOf(
                    HyperEdge("JOHN", listOf("_"), listOf("_")))),
            "JOHN" to HyperGraph(mutableListOf(
                    HyperEdge("John", listOf("_"), listOf("3")),
                    HyperEdge("LOVES", listOf("3"), listOf("_")))),
            "LOVES" to HyperGraph(mutableListOf(
                    HyperEdge("loves", listOf("_"), listOf("4")),
                    HyperEdge("MARY", listOf("4"), listOf("_")))),
            "MARY" to HyperGraph(mutableListOf(
                    HyperEdge("Mary", listOf("_"), listOf("_"))))
    )

    val stateMachine = StateMachine(hg, rules)

    println(stateMachine.hyperGraph)
    heReplace(stateMachine, "S")
    heReplace(stateMachine, "JOHN")
    heReplace(stateMachine, "LOVES")
    heReplace(stateMachine, "MARY")
}
