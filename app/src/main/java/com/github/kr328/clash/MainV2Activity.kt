package com.github.kr328.clash

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.ConnectivityManager.NetworkCallback
import android.net.Network
import android.net.Uri
import android.os.Build
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import app.hw.network.util.NetworkUtil
import com.github.kr328.clash.common.constants.Authorities
import com.github.kr328.clash.common.log.Logger
import com.github.kr328.clash.design.Design
import com.github.kr328.clash.design.R
import com.github.kr328.clash.design.databinding.DesignMainV2Binding
import com.github.kr328.clash.design.dialog.CommonDialog
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainV2Activity : BaseActivity<Design<Any>>() {

    private var currentIndex = 0
    private val mRegisterFragment: RegisterFragment by lazy { RegisterFragment.newInstance() }
    private val mResetPwdFragment: ResetPwdFragment by lazy { ResetPwdFragment.newInstance() }
    private val mLoginFragment: LoginFragment by lazy { LoginFragment.newInstance() }
    private val mHomeFragment: HomeFragment by lazy { HomeFragment.newInstance() }

    private val mSubsFragment: StoreFragment by lazy { StoreFragment.newInstance() }

    //private val mSubsFragment: ProductFragment by lazy { ProductFragment.newInstance() }
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

                MainViewModel.IDX_FRAG_LOGIN -> {
                    if (viewModel.prevFragIdx == MainViewModel.IDX_FRAG_SUBS) {
                        showFragmentByIndex(MainViewModel.IDX_FRAG_SUBS)
                    } else {
                        finish()
                    }
                }

                else -> {
                    finish()
                }
            }
        }
    }

    override fun onServiceRecreated() {
        events.trySend(Event.ServiceRecreated)
    }

    override fun onStarted() {
        events.trySend(Event.ClashStart)
    }

    override fun onStopped(cause: String?) {
        events.trySend(Event.ClashStop)
    }

    override fun onProfileChanged() {
        events.trySend(Event.ProfileLoaded)
    }


    override suspend fun main() {
        binding = DesignMainV2Binding.inflate(layoutInflater)
        onBackPressedDispatcher.addCallback(this, pressBackListener)
        setContentView(binding.root)
        val connMgr = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getSystemService(ConnectivityManager::class.java)
        } else {
            getSystemService(Context.CONNECTIVITY_SERVICE)
        } as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Logger.d("is Network active:${connMgr.isActiveNetworkMetered}")
            connMgr.registerDefaultNetworkCallback(object : NetworkCallback() {
                override fun onAvailable(network: Network) {
                    super.onAvailable(network)
                    Logger.e("Network is available.")
                    init()
                }

                override fun onLost(network: Network) {
                    super.onLost(network)
                    Logger.e("Network is lost.")
                }

                override fun onUnavailable() {
                    super.onUnavailable()
                    Logger.e("Network is unavailable.")
                }
            })
        } else {
            if (!NetworkUtil.isNetConnected(this)) {
                Logger.e("Network connect failed.")
                CommonDialog.show(supportFragmentManager) {
                    title = "提示"
                    content = "网络连接异常，请检查网络设置！"
                    rightButton = "重试"
                    listener = object : CommonDialog.OnClickListener {
                        override fun onPositiveClick(dialog: CommonDialog, clue: String) {
                            if (NetworkUtil.isNetConnected(this@MainV2Activity)) {
                                init()
                                dialog.dismiss()
                            }
                        }

                        override fun onNegativeClick(dialog: CommonDialog) {
                            dialog.dismiss()
                            if (!NetworkUtil.isNetConnected(this@MainV2Activity)) {
                                finish()
                            }
                        }
                    }
                }
            } else {
                init()
            }
        }
    }

    private fun init() {
        launch(Dispatchers.Main) {
            viewModel = ViewModelProvider(this@MainV2Activity)[MainViewModel::class.java]
            viewModel.initConfigs(this@MainV2Activity)
            appStore = AppStore(this@MainV2Activity)

            // 根据登录状态确定初始状态应该跳转到什么页面
            if (appStore.enteredHome) {
                viewModel.userHasLogin = appStore.hasLoginApp
                showFragmentByIndex(MainViewModel.IDX_FRAG_HOME)
            } else {
                showFragmentByIndex(MainViewModel.IDX_FRAG_LOGIN)
            }

            initTabEvents()
            initObserver()
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
            when (it) {
                MainViewModel.IDX_FRAG_HOME ->
                    binding.navigation.selectedItemId = R.id.navigation_home

                else -> showFragmentByIndex(it)
            }
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

    fun setPrivacyTerms(tvTerms: TextView) {
        val tosTitle = getString(com.github.kr328.clash.R.string.privacy_statement_tos)
        val ppTitle = getString(com.github.kr328.clash.R.string.privacy_statement_pp)
        val spannableStringBuilder = SpannableStringBuilder()
        val spannableString1 =
            SpannableString(getString(com.github.kr328.clash.R.string.privacy_statement) + " ")
        spannableStringBuilder.append(spannableString1)
        spannableStringBuilder.append(
            setColorAndLink(1, tosTitle)
        )
        spannableStringBuilder.append(" " + getString(com.github.kr328.clash.R.string.privacy_statement_link) + " ")
        spannableStringBuilder.append(
            setColorAndLink(2, ppTitle)
        )

        //val tvTerms = binding.tvPpTos
        tvTerms.movementMethod = LinkMovementMethod.getInstance()
        tvTerms.highlightColor = Color.TRANSPARENT
        tvTerms.text = spannableStringBuilder
    }

    private fun setColorAndLink(tag: Int, string: String): SpannableString {
        val spannable = SpannableString(string)
        val appStore = AppStore(this)
        spannable.setSpan(object : ClickableSpan() {
            override fun onClick(view: View) {
                if (1 == tag) {
                    //context?.toast("点击了用户协议")
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(appStore.tosAddress)))
                } else {
                    //context?.toast("点击了隐私协议")
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(appStore.ppAddress)))
                }
            }
        }, 0, string.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            spannable.setSpan(
                ForegroundColorSpan(
                    resources.getColor(
                        com.github.kr328.clash.R.color.app_color,
                        null
                    )
                ),
                0, string.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        } else {
            spannable.setSpan(
                ForegroundColorSpan(resources.getColor(com.github.kr328.clash.R.color.app_color)),
                0, string.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        return spannable
    }

}