/*
 * GeoSave - app for easy sharing and collaborating on GPS related data
 * Copyright (C) 2018  Jakub Dorda
 *
 * Software under GPLv3 licence - full copyright notice available at:
 * https://github.com/jakdor/geosave/blob/master/README.md
 */

package com.jakdor.geosave.ui.splash

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.crashlytics.android.Crashlytics
import com.jakdor.geosave.ui.main.MainActivity
import io.fabric.sdk.android.Fabric
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.FirebaseFirestore

class SplashActivity: AppCompatActivity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Fabric.with(this, Crashlytics())

        val firestore = FirebaseFirestore.getInstance()
        val settings = FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .setTimestampsInSnapshotsEnabled(true)
                .build()
        firestore.firestoreSettings = settings

        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}