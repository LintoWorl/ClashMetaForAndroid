package com.github.kr328.clash.design.component

import android.content.Context
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.PopupMenu
import com.github.kr328.clash.core.model.ProxySort
import com.github.kr328.clash.core.model.TunnelState
import com.github.kr328.clash.design.HomeDesign
import com.github.kr328.clash.design.ProxyDesign
import com.github.kr328.clash.design.R
import com.github.kr328.clash.design.store.UiStore
import kotlinx.coroutines.channels.Channel

class ProxyModeMenu(
    context: Context,
    menuView: View,
    mode: TunnelState.Mode?,
    private val requests: Channel<HomeDesign.Request>,
) : PopupMenu.OnMenuItemClickListener {
    private val menu = PopupMenu(context, menuView)

    fun show() {
        menu.show()
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        item.isChecked = !item.isChecked

        when (item.itemId) {
            R.id.proxy_mode_global -> {
                requests.trySend(HomeDesign.Request.ProxyGlobal)
            }

            R.id.proxy_mode_rule -> {
                requests.trySend(HomeDesign.Request.ProxyRule)
            }

            else -> return false
        }

        return true
    }

    init {
        menu.menuInflater.inflate(R.menu.menu_proxy_mode, menu.menu)

        menu.menu.apply {
            when (mode) {
                TunnelState.Mode.Global -> findItem(R.id.proxy_mode_global).isChecked = true
                TunnelState.Mode.Rule -> findItem(R.id.proxy_mode_rule).isChecked = true
                else -> {}
            }
        }

        menu.setOnMenuItemClickListener(this)
    }
}
