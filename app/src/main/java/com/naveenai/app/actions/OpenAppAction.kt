package com.naveenai.app.actions

import android.content.ActivityNotFoundException
import android.content.Context
import com.naveenai.app.command.AssistantIntent

fun interface AppLauncher {
    fun launch(app: InstalledApp)
}

class PackageManagerAppLauncher(
    context: Context,
) : AppLauncher {
    private val appContext = context.applicationContext

    override fun launch(app: InstalledApp) {
        val launchIntent = appContext.packageManager.getLaunchIntentForPackage(app.packageName)
            ?: throw ActivityNotFoundException("No launcher activity for ${app.packageName}")
        launchIntent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
        appContext.startActivity(launchIntent)
    }
}

class OpenAppAction(
    private val resolver: AppResolver,
    private val launcher: AppLauncher,
) : AndroidAction {
    override val intent = AssistantIntent.OPEN_APP

    override suspend fun execute(request: ActionRequest): ActionResult {
        val requestedName = request.argument?.trim().orEmpty()
        if (requestedName.isEmpty()) {
            return ActionResult(false, "Please tell me which app to open.")
        }

        return when (val resolution = resolver.resolve(requestedName)) {
            is AppResolution.Missing ->
                ActionResult(false, "I couldn't find $requestedName on this device.")
            is AppResolution.Ambiguous ->
                ActionResult(false, "I found multiple apps named $requestedName. Please be more specific.")
            is AppResolution.Found -> try {
                launcher.launch(resolution.app)
                ActionResult(true, "Opening ${resolution.app.label}.")
            } catch (_: ActivityNotFoundException) {
                ActionResult(false, "I couldn't open ${resolution.app.label} on this device.")
            } catch (_: Exception) {
                ActionResult(false, "I couldn't open ${resolution.app.label} on this device.")
            }
        }
    }

    companion object {
        fun forAndroid(context: Context): OpenAppAction = OpenAppAction(
            resolver = PackageManagerAppResolver(context),
            launcher = PackageManagerAppLauncher(context),
        )
    }
}