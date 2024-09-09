package com.github.kr328.clash

import android.app.Application
import android.content.Context
import app.hw.network.RetrofitManager
import app.hw.network.api.INetworkBaseInfo
import app.hw.network.contant.Constant.DM_BACKUP
import app.hw.network.contant.Constant.PROTOCOL_HTTPS
import com.github.kr328.clash.common.Global
import com.github.kr328.clash.common.compat.currentProcessName
import com.github.kr328.clash.common.log.Logger
import com.github.kr328.clash.remote.Remote
import com.github.kr328.clash.service.util.sendServiceRecreated
import com.github.kr328.clash.util.clashDir
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

class MainApplication : Application() {
    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)

        Global.init(this)
    }

    override fun onCreate() {
        super.onCreate()

        val processName = currentProcessName
        extractGeoFiles()

        Logger.d("Process $processName started")
        Logger.i("packageName is:$packageName")
        if (processName == packageName) {
            Remote.launch()
            initNetwork()
        } else {
            sendServiceRecreated()
        }
    }

    private fun extractGeoFiles() {
        CoroutineScope(Dispatchers.IO).launch {
            clashDir.mkdirs()

            Logger.i("start extractGeoFiles:${clashDir.absoluteFile}")
            val geoipFile = File(clashDir, "geoip.metadb")
            if (!geoipFile.exists()) {
                val res = FileOutputStream(geoipFile).use {
                    assets.open("geoip.metadb").copyTo(it)
                }
                Logger.d("Copy geoip.metadb: $res")
            }

            val geositeFile = File(clashDir, "geosite.dat")
            if (!geositeFile.exists()) {
                val res = FileOutputStream(geositeFile).use {
                    assets.open("geosite.dat").copyTo(it)
                }
                Logger.d("Copy geosite.dat: $res")
            }
        }
    }

    private fun initNetwork() {
        RetrofitManager.init(object : INetworkBaseInfo {
            override fun getAppContext(): Application {
                return this@MainApplication
            }

            override fun baseServerUrl(): String {
                return "${PROTOCOL_HTTPS}${DM_BACKUP}/api/v1/"
            }

            override fun appVerCode(): String {
                return BuildConfig.VERSION_CODE.toString()
            }

            override fun appVerName(): String {
                return BuildConfig.VERSION_NAME
            }
        })
        Global.commEvents.trySend("network_init_succ")
    }

    fun finalize() {
        Global.destroy()
    }
}