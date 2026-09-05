package com.naveenai.app.command

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CommandRouterTest {
    @Test
    fun routesGreetingToGreetingResponse() {
        val router = CommandRouter()
        val result = router.route("Hello Naveen")

        assertTrue(result.success)
        assertEquals(AssistantIntent.GREETING, result.intent)
        assertEquals("Hello! How can I help you?", result.response)
    }

    @Test
    fun routesHelpToHelpResponse() {
        val router = CommandRouter()
        val result = router.route("What can you do")

        assertTrue(result.success)
        assertEquals(AssistantIntent.HELP, result.intent)
        assertTrue(result.response.contains("greetings and help"))
    }

    @Test
    fun routesUnknownCommandToHonestFallback() {
        val result = CommandRouter().route("xyz random command")

        assertFalse(result.success)
        assertEquals(AssistantIntent.UNKNOWN, result.intent)
        assertEquals("I didn't understand that command yet.", result.response)
    }
}

class LocalIntentClassifierTest {
    private val classifier = LocalIntentClassifier()

    @Test
    fun classifiesHelloAsGreeting() {
        assertEquals(AssistantIntent.GREETING, classifier.classify("hello").intent)
    }

    @Test
    fun classifiesWhatCanYouDoAsHelp() {
        assertEquals(AssistantIntent.HELP, classifier.classify("what can you do").intent)
    }

    @Test
    fun classifiesUnknownTextAsUnknown() {
        assertEquals(AssistantIntent.UNKNOWN, classifier.classify("xyz random command").intent)
    }
}
