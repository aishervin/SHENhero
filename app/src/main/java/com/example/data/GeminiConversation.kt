package com.example.data

import com.example.data.local.ChatMessageEntity

internal fun buildGeminiConversation(
    history: List<ChatMessageEntity>,
    prompt: String,
    image: Part? = null
): List<Content> {
    val priorTurns = history
        .asSequence()
        .filter { it.mode != "error" && (it.sender == "user" || it.sender == "shen") }
        .map { message ->
            Content(
                role = if (message.sender == "user") "user" else "model",
                parts = listOf(Part(text = message.text.takeLast(MAX_CONTEXT_CHARS)))
            )
        }
        .toList()

    val currentParts = buildList {
        add(Part(text = prompt))
        image?.let(::add)
    }
    return priorTurns + Content(role = "user", parts = currentParts)
}

private const val MAX_CONTEXT_CHARS = 4_000
