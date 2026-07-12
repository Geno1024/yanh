package g.sw.star.ssh

data class ParsedCommand(
    val className: String,
    val methodName: String,
    val args: Map<String, String>,
)

object CommandParser {
    fun parse(line: String): ParsedCommand {
        val tokens = tokenize(line)
        if (tokens.size < 2) {
            throw IllegalArgumentException("Usage: {class} {method} [--key value ...]")
        }

        val className = tokens[0]
        val methodName = tokens[1]

        val args = mutableMapOf<String, String>()
        var i = 2
        while (i < tokens.size) {
            val token = tokens[i]
            if (!token.startsWith("--")) {
                throw IllegalArgumentException("Expected --flag, got: $token")
            }
            val stripped = token.removePrefix("--")
            val eq = stripped.indexOf('=')
            if (eq != -1) {
                args[stripped.substring(0, eq)] = stripped.substring(eq + 1)
                i++
            } else {
                val key = stripped
                i++
                if (i < tokens.size && !tokens[i].startsWith("--")) {
                    args[key] = tokens[i]
                    i++
                } else {
                    args[key] = "true"
                }
            }
        }

        return ParsedCommand(className, methodName, args)
    }

    private fun tokenize(line: String): List<String> {
        val result = mutableListOf<String>()
        val current = StringBuilder()
        var inQuote = false
        for (ch in line) {
            when {
                ch == '"' -> inQuote = !inQuote
                ch == ' ' && !inQuote -> {
                    if (current.isNotEmpty()) {
                        result.add(current.toString())
                        current.clear()
                    }
                }
                else -> current.append(ch)
            }
        }
        if (current.isNotEmpty()) result.add(current.toString())
        return result
    }
}
