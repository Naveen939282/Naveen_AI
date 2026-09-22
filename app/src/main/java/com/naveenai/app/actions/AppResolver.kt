package com.naveenai.app.actions

import android.content.Context
import android.content.Intent
import java.util.Locale

data class InstalledApp(
    val packageName: String,
    val label: String,
)

sealed class AppResolution {
    data class Found(val app: InstalledApp) : AppResolution()
    data object Missing : AppResolution()
    data class Ambiguous(val apps: List<InstalledApp>) : AppResolution()
}

interface AppResolver {
    fun resolve(appName: String): AppResolution
}

class LabelAppResolver(
    private val installedApps: List<InstalledApp>,
) : AppResolver {
    override fun resolve(appName: String): AppResolution {
        val normalizedName = normalize(appName)
        if (normalizedName.isEmpty()) return AppResolution.Missing

        val matches = installedApps
            .distinctBy(InstalledApp::packageName)
            .filter { normalize(it.label) == normalizedName }

        return when (matches.size) {
            0 -> AppResolution.Missing
            1 -> AppResolution.Found(matches.first())
            else -> AppResolution.Ambiguous(matches)
        }
    }

    companion object {
        fun normalize(value: String): String = value
            .trim()
            .lowercase(Locale.ROOT)
            .replace(Regex("[^\\p{L}\\p{N}]+"), " ")
            .trim()
    }
}

class PackageManagerAppResolver(
    context: Context,
) : AppResolver {
    private val appContext = context.applicationContext

    override fun resolve(appName: String): AppResolution {
        val packageManager = appContext.packageManager
        val launcherIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val installedApps = packageManager.queryIntentActivities(launcherIntent, 0)
            .map { info ->
                InstalledApp(
                    packageName = info.activityInfo.packageName,
                    label = info.loadLabel(packageManager).toString(),
                )
            }
        return LabelAppResolver(installedApps).resolve(appName)
    }
}