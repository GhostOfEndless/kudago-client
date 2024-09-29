package org.example.util

class NewsPrinter {
    private val stringBuilder = StringBuilder()

    fun header(level: Int, content: () -> String) {
        stringBuilder.append("#".repeat(level)).append(" ")
        stringBuilder.append(content()).append("\n\n")
    }

    fun text(content: () -> String) {
        stringBuilder.append(content()).append("\n")
    }

    fun bold(content: String) = "**$content**"

    fun underlined(content: String) = "__${content}__"

    fun link(url: String, text: String) = "[$text]($url)"

    fun divider() {
        stringBuilder.append("\n---\n\n")
    }

    fun build() = stringBuilder.toString()
}