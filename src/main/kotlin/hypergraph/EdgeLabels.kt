package hypergraph

interface EdgeLabel

open class NonTerminalEdgeLabel(val name: String) : EdgeLabel

data class MarkupNonTerminal(val nonTerminal: String) : NonTerminalEdgeLabel(nonTerminal)

data class OpenMarkupNonTerminal(val nonTerminal: String, val tagName: String) : NonTerminalEdgeLabel(nonTerminal)

data class TextNonTerminal(val label: String) : NonTerminalEdgeLabel(label)

abstract class TerminalEdgeLabel<in T> : EdgeLabel

class TextTerminal(val content: String) : TerminalEdgeLabel<TextToken>() {
    override fun toString(): String = """"$content""""
}

class MarkupTerminal(val tagName: String) : TerminalEdgeLabel<MarkupToken>() {
    override fun toString(): String = "[$tagName]"
}

class RuleEdgeLabel(
        val tokenMatchPredicate: (Token, EdgeLabel) -> Boolean,
        val edgeLabelMaker: (Token) -> EdgeLabel
) : NonTerminalEdgeLabel("")

data class NonTerminal(val labelId: String) : NonTerminalEdgeLabel(labelId)

data class Terminal(val content: String) : TerminalEdgeLabel<String>()
