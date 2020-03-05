package hypergraph

interface EdgeLabel

abstract class NonTerminalEdgeLabel(val name: String) : EdgeLabel

interface TerminalEdgeLabel : EdgeLabel

class NonTerminal(labelId: String) : NonTerminalEdgeLabel(labelId) {
    override fun toString(): String = "NonTerminal($name)"
}

class Terminal(val content: String) : TerminalEdgeLabel

class OpenMarkupNonTerminal(labelId: String, val tagName: String) : NonTerminalEdgeLabel(labelId) {
    override fun toString(): String = "OpenMarkupNonTerminal($name($tagName))"
}

class RuleEdgeLabel(
        val tokenMatchPredicate: (Token, EdgeLabel) -> Boolean,
        val edgeLabelMaker: (Token) -> EdgeLabel
) : NonTerminalEdgeLabel("")

class TextTerminal(val content: String) : TerminalEdgeLabel {
    override fun toString(): String = """"$content""""
}

class MarkupTerminal(val tagName: String) : TerminalEdgeLabel {
    override fun toString(): String = "[$tagName]"
}
