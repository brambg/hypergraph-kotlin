package hypergraph

interface EdgeLabel {
    fun <T> matchesToken(token: T): Boolean
}

open class NonTerminalEdgeLabel(val name: String) : EdgeLabel {
    override fun <T> matchesToken(token: T): Boolean = false
}

data class MarkupNonTerminal(val label: String) : NonTerminalEdgeLabel(label) {
    override fun <T> matchesToken(token: T): Boolean =
            token is OpenMarkupToken
}

data class OpenMarkupNonTerminal(val label: String, val parameter: (MarkupToken) -> Any) : NonTerminalEdgeLabel(label),
        LabelTemplate<OpenMarkupToken> {
    private var parameterValue: Any? = null

    override fun <T> matchesToken(token: T): Boolean =
            token is OpenMarkupToken //&& parameter(token) == parameterValue

    override fun applyToken(token: OpenMarkupToken) {
    }
}

data class TextNonTerminal(val label: String) : NonTerminalEdgeLabel(label) {
    override fun <T> matchesToken(token: T): Boolean =
            token is TextToken
}

interface LabelTemplate<in T> {
    fun applyToken(token: T)
}

abstract class TerminalEdgeLabel<in T> : EdgeLabel, LabelTemplate<T>

class TextTerminal : TerminalEdgeLabel<TextToken>() {
    private var content: String? = null

    override fun <T> matchesToken(token: T): Boolean = token is TextToken

    override fun applyToken(textToken: TextToken) {
        content = textToken.content
    }

    override fun toString(): String =
            if (content != null) {
                """"$content""""
            } else {
                error("content not set")
            }
}

class MarkupTerminal(val parameter: (MarkupToken) -> Any) : TerminalEdgeLabel<MarkupToken>() {

    private var parameterValue: Any? = null

    override fun <T> matchesToken(token: T): Boolean = token is CloseMarkupToken && parameter(token) == parameterValue

    override fun toString(): String =
            if (parameterValue != null) {
                "[$parameterValue]"
            } else {
                error("markupName not set")
            }

    override fun applyToken(token: MarkupToken) {
        this.parameterValue = parameter(token)
    }
}

data class NonTerminal(val label: String) : NonTerminalEdgeLabel(label)

data class Terminal(val content: String) : TerminalEdgeLabel<String>() {
    override fun <T> matchesToken(token: T): Boolean =
            token == content

    override fun applyToken(token: String) {
    }
}
