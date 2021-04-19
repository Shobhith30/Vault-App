package com.example.vaultapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.example.vaultapplication.auth.AuthenticationActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        supportActionBar?.hide()
        openMainScreen()
    }

    private fun openMainScreen() {
        lifecycleScope.launch{
            delay(2000L)
            val intent =  Intent(this@SplashActivity,AuthenticationActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}