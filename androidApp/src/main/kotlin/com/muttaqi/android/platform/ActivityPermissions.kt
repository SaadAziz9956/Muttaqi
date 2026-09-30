package com.muttaqi.android.platform

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.edit

/**
 * Runtime permissions for the device services the shared code uses, through the Activity Result APIs. The system's
 * dialog needs an activity while the services are app-wide, so this follows whichever activity is showing. Create it
 * before the first activity starts (it's made at Koin's start), so it sees that one too
 */
class ActivityPermissions(private val application: Application) : Application.ActivityLifecycleCallbacks {
    private var activity: ComponentActivity? = null
    /** Which permissions have been asked for, since the system can't tell "never asked" from "don't ask again" */
    private val asked = application.getSharedPreferences("muttaqi_permissions", Context.MODE_PRIVATE)
    private var requests = 0

    init {
        application.registerActivityLifecycleCallbacks(this)
    }

    fun isGranted(permission: String): Boolean =
        ContextCompat.checkSelfPermission(application, permission) == PackageManager.PERMISSION_GRANTED

    /** Refused so that the system won't ask again, so only the app's settings can grant it */
    fun isPermanentlyDenied(permission: String): Boolean {
        val activity = activity ?: return false
        return !isGranted(permission) &&
            asked.getBoolean(permission, false) &&
            !ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)
    }

    /** Shows the system's dialog for [permissions], then reports which were granted; nothing is asked without an activity */
    fun request(permissions: Array<String>, onResult: (Map<String, Boolean>) -> Unit) {
        val activity = activity ?: return onResult(emptyMap())
        asked.edit { permissions.forEach { putBoolean(it, true) } }
        lateinit var launcher: ActivityResultLauncher<Array<String>>
        launcher = activity.activityResultRegistry.register("muttaqi.permissions.${requests++}", RequestMultiplePermissions()) { result ->
            launcher.unregister()
            onResult(result)
        }
        launcher.launch(permissions)
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) = follow(activity)

    override fun onActivityResumed(activity: Activity) = follow(activity)

    override fun onActivityDestroyed(activity: Activity) {
        if (this.activity === activity) this.activity = null
    }

    override fun onActivityStarted(activity: Activity) = Unit

    override fun onActivityPaused(activity: Activity) = Unit

    override fun onActivityStopped(activity: Activity) = Unit

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit

    private fun follow(activity: Activity) {
        if (activity is ComponentActivity) this.activity = activity
    }
}
