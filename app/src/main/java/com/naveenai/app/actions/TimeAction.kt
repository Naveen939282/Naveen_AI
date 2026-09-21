package com.naveenai.app.actions

import com.naveenai.app.command.AssistantIntent
import java.time.Clock
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

class TimeAction(
    private val clock: Clock = Clock.systemDefaultZone(),
) : AndroidAction {
    override val intent: AssistantIntent = AssistantIntent.TIME

    override suspend fun execute(): ActionResult {
        val formatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)
            .withLocale(Locale.getDefault())
        val time = formatter.format(ZonedDateTime.now(clock))
        return ActionResult(true, "It is $time.")
    }
}