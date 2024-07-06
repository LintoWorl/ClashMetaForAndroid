package com.github.kr328.clash

import androidx.lifecycle.Lifecycle
import com.github.kr328.clash.design.MainDesignV2
import com.github.kr328.clash.design.R
import com.github.kr328.clash.design.databinding.DesignMainV2Binding
import com.github.kr328.clash.design.util.hide
import com.github.kr328.clash.design.util.show

class MainV2Activity : BaseActivity<MainDesignV2>() {

    private var currentIndex = 0
    private val mRegisterFragment: RegisterFragment by lazy { RegisterFragment.newInstance() }
    private val mLoginFragment: LoginFragment by lazy { LoginFragment.newInstance() }
    private val mHomeFragment: HomeFragment by lazy { HomeFragment.newInstance() }
    private val mSubsFragment: UserFragment by lazy { UserFragment.newInstance() }
    private val mUserFragment: UserFragment by lazy { UserFragment.newInstance() }
    private val map by lazy {
        mapOf(
            Pair(-1, "mRegisterFragment"),
            Pair(0, "mLoginFragment"),
            Pair(1, "mHomeFragment"),
            Pair(2, "mSubsFragment"),
            Pair(3, "mUserFragment")
        )
    }
    private lateinit var binding: DesignMainV2Binding

    override suspend fun main() {
        val design = MainDesignV2(this)

        setContentDesign(design)

        binding = design.binding
        design.initTabNav { menu ->
            when (menu.itemId) {
                R.id.navigation_home -> {
                    currentIndex = 1
                    showFragmentNew()
                }

                R.id.navigation_subs -> {
                    currentIndex = 2
                    showFragmentNew()
                    mSubsFragment.updateView("This is the page of subscription.")
                }

                R.id.navigation_mine -> {
                    currentIndex = 3
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
            1 -> mHomeFragment.apply {
                transaction.replace(R.id.main_container, this, tag)
                binding.navigation.show()
            }

            2 -> mSubsFragment.apply {
                transaction.replace(R.id.main_container, this, tag)
                binding.navigation.show()
            }

            3 -> mUserFragment.apply {
                transaction.replace(R.id.main_container, this, tag)
                binding.navigation.show()
            }

            0 -> mLoginFragment.apply {
                transaction.replace(R.id.main_container, this, tag)
                binding.navigation.hide()
            }

            else -> mRegisterFragment.apply {
                transaction.replace(R.id.main_container, this, tag)
                binding.navigation.hide()
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