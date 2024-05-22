package com.github.kr328.clash

import com.github.kr328.clash.common.util.intent
import com.github.kr328.clash.design.MainDesignV2
import com.github.kr328.clash.design.R

class MainV2Activity : BaseActivity<MainDesignV2>() {
    override suspend fun main() {
        val design = MainDesignV2(this)

        setContentDesign(design)

        design.initTabNav { menu ->
            when (menu.itemId) {
                R.id.navigation_home -> {
                    startActivity(MainActivity::class.intent)
                }

                R.id.navigation_subs -> {
                    design.updateContent("Subscribe")
                }

                R.id.navigation_mine -> {
                    design.updateContent("Mine!")
                }
            }
        }
    }
}