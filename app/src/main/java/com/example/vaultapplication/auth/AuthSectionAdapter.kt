package com.example.vaultapplication.auth

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter

class AuthSectionAdapter(private val supportFragmentManager: FragmentManager, private val tabCount: Int) : FragmentStatePagerAdapter(supportFragmentManager,
    BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
    override fun getCount(): Int {
        return tabCount
    }

    override fun getItem(position: Int): Fragment {
        when(position){
            0 -> return LoginFragment()
            1-> return SignUpFragment()
            else -> return LoginFragment()
        }
    }


}
