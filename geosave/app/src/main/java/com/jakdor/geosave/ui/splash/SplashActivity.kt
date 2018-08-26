package com.jakdor.geosave.ui.splash

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.crashlytics.android.Crashlytics
import com.jakdor.geosave.ui.main.MainActivity
import io.fabric.sdk.android.Fabric

class SplashActivity: AppCompatActivity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Fabric.with(this, Crashlytics())

        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}