package hypergraph

data class HyperEdge<N>(
        val source: List<N>,
        val label: EdgeLabel,
        val target: List<N>
) {
    override fun toString(): String {
        return "(${source.joinToString()})--[$label]->(${target.joinToString()})"
    }
}

typealias HyperGraph<N> = List<HyperEdge<N>>

fun <N> hyperGraphOf(vararg edges: HyperEdge<N>) = listOf(*edges)

typealias MutableHyperGraph<N> = MutableList<HyperEdge<N>>

fun <N> mutableHyperGraphOf(vararg edges: HyperEdge<N>) = mutableListOf(*edges)

//fun String.isAllCaps(): Boolean = this.toUpperCase() == this

interface EdgeLabel {
    fun <T> matchesToken(token: T): Boolean
}

open class TerminalEdgeLabel : EdgeLabel {
    // terminals are already matched, can't be matched again
    override fun <T> matchesToken(token: T): Boolean = false
}

abstract class NonTerminalEdgeLabel(val name: String) : EdgeLabel

data class MarkupNonTerminal(val label: String) : NonTerminalEdgeLabel(label) {
    override fun <T> matchesToken(token: T): Boolean = token is OpenMarkupToken
}

data class OpenMarkupNonTerminal(val tagName: String) : NonTerminalEdgeLabel(tagName) {
    override fun <T> matchesToken(token: T): Boolean =
            token is CloseMarkupToken && token.tagName == tagName
}

data class ClosedMarkupTerminal(val tagName: String) : TerminalEdgeLabel()

data class TextNonTerminal(val variableName: String) : NonTerminalEdgeLabel(variableName) {
    override fun <T> matchesToken(token: T): Boolean = token is TextToken
}

data class TextTerminal(val content: String) : TerminalEdgeLabel()

data class NonTerminal(val label: String) : NonTerminalEdgeLabel(label)

data class Terminal(val content: String) : TerminalEdgeLabel() {
    override fun <T> matchesToken(token: T): Boolean =
            (label == "OBJECT" && token == "John") ||
            (label == "VERB" && token == "loves") ||
            (label == "SUBJECT" && token == "Mary")
}

interface Token

data class OpenMarkupToken(val tagName: String) : Token
data class CloseMarkupToken(val tagName: String) : Token
data class TextToken(val content: String) : Token
