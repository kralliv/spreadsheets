package de.krall.spreadsheets.util

private val TEMPLATE_PATTERN = Regex("\\{([0-9]*)}")

fun String.messageFormat(vararg arguments: Any?): String {
    if (arguments.isEmpty()) return this

    val processedArguments = arrayOfNulls<Any>(arguments.size)
    for (i in arguments.indices) {
        var argument = arguments[i]
        if (argument is Throwable) {
            argument = argument.message
        }
        processedArguments[i] = argument
    }

    var positionalIndex = 0
    return this.replace(TEMPLATE_PATTERN) { match ->
        val explicitIndexText = match.groupValues[1]

        val argument = if (explicitIndexText.isNotEmpty()) {
            val index = explicitIndexText.toIntOrNull() ?: return@replace ""
            if (index >= processedArguments.size) return@replace ""
            processedArguments[index]
        } else {
            if (positionalIndex >= processedArguments.size) return@replace ""
            processedArguments[positionalIndex++]
        }

        argument.toString()
    }
}
