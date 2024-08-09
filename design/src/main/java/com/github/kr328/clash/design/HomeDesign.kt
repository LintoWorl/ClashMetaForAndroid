package com.github.kr328.clash.design

import android.content.Context
import android.view.View
import app.hw.network.model.NoticeBean
import com.github.kr328.clash.core.model.TunnelState
import com.github.kr328.clash.core.util.trafficTotal
import com.github.kr328.clash.design.adapter.NoticeMsgAdapter
import com.github.kr328.clash.design.component.ProxyModeMenu
import com.github.kr328.clash.design.databinding.DesignHomeBinding
import com.github.kr328.clash.design.util.layoutInflater
import com.github.kr328.clash.design.util.onClickNew
import com.github.kr328.clash.design.util.resolveThemedColor
import com.github.kr328.clash.design.util.root
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class HomeDesign(context: Context) : Design<HomeDesign.Request>(context) {
    enum class Request {
        ToggleStatus,
        OpenProxy,

        //OpenProviders
        ProxyGlobal,
        ProxyRule
    }

    private val binding = DesignHomeBinding
        .inflate(context.layoutInflater, context.root, false)

    override val root: View
        get() = binding.root
    private lateinit var noticeAdapter: NoticeMsgAdapter

    private var proxyMode: TunnelState.Mode? = null
    private val modeMenu: ProxyModeMenu by lazy {
        ProxyModeMenu(context, binding.tvProxyMode, proxyMode, requests)
    }

    suspend fun setProfileName(name: String?) {
        withContext(Dispatchers.Main) {
            binding.profileName = name
        }
    }

    suspend fun setClashRunning(running: Boolean) {
        withContext(Dispatchers.Main) {
            binding.clashRunning = running
        }
    }

    suspend fun setForwarded(value: Long) {
        withContext(Dispatchers.Main) {
            binding.forwarded = value.trafficTotal()
        }
    }

    suspend fun setProxyName(name: String) {
        withContext(Dispatchers.Main) {
            binding.proxyName = name
        }
    }

    suspend fun setMode(mode: TunnelState.Mode) {
        proxyMode = mode
        withContext(Dispatchers.Main) {
            binding.mode = when (mode) {
                TunnelState.Mode.Direct -> context.getString(R.string.direct_mode)
                TunnelState.Mode.Global -> context.getString(R.string.proxy_mode_global)
                TunnelState.Mode.Rule -> context.getString(R.string.proxy_mode_smart)
                else -> context.getString(R.string.proxy_mode_smart)
            }
        }
    }

    suspend fun setHasProviders(has: Boolean) {
        withContext(Dispatchers.Main) {
            binding.hasProviders = has
        }
    }

    init {
        binding.self = this

        binding.colorClashStarted = context.resolveThemedColor(R.attr.colorPrimary)
        binding.colorClashStopped = context.resolveThemedColor(R.attr.colorClashStopped)

        binding.tvProxyMode.onClickNew { modeMenu.show() }
    }

    fun request(request: Request) {
        requests.trySend(request)
    }

    fun initNoticeView(notices: List<NoticeBean>) {
        noticeAdapter = NoticeMsgAdapter(context)
        noticeAdapter.noticeBeans = notices
        binding.bannerNoticeArea.apply {
            setCyclic(true)
            setAutoPlay(true)
            adapter = noticeAdapter
            isNestedScrollingEnabled = false
        }
    }
}