package com.naveenai.app.actions

import com.naveenai.app.command.AssistantIntent
import java.time.Clock
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

class DateAction(
    private val clock: Clock = Clock.systemDefaultZone(),
) : AndroidAction {
    override val intent: AssistantIntent = AssistantIntent.DATE

    override suspend fun execute(): ActionResult {
        val formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG)
            .withLocale(Locale.getDefault())
        val date = formatter.format(ZonedDateTime.now(clock))
        return ActionResult(true, "Today is $date.")
    }
}