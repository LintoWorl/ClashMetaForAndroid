package com.github.kr328.clash.design

import android.content.Context
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.github.kr328.clash.design.databinding.DesignMainV2Binding
import com.github.kr328.clash.design.util.layoutInflater
import com.github.kr328.clash.design.util.root

class MainDesignV2(context: Context) : Design<MainDesignV2.Request>(context) {

    enum class Request {
        Home,
        Subscribe,
        Mine
    }

    private val binding = DesignMainV2Binding
        .inflate(context.layoutInflater, context.root, false)

    override val root: View
        get() = binding.root


    init {
        binding.self = this
        binding.home = true
        binding.subscribe = true
        binding.userCenter = "Mine"
    }

    fun request(request: Request) {
        requests.trySend(request)
    }

    fun initTabNav(onNavi: (MenuItem) -> Unit) {
        binding.navigation.setOnItemSelectedListener { menu ->
            onNavi(menu)
            return@setOnItemSelectedListener true
        }
    }

    fun updateContent(content: String) {
        binding.message.text = content
        Toast.makeText(context, content, Toast.LENGTH_SHORT).show()
    }
}