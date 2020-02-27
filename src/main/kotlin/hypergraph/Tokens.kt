package hypergraph

interface Token

open class MarkupToken(val tagName: String) : Token

class OpenMarkupToken(tagName: String) : MarkupToken(tagName)

class CloseMarkupToken(tagName: String) : MarkupToken(tagName)

data class TextToken(val content: String) : Token
