package com.example.giftem.screens

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.HandlerCompat.postDelayed
import com.example.giftem.R

class SplashActivity : AppCompatActivity() {

    private val SPLASH_TIME: Long = 3000
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

       Handler().postDelayed({
            startActivity( Intent(this,LoginActivity::class.java))
            finish()
        },SPLASH_TIME)

    }
}