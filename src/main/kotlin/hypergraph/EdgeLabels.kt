package hypergraph

interface EdgeLabel

open class NonTerminalEdgeLabel(val name: String) : EdgeLabel

abstract class TerminalEdgeLabel : EdgeLabel

data class NonTerminal(val labelId: String) : NonTerminalEdgeLabel(labelId)

data class Terminal(val content: String) : TerminalEdgeLabel()

data class OpenMarkupNonTerminal(val labelId: String, val tagName: String) : NonTerminalEdgeLabel(labelId)

class RuleEdgeLabel(
        val tokenMatchPredicate: (Token, EdgeLabel) -> Boolean,
        val edgeLabelMaker: (Token) -> EdgeLabel
) : NonTerminalEdgeLabel("")

class TextTerminal(val content: String) : TerminalEdgeLabel() {
    override fun toString(): String = """"$content""""
}

class MarkupTerminal(val tagName: String) : TerminalEdgeLabel() {
    override fun toString(): String = "[$tagName]"
}
