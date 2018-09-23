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
import com.crashlytics.android.Crashlytics
import com.jakdor.geosave.ui.main.MainActivity
import io.fabric.sdk.android.Fabric
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.FirebaseFirestore
import com.jakdor.geosave.common.repository.ReposRepository
import com.jakdor.geosave.common.wrapper.FirebaseAuthWrapper
import dagger.android.support.DaggerAppCompatActivity
import javax.inject.Inject

class SplashActivity: DaggerAppCompatActivity(){

    @Inject
    lateinit var reposRepository: ReposRepository

    @Inject
    lateinit var firebaseAuthWrapper: FirebaseAuthWrapper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Fabric.with(this, Crashlytics())

        val firestore = FirebaseFirestore.getInstance()
        val settings = FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .setTimestampsInSnapshotsEnabled(true)
                .build()
        firestore.firestoreSettings = settings

        firebaseAuthWrapper.checkUserObj()
        reposRepository.fastInitialReposLoad()

        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}