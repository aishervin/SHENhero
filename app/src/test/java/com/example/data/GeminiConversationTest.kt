package com.example.data

import com.example.data.local.ChatMessageEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GeminiConversationTest {
    @Test
    fun mapsSavedTurnsToGeminiRolesAndAddsCurrentPrompt() {
        val history = listOf(
            ChatMessageEntity(sender = "user", text = "Hello"),
            ChatMessageEntity(sender = "shen", text = "Hi there"),
            ChatMessageEntity(sender = "shen", text = "An error", mode = "error")
        )

        val contents = buildGeminiConversation(history, "What is next?")

        assertEquals(listOf("user", "model", "user"), contents.map { it.role })
        assertEquals("Hello", contents[0].parts.single().text)
        assertEquals("Hi there", contents[1].parts.single().text)
        assertEquals("What is next?", contents[2].parts.single().text)
    }

    @Test
    fun putsImagePartBesideCurrentPrompt() {
        val image = Part(inlineData = InlineData("image/jpeg", "abc"))

        val current = buildGeminiConversation(emptyList(), "Describe this", image).single()

        assertEquals(2, current.parts.size)
        assertEquals("Describe this", current.parts[0].text)
        assertEquals("abc", current.parts[1].inlineData?.data)
    }

    @Test
    fun turnsWithoutImageDoNotAddEmptyImageData() {
        val current = buildGeminiConversation(emptyList(), "Hi").single()

        assertEquals(1, current.parts.size)
        assertNull(current.parts.single().inlineData)
    }
}
