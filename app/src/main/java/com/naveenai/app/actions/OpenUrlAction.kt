package com.naveenai.app.actions

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.naveenai.app.command.AssistantIntent

fun interface UrlLauncher {
    fun launch(url: String)
}

class PackageManagerUrlLauncher(
    context: Context,
) : UrlLauncher {
    private val appContext = context.applicationContext

    override fun launch(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        appContext.startActivity(intent)
    }
}

class OpenUrlAction(
    private val launcher: UrlLauncher,
) : AndroidAction {
    override val intent = AssistantIntent.OPEN_URL

    override suspend fun execute(request: ActionRequest): ActionResult {
        val url = normalize(request.argument.orEmpty())
            ?: return ActionResult(false, "I couldn't open that link.")

        return try {
            launcher.launch(url)
            ActionResult(true, "Opening $url.")
        } catch (_: ActivityNotFoundException) {
            ActionResult(false, "I couldn't open that link.")
        } catch (_: Exception) {
            ActionResult(false, "I couldn't open that link.")
        }
    }

    companion object {
        fun forAndroid(context: Context): OpenUrlAction = OpenUrlAction(
            launcher = PackageManagerUrlLauncher(context),
        )

        fun normalize(target: String): String? {
            val trimmed = target.trim().trimEnd('.', '!', '?')
            val normalized = trimmed.lowercase()
            if (normalized.isEmpty()) return null

            return when (normalized) {
                "google" -> "https://www.google.com"
                "github" -> "https://github.com"
                "youtube",
                "youtube.com",
                "www.youtube.com",
                "https://youtube.com",
                "https://www.youtube.com",
                -> "https://www.youtube.com"
                else -> when {
                    normalized.startsWith("https://") || normalized.startsWith("http://") -> trimmed
                    normalized.startsWith("www.") || normalized.contains('.') -> "https://$trimmed"
                    else -> null
                }
            }
        }
    }
}