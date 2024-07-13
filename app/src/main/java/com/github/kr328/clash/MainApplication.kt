package com.github.kr328.clash

import android.app.Application
import android.content.Context
import app.hw.network.RetrofitManager
import app.hw.network.api.INetworkBaseInfo
import com.github.kr328.clash.common.Global
import com.github.kr328.clash.common.compat.currentProcessName
import com.github.kr328.clash.common.log.Log
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

        Log.d("Process $processName started")

        if (processName == packageName) {
            Remote.launch()
            initNetwork(this)
        } else {
            sendServiceRecreated()
        }
    }

    private fun extractGeoFiles() {
        CoroutineScope(Dispatchers.IO).launch {
            clashDir.mkdirs()

            Log.i("start extractGeoFiles:${clashDir.absoluteFile}")
            val geoipFile = File(clashDir, "geoip.metadb")
            if (!geoipFile.exists()) {
                val res = FileOutputStream(geoipFile).use {
                    assets.open("geoip.metadb").copyTo(it)
                }
                Log.d("Copy geoip.metadb: $res")
            }

            val geositeFile = File(clashDir, "geosite.dat")
            if (!geositeFile.exists()) {
                val res = FileOutputStream(geositeFile).use {
                    assets.open("geosite.dat").copyTo(it)
                }
                Log.d("Copy geosite.dat: $res")
            }
        }
    }

    private fun initNetwork(app: Application) {
        RetrofitManager.init(object : INetworkBaseInfo {
            override fun getAppContext(): Application {
                return app
            }

            override fun baseServerUrl(): String {
                return "https://user.fenghuolun.vip/api/v1"
            }

            override fun appVerCode(): String {
                return BuildConfig.VERSION_CODE.toString()
            }

            override fun appVerName(): String {
                return BuildConfig.VERSION_NAME
            }
        })
    }

    fun finalize() {
        Global.destroy()
    }
}