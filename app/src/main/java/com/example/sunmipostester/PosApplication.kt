package com.example.sunmipostester

import android.app.Application
import android.os.Process
import java.util.TimeZone

/** Installs a last-chance uncaught-exception handler and then delegates to Android. */
class PosApplication : Application() {
    companion object {
        lateinit var instance: PosApplication
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        // Keep every app component, formatter and Calendar on Vietnam time (UTC+07:00),
        // independent of the Android device's currently selected timezone.
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"))
        val platformHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, error ->
            runCatching { CrashLogStore.write(applicationContext, thread, error) }
            if (platformHandler != null) {
                platformHandler.uncaughtException(thread, error)
            } else {
                Process.killProcess(Process.myPid())
                kotlin.system.exitProcess(10)
            }
        }
    }
}
