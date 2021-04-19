package com.example.vaultapplication.auth

import android.graphics.Typeface
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.children
import com.example.vaultapplication.databinding.ActivityAuthenticationBinding
import com.google.android.material.tabs.TabLayout


class AuthenticationActivity : AppCompatActivity() {
    private lateinit var authenticationBinding: ActivityAuthenticationBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        authenticationBinding = ActivityAuthenticationBinding.inflate(layoutInflater)
        setContentView(authenticationBinding.root)

        supportActionBar?.hide()

        val signInTab = authenticationBinding.tabLayout.newTab().setText("SIGN IN")
        val signUpTab =  authenticationBinding.tabLayout.newTab().setText("SIGN UP")
        authenticationBinding.tabLayout.addTab(
            signInTab,true
        )
        authenticationBinding.tabLayout.addTab(
           signUpTab
        )

        val authSectionAdapter =
            AuthSectionAdapter(supportFragmentManager, authenticationBinding.tabLayout.tabCount)
        authenticationBinding.authViewPager.adapter = authSectionAdapter

        val selectedTab = authenticationBinding.tabLayout.getTabAt(authenticationBinding.tabLayout.selectedTabPosition)
        selectedTab?.let {
            setStyleForTab(it,Typeface.BOLD) }

        authenticationBinding.tabLayout.addOnTabSelectedListener(object :
            TabLayout.ViewPagerOnTabSelectedListener(authenticationBinding.authViewPager) {

            override fun onTabSelected(tab: TabLayout.Tab) {
                super.onTabSelected(tab)
                tab?.let { setStyleForTab(it,Typeface.BOLD) }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                super.onTabUnselected(tab)
                tab?.let { setStyleForTab(it,Typeface.NORMAL) }
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
                super.onTabReselected(tab)
                tab?.let { setStyleForTab(it,Typeface.BOLD) }
            }


        })



    }
    fun setStyleForTab(tab: TabLayout.Tab, style: Int) {
        tab.view.children.find { it is TextView }?.let { tv ->
            (tv as TextView).post {
                tv.setTypeface(null, style)
            }
        }
    }
}