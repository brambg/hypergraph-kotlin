package hypergraph

interface EdgeLabel {
//    fun <T> matchesToken(token: T): Boolean
}

open class NonTerminalEdgeLabel(val name: String) : EdgeLabel {
//    override fun <T> matchesToken(token: T): Boolean = false
}

//data class MarkupNonTerminal(val nonTerminal: String, val parameter: String = "") : NonTerminalEdgeLabel(nonTerminal) {
//
//    override fun <T> matchesToken(token: T): Boolean =
//            token is OpenMarkupToken
//}

data class OpenMarkupNonTerminal(val nonTerminal: String, val tagName: String) : NonTerminalEdgeLabel(nonTerminal)/*,
        LabelTemplate<OpenMarkupToken>*/ {

//    override fun <T> matchesToken(token: T): Boolean =
//            token is OpenMarkupToken
//
//    override fun applyToken(token: OpenMarkupToken) {
//    }
}

data class TextNonTerminal(val label: String) : NonTerminalEdgeLabel(label) {
//    override fun <T> matchesToken(token: T): Boolean =
//            token is TextToken
}

//interface LabelTemplate<in T> {
//    fun applyToken(token: T)
//}

abstract class TerminalEdgeLabel<in T> : EdgeLabel/*, LabelTemplate<T>*/

class TextTerminal(val content: String) : TerminalEdgeLabel<TextToken>() {

//    override fun <T> matchesToken(token: T): Boolean = token is TextToken

//    override fun applyToken(textToken: TextToken) {
//        content = textToken.content
//    }

    override fun toString(): String =
            if (content != null) {
                """"$content""""
            } else {
                error("content not set")
            }
}

class MarkupTerminal(val tagName: String) : TerminalEdgeLabel<MarkupToken>() {

//    override fun <T> matchesToken(token: T): Boolean = token is CloseMarkupToken && token.tagName == tagName

    override fun toString(): String =
            if (tagName != null) {
                "[$tagName]"
            } else {
                error("tagName not set")
            }

//    override fun applyToken(token: MarkupToken) {
//    }
}

class RuleEdgeLabel(
        val tokenMatchPredicate: (Token, EdgeLabel) -> Boolean,
        val edgeLabelMaker: (Token) -> EdgeLabel
) :
        NonTerminalEdgeLabel("") {

    // to determine if this rule applies to the given token
//    override fun <T> matchesToken(token: T): Boolean = tokenMatchPredicate(token as Token, this)

    // to produce the EdgeLabel for thw HyperGraph
    fun applyToken(token: Token): EdgeLabel = edgeLabelMaker(token)
}

data class NonTerminal(val labelId: String) : NonTerminalEdgeLabel(labelId)

data class Terminal(val content: String) : TerminalEdgeLabel<String>() {
//    override fun <T> matchesToken(token: T): Boolean =
//            token == content
//
//    override fun applyToken(token: String) {
//    }
}
