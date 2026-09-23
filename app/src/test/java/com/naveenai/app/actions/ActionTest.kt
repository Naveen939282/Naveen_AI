package com.naveenai.app.actions

import com.naveenai.app.ai.AIRequest
import com.naveenai.app.ai.AIResponse
import com.naveenai.app.ai.AIProvider
import com.naveenai.app.ai.AssistantCoordinator
import com.naveenai.app.command.AssistantIntent
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ActionResultTest {
    @Test
    fun representsSuccessAndFailure() {
        assertTrue(ActionResult(true, "Done").success)
        assertFalse(ActionResult(false, "Failed").success)
    }
}

class ActionDispatcherTest {
    @Test
    fun selectsActionByIntent() = runBlocking {
        val action = FakeAction(AssistantIntent.TIME, ActionResult(true, "Selected"))

        assertEquals(ActionResult(true, "Selected"), ActionDispatcher(listOf(action)).dispatch(AssistantIntent.TIME))
        assertTrue(action.executed)
    }

    @Test
    fun handlesUnsupportedAction() = runBlocking {
        val result = ActionDispatcher(emptyList()).dispatch(AssistantIntent.TIME)

        assertFalse(result.success)
        assertTrue(result.message.isNotEmpty())
    }
}

class TimeAndDateActionTest {
    private val clock = Clock.fixed(Instant.parse("2026-09-21T16:30:00Z"), ZoneId.of("UTC"))

    @Test
    fun timeActionReturnsSuccessAndReadableOutput() = runBlocking {
        val result = TimeAction(clock).execute()

        assertTrue(result.success)
        assertTrue(result.message.isNotEmpty())
    }

    @Test
    fun dateActionReturnsSuccessAndReadableOutput() = runBlocking {
        val result = DateAction(clock).execute()

        assertTrue(result.success)
        assertTrue(result.message.isNotEmpty())
    }
}

class ActionCoordinatorTest {
    @Test
    fun deterministicActionsDoNotCallOllama() = runBlocking {
        val provider = RecordingAIProvider()
        val openAppAction = FakeAction(AssistantIntent.OPEN_APP, ActionResult(true, "Opening YouTube."))
        val coordinator = AssistantCoordinator(
            actionDispatcher = ActionDispatcher(listOf(TimeAction(), DateAction(), openAppAction)),
            aiProvider = provider,
        )

        assertTrue(coordinator.process("What time is it?").success)
        assertTrue(coordinator.process("What's today's date?").success)
        assertTrue(coordinator.process("Open YouTube").success)
        assertTrue(openAppAction.executed)
        assertEquals(0, provider.calls)
    }
}

private class FakeAction(
    override val intent: AssistantIntent,
    private val result: ActionResult,
) : AndroidAction {
    var executed = false

    override suspend fun execute(request: ActionRequest): ActionResult {
        executed = true
        return result
    }
}

private class RecordingAIProvider : AIProvider {
    var calls = 0

    override suspend fun generateResponse(request: AIRequest): AIResponse {
        calls++
        return AIResponse(true, "Unexpected provider call")
    }

    override fun isAvailable(): Boolean = true
}

class LabelAppResolverTest {
    private val resolver = LabelAppResolver(
        listOf(
            InstalledApp("com.google.android.youtube", "YouTube"),
            InstalledApp("com.spotify.music", "Spotify"),
            InstalledApp("com.example.youtube", "YouTube"),
        ),
    )

    @Test
    fun resolvesExactLabelCaseInsensitively() {
        assertEquals(
            AppResolution.Ambiguous(
                listOf(
                    InstalledApp("com.google.android.youtube", "YouTube"),
                    InstalledApp("com.example.youtube", "YouTube"),
                ),
            ),
            resolver.resolve("youtube"),
        )
        assertEquals(
            AppResolution.Found(InstalledApp("com.spotify.music", "Spotify")),
            resolver.resolve("SPOTIFY"),
        )
    }

    @Test
    fun handlesMissingAndNormalizedLabels() {
        assertEquals(AppResolution.Missing, resolver.resolve("Maps"))
        assertEquals(
            AppResolution.Found(InstalledApp("com.spotify.music", "Spotify")),
            resolver.resolve("  Spotify!!! "),
        )
    }
}

class OpenAppActionTest {
    private val app = InstalledApp("com.spotify.music", "Spotify")

    @Test
    fun launchesResolvedApplication() = runBlocking {
        val launcher = RecordingLauncher()
        val result = OpenAppAction(FakeResolver(AppResolution.Found(app)), launcher)
            .execute(ActionRequest(AssistantIntent.OPEN_APP, "Spotify"))

        assertTrue(result.success)
        assertEquals(app, launcher.launchedApp)
    }

    @Test
    fun reportsMissingApplication() = runBlocking {
        val result = OpenAppAction(FakeResolver(AppResolution.Missing), RecordingLauncher())
            .execute(ActionRequest(AssistantIntent.OPEN_APP, "Maps"))

        assertFalse(result.success)
        assertTrue(result.message.contains("Maps"))
    }

    @Test
    fun reportsLaunchFailureWithoutThrowing() = runBlocking {
        val launcher = RecordingLauncher { error("launch failed") }
        val result = OpenAppAction(FakeResolver(AppResolution.Found(app)), launcher)
            .execute(ActionRequest(AssistantIntent.OPEN_APP, "Spotify"))

        assertFalse(result.success)
        assertTrue(result.message.contains("Spotify"))
    }
}

private class FakeResolver(
    private val resolution: AppResolution,
) : AppResolver {
    override fun resolve(appName: String): AppResolution = resolution
}

private class RecordingLauncher(
    private val action: () -> Unit = {},
) : AppLauncher {
    var launchedApp: InstalledApp? = null

    override fun launch(app: InstalledApp) {
        launchedApp = app
        action()
    }
}

class OpenUrlActionTest {
    @Test
    fun normalizesNamedAndDomainUrls() {
        assertEquals("https://www.google.com", OpenUrlAction.normalize("Google"))
        assertEquals("https://www.google.com", OpenUrlAction.normalize("www.google.com"))
        assertEquals("https://github.com", OpenUrlAction.normalize("github.com"))
        assertEquals("https://github.com", OpenUrlAction.normalize("https://github.com"))
        assertEquals("http://example.com", OpenUrlAction.normalize("http://example.com"))
        assertEquals("https://www.youtube.com", OpenUrlAction.normalize("youtube"))
        assertEquals("https://www.youtube.com", OpenUrlAction.normalize("youtube.com"))
        assertEquals("https://www.youtube.com", OpenUrlAction.normalize("www.youtube.com"))
        assertEquals("https://www.youtube.com", OpenUrlAction.normalize("https://youtube.com"))
        assertEquals("https://www.youtube.com", OpenUrlAction.normalize("https://www.youtube.com"))
    }

    @Test
    fun launchesYouTubeUrlVariants() = runBlocking {
        listOf(
            "Open YouTube.com" to "YouTube.com!",
            "Go to YouTube.com" to "YouTube.com",
            "Open www.youtube.com" to "www.youtube.com",
            "Open https://youtube.com" to "https://youtube.com",
        ).forEach { (_, target) ->
            val launcher = RecordingUrlLauncher()
            val result = OpenUrlAction(launcher).execute(
                ActionRequest(AssistantIntent.OPEN_URL, target)
            )

            assertTrue(result.success)
            assertEquals("https://www.youtube.com", launcher.launchedUrl)
        }
    }

    @Test
    fun reportsLaunchFailureWithoutThrowing() = runBlocking {
        val result = OpenUrlAction(RecordingUrlLauncher { error("no browser") }).execute(
            ActionRequest(AssistantIntent.OPEN_URL, "github.com")
        )

        assertFalse(result.success)
        assertEquals("I couldn't open that link.", result.message)
    }
}

private class RecordingUrlLauncher(
    private val action: () -> Unit = {},
) : UrlLauncher {
    var launchedUrl: String? = null

    override fun launch(url: String) {
        launchedUrl = url
        action()
    }
}