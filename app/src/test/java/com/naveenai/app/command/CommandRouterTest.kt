package com.naveenai.app.command

import org.junit.Assert.assertEquals
import org.junit.Test

class CommandRouterTest {
    @Test
    fun routesSearchCommandsToWebSearchIntent() {
        val router = CommandRouter()
        val result = router.route("Search for Python tutorials")

        assertEquals("Action execution for 'web_search' is planned for a future step.", result)
    }

    @Test
    fun routesReminderCommandsToReminderIntent() {
        val router = CommandRouter()
        val result = router.route("Set a reminder for 6 PM")

        assertEquals("Action execution for 'set_reminder' is planned for a future step.", result)
    }
}
