package com.github.kr328.clash

import android.annotation.SuppressLint
import android.os.Build
import android.os.Looper
import androidx.activity.OnBackPressedCallback
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import com.github.kr328.clash.common.constants.Authorities
import com.github.kr328.clash.design.Design
import com.github.kr328.clash.design.R
import com.github.kr328.clash.design.databinding.DesignMainV2Binding
import com.github.kr328.clash.design.util.hide
import com.github.kr328.clash.design.util.show
import com.github.kr328.clash.fragment.ChangePwdFragment
import com.github.kr328.clash.fragment.HomeFragment
import com.github.kr328.clash.fragment.LoginFragment
import com.github.kr328.clash.fragment.RegisterFragment
import com.github.kr328.clash.fragment.ResetPwdFragment
import com.github.kr328.clash.fragment.StoreFragment
import com.github.kr328.clash.fragment.UserFragment
import com.github.kr328.clash.store.AppStore
import com.github.kr328.clash.vm.MainViewModel

class MainV2Activity : BaseActivity<Design<Any>>() {

    private var currentIndex = 0
    private val mRegisterFragment: RegisterFragment by lazy { RegisterFragment.newInstance() }
    private val mResetPwdFragment: ResetPwdFragment by lazy { ResetPwdFragment.newInstance() }
    private val mLoginFragment: LoginFragment by lazy { LoginFragment.newInstance() }
    private val mHomeFragment: HomeFragment by lazy { HomeFragment.newInstance() }
    private val mSubsFragment: StoreFragment by lazy { StoreFragment.newInstance() }
    private val mUserFragment: UserFragment by lazy { UserFragment.newInstance() }
    private val mChangePwdFragment: ChangePwdFragment by lazy { ChangePwdFragment.newInstance() }
    private lateinit var viewModel: MainViewModel
    private lateinit var binding: DesignMainV2Binding
    private lateinit var appStore: AppStore

    private val pressBackListener = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            when (currentIndex) {
                MainViewModel.IDX_FRAG_REPWD, MainViewModel.IDX_FRAG_REGST -> {
                    showFragmentByIndex(MainViewModel.IDX_FRAG_LOGIN)
                }

                MainViewModel.IDX_FRAG_XPWD -> {
                    showFragmentByIndex(MainViewModel.IDX_FRAG_USER)
                }

                else -> {
                    finish()
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override suspend fun main() {
        binding = DesignMainV2Binding.inflate(layoutInflater)
        setContentView(binding.root)
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        appStore = AppStore(this)
        onBackPressedDispatcher.addCallback(this, pressBackListener)

        // 根据登录状态确定初始状态应该跳转到什么页面
        if (appStore.enteredHome) {
            showFragmentByIndex(MainViewModel.IDX_FRAG_HOME)
            Looper.getMainLooper().queue.addIdleHandler {
                viewModel.fetchNoticeInfo()
                return@addIdleHandler false
            }
        } else {
            showFragmentByIndex(MainViewModel.IDX_FRAG_LOGIN)
        }

        initTabEvents()
        initObserver()
    }

    private fun initTabEvents() {
        binding.navigation.setOnItemSelectedListener { menu ->
            when (menu.itemId) {
                R.id.navigation_home -> {
                    showFragmentByIndex(MainViewModel.IDX_FRAG_HOME)
                }

                R.id.navigation_subs -> {
                    showFragmentByIndex(MainViewModel.IDX_FRAG_SUBS)
                }

                R.id.navigation_mine -> {
                    showFragmentByIndex(MainViewModel.IDX_FRAG_USER)
                }
            }
            return@setOnItemSelectedListener true
        }
    }

    private fun initObserver() {
        viewModel.fragIndex.observe(this) {
            showFragmentByIndex(it)
        }
    }

    @SuppressLint("CommitTransaction")
    private fun showFragmentByIndex(index: Int) {
        currentIndex = index
        val oldFragment: Fragment? = getCurrentFragment()
        val newFragment: Fragment = getFragmentByIndex(index) ?: return
        if (oldFragment == newFragment && oldFragment.isVisible) {
            return
        }

        // 新的fragment已经显示了
        val transaction = supportFragmentManager.beginTransaction()
        if (oldFragment != null) {
            transaction.hide(oldFragment)
            transaction.setMaxLifecycle(oldFragment, Lifecycle.State.STARTED)
        }
        if (!newFragment.isAdded) {
            transaction.add(R.id.main_container, newFragment, newFragment.javaClass.name)
        }

        transaction.setMaxLifecycle(newFragment, Lifecycle.State.RESUMED)
        transaction.show(newFragment).commitAllowingStateLoss()
        if (index == MainViewModel.IDX_FRAG_HOME) {
            appStore.enteredHome = true
            Authorities.authData = appStore.authData
        }
        //使用此方式在主线程中立即执行事务队列所有事务，同步当前的状态,确保来回快速切换的时候事务不会堆积在队列中异步执行，避免卡顿问题
        supportFragmentManager.executePendingTransactions()
    }

    /**
     * 获取当前的fragment
     */
    private fun getCurrentFragment(): Fragment? {
        if (mRegisterFragment.isVisible) return mRegisterFragment
        if (mResetPwdFragment.isVisible) return mResetPwdFragment
        if (mLoginFragment.isVisible) return mLoginFragment
        if (mHomeFragment.isVisible) return mHomeFragment
        if (mSubsFragment.isVisible) return mSubsFragment
        if (mUserFragment.isVisible) return mUserFragment
        if (mChangePwdFragment.isVisible) return mChangePwdFragment
        return null
    }

    /**
     * 根据index获取fragment,index值取1到4
     */
    private fun getFragmentByIndex(index: Int): Fragment? {
        return when (index) {
            MainViewModel.IDX_FRAG_REGST -> {
                binding.navigation.hide()
                mRegisterFragment
            }

            MainViewModel.IDX_FRAG_REPWD -> {
                binding.navigation.hide()
                mResetPwdFragment
            }

            MainViewModel.IDX_FRAG_LOGIN -> {
                binding.navigation.hide()
                mLoginFragment
            }

            MainViewModel.IDX_FRAG_HOME -> {
                binding.navigation.show()
                mHomeFragment
            }

            MainViewModel.IDX_FRAG_SUBS -> {
                binding.navigation.show()
                mSubsFragment
            }

            MainViewModel.IDX_FRAG_USER -> {
                binding.navigation.show()
                mUserFragment
            }

            MainViewModel.IDX_FRAG_XPWD -> {
                binding.navigation.hide()
                mChangePwdFragment
            }

            else -> null
        }
    }

}