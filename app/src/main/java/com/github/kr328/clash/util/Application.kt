package com.github.kr328.clash.util

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Patterns
import com.github.kr328.clash.BuildConfig
import com.github.kr328.clash.common.util.encode
import java.io.File
import java.math.BigInteger
import java.security.MessageDigest
import java.util.zip.ZipFile
import kotlin.math.pow

object ApplicationObserver {
    private val _createdActivities: MutableSet<Activity> = mutableSetOf()
    private val _visibleActivities: MutableSet<Activity> = mutableSetOf()

    private var visibleChanged: (Boolean) -> Unit = {}

    private var appVisible = false
        private set(value) {
            if (field != value) {
                field = value

                visibleChanged(value)
            }
        }

    val createdActivities: Set<Activity>
        get() = _createdActivities

    private val activityObserver = object : Application.ActivityLifecycleCallbacks {
        @Synchronized
        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
            _createdActivities.add(activity)
        }

        @Synchronized
        override fun onActivityDestroyed(activity: Activity) {
            _createdActivities.remove(activity)
            _visibleActivities.remove(activity)
            appVisible = _visibleActivities.isNotEmpty()
        }

        override fun onActivityStarted(activity: Activity) {
            _visibleActivities.add(activity)
            appVisible = true
        }

        override fun onActivityStopped(activity: Activity) {
            _visibleActivities.remove(activity)
            appVisible = _visibleActivities.isNotEmpty()
        }

        override fun onActivityPaused(activity: Activity) {}
        override fun onActivityResumed(activity: Activity) {}
        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    }

    fun onVisibleChanged(visibleChanged: (Boolean) -> Unit) {
        this.visibleChanged = visibleChanged
    }

    fun attach(application: Application) {
        application.registerActivityLifecycleCallbacks(activityObserver)
    }
}

fun Context.verifyApk(): Boolean {
    return try {
        val info = applicationInfo
        val sources = info.splitSourceDirs ?: arrayOf(info.sourceDir) ?: return false

        val regexNativeLibrary = Regex("lib/(\\S+)/libclash.so")
        val availableAbi = Build.SUPPORTED_ABIS.toSet()
        val apkAbi = sources
            .asSequence()
            .filter { File(it).exists() }
            .flatMap { ZipFile(it).entries().asSequence() }
            .mapNotNull { regexNativeLibrary.matchEntire(it.name) }
            .mapNotNull { it.groups[1]?.value }
            .toSet()

        availableAbi.intersect(apkAbi).isNotEmpty()
    } catch (e: Exception) {
        false
    }
}

fun appFeatSign(key: Int): String {
    val tokenVal = key + (10.0.pow(5.0) + 10.0.pow(4) + 10.0.pow(3)).toInt()+ 100 + 10 + 1
    return encode(tokenVal.toString())
}

private fun getMd5ByByteArray(byteArray: ByteArray): String {
    try {
        val md5 = MessageDigest.getInstance("MD5")
        md5.update(byteArray)
        val bi = BigInteger(1, md5.digest())
        return bi.toString(16)
    } catch (ignore: Exception) {
    }
    return ""
}