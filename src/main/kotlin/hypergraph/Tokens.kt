package hypergraph

interface Token

open class MarkupToken(val tagName: String) : Token

class OpenMarkupToken(tagName: String) : MarkupToken(tagName) {
    override fun toString(): String = "[$tagName>"
}

class CloseMarkupToken(tagName: String) : MarkupToken(tagName) {
    override fun toString(): String = "<$tagName]"
}

data class TextToken(val content: String) : Token {
    override fun toString(): String = content
}
