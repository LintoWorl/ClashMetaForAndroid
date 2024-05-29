package com.github.kr328.clash

import android.graphics.Color
import androidx.lifecycle.Lifecycle
import com.github.kr328.clash.common.util.intent
import com.github.kr328.clash.design.MainDesignV2
import com.github.kr328.clash.design.R
import com.github.kr328.clash.design.databinding.DesignMainV2Binding

class MainV2Activity : BaseActivity<MainDesignV2>() {

    private var currentIndex = 0
    private val mHomeFragment: HomeFragment by lazy { HomeFragment.newInstance() }
    private val mSubsFragment: UserFragment by lazy { UserFragment.newInstance() }
    private val mUserFragment: UserFragment by lazy { UserFragment.newInstance() }
    private val map by lazy {
        mapOf(Pair(0, "mHomeFragment"), Pair(1, "mSubsFragment"), Pair(2, "mUserFragment"))
    }
    private lateinit var binding: DesignMainV2Binding

    override suspend fun main() {
        val design = MainDesignV2(this)

        setContentDesign(design)

        binding = design.binding
        design.initTabNav { menu ->
            when (menu.itemId) {
                R.id.navigation_home -> {
                    //startActivity(MainActivity::class.intent)
                    currentIndex = 0
                    showFragmentNew()
                }

                R.id.navigation_subs -> {
                    currentIndex = 1
                    showFragmentNew()
                    mSubsFragment.updateView("This is the page of subscription.")
                }

                R.id.navigation_mine -> {
                    currentIndex = 2
                    showFragmentNew()
                    mUserFragment.updateView("Welcome to User Center!")
                }
            }
        }
        showFragmentNew()
    }

    private fun showFragmentNew() {
        val transaction = supportFragmentManager.beginTransaction()
        val tag = map[currentIndex]
        val newFragment = supportFragmentManager.findFragmentByTag(tag) ?: when (currentIndex) {
            0 -> mHomeFragment.apply {
                transaction.replace(R.id.main_container, this, tag)
            }

            1 -> mSubsFragment.apply {
                transaction.replace(R.id.main_container, this, tag)
            }

            else -> mUserFragment.apply {
                transaction.replace(R.id.main_container, this, tag)
            }
        }
        //setTabStyle(currentIndex)
        transaction.setMaxLifecycle(newFragment, Lifecycle.State.RESUMED)
        transaction.show(newFragment).apply {
            map.map { outIt ->
                if (outIt.key != currentIndex) {
                    supportFragmentManager.findFragmentByTag(outIt.value)?.let { this.hide(it) }
                }
            }
        }.commitAllowingStateLoss()
        supportFragmentManager.executePendingTransactions()
    }

}