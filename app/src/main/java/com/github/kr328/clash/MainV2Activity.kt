package com.github.kr328.clash

import android.annotation.SuppressLint
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import com.github.kr328.clash.common.Global
import com.github.kr328.clash.core.model.FetchStatus
import com.github.kr328.clash.design.Design
import com.github.kr328.clash.design.R
import com.github.kr328.clash.design.databinding.DesignMainV2Binding
import com.github.kr328.clash.design.dialog.ModelProgressBarConfigure
import com.github.kr328.clash.design.dialog.withModelProgressBar
import com.github.kr328.clash.design.util.hide
import com.github.kr328.clash.design.util.show
import com.github.kr328.clash.fragment.HomeFragment
import com.github.kr328.clash.fragment.LoginFragment
import com.github.kr328.clash.fragment.ProductFragment
import com.github.kr328.clash.fragment.RegisterFragment
import com.github.kr328.clash.fragment.ResetPwdFragment
import com.github.kr328.clash.fragment.UserFragment
import com.github.kr328.clash.service.model.Profile
import com.github.kr328.clash.store.AppStore
import com.github.kr328.clash.util.subsUrl
import com.github.kr328.clash.util.withProfile
import com.github.kr328.clash.vm.MainViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.select
import java.util.*

class MainV2Activity : BaseActivity<Design<Any>>() {

    private var currentIndex = 0
    private val mRegisterFragment: RegisterFragment by lazy { RegisterFragment.newInstance() }
    private val mResetPwdFragment: ResetPwdFragment by lazy { ResetPwdFragment.newInstance() }
    private val mLoginFragment: LoginFragment by lazy { LoginFragment.newInstance() }
    private val mHomeFragment: HomeFragment by lazy { HomeFragment.newInstance() }
    private val mSubsFragment: ProductFragment by lazy { ProductFragment.newInstance() }
    private val mUserFragment: UserFragment by lazy { UserFragment.newInstance() }
    private lateinit var viewModel: MainViewModel
    private lateinit var binding: DesignMainV2Binding
    private lateinit var appStore: AppStore

    override suspend fun main() {
        binding = DesignMainV2Binding.inflate(layoutInflater)
        setContentView(binding.root)
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]

        appStore = AppStore(this)
        fetchProfile()
        viewModel.initConfigs(this)

        // 根据登录状态确定初始状态应该跳转到什么页面
        if (appStore.enteredHome) {
            showFragmentByIndex(MainViewModel.IDX_FRAG_HOME)
        } else {
            showFragmentByIndex(MainViewModel.IDX_FRAG_LOGIN)
        }
        initTabEvents()
        initObserver()
    }

    private suspend fun fetchProfile() {
        withProfile {
            val savedProf = queryActive()
            if (savedProf == null) {
                val name = getString(R.string.new_profile)
                //val name = "default_profile"
                val uuid: UUID = create(Profile.Type.Url, name)

                val originProf = queryByUUID(uuid) ?: return@withProfile
                val profile = originProf.copy(source = subsUrl)
                load(profile)
                defer {
                    release(uuid)
                }
            } else {
                update(savedProf.uuid)
            }
            //delay(3000)
        }
    }

    private fun load(profile: Profile) {
        try {
            withProcessing { updateStatus ->
                withProfile {
                    patch(profile.uuid, profile.name, profile.source, profile.interval)

                    coroutineScope {
                        commit(profile.uuid) {
                            launch {
                                updateStatus(it)
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun withProcessing(executeTask: suspend (suspend (FetchStatus) -> Unit) -> Unit) {
        try {
            launch(Dispatchers.Main) {
                withModelProgressBar {
                    configure {
                        isIndeterminate = true
                        text = getString(R.string.initializing)
                    }

                    executeTask {
                        configure {
                            applyFrom(it)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun ModelProgressBarConfigure.applyFrom(status: FetchStatus) {
        when (status.action) {
            FetchStatus.Action.FetchConfiguration -> {
                text = getString(R.string.format_fetching_configuration, status.args[0])
                isIndeterminate = true
            }

            FetchStatus.Action.Verifying -> {
                text = getString(R.string.verifying)
                isIndeterminate = false
                max = status.max
                progress = status.progress
            }

            else -> {}
        }
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

            else -> null
        }
    }

}