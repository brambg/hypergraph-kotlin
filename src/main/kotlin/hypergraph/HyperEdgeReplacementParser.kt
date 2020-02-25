package hypergraph

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
    val hg = HyperEdgeReplacementParser.HyperGraph(mutableListOf(HyperEdgeReplacementParser.HyperEdge("S", listOf("1"), listOf("2"))))

    val rules = mapOf<String, HyperEdgeReplacementParser.HyperGraph<String>>(
            "S" to HyperEdgeReplacementParser.HyperGraph(mutableListOf(
                    HyperEdgeReplacementParser.HyperEdge("JOHN", listOf("_"), listOf("_")))),
            "JOHN" to HyperEdgeReplacementParser.HyperGraph(mutableListOf(
                    HyperEdgeReplacementParser.HyperEdge("John", listOf("_"), listOf("3")),
                    HyperEdgeReplacementParser.HyperEdge("LOVES", listOf("3"), listOf("_")))),
            "LOVES" to HyperEdgeReplacementParser.HyperGraph(mutableListOf(
                    HyperEdgeReplacementParser.HyperEdge("loves", listOf("_"), listOf("4")),
                    HyperEdgeReplacementParser.HyperEdge("MARY", listOf("4"), listOf("_")))),
            "MARY" to HyperEdgeReplacementParser.HyperGraph(mutableListOf(
                    HyperEdgeReplacementParser.HyperEdge("Mary", listOf("_"), listOf("_"))))
    )

    val stateMachine = HyperEdgeReplacementParser.StateMachine(hg, rules)

    println(stateMachine.hyperGraph)
    heReplace(stateMachine, "S")
    heReplace(stateMachine, "JOHN")
    heReplace(stateMachine, "LOVES")
    heReplace(stateMachine, "MARY")
}

