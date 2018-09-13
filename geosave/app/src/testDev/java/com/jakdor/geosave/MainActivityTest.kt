/*
 * GeoSave - app for easy sharing and collaborating on GPS related data
 * Copyright (C) 2018  Jakub Dorda
 *
 * Software under GPLv3 licence - full copyright notice available at:
 * https://github.com/jakdor/geosave/blob/master/README.md
 */

package com.jakdor.geosave

import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Toast
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.jakdor.geosave.ui.main.MainActivity
import com.jakdor.geosave.ui.main.MainPresenter
import com.jakdor.geosave.utils.TestApp
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import kotlinx.android.synthetic.main.activity_main.*
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.android.controller.ActivityController
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowToast
import java.util.*

@RunWith(RobolectricTestRunner::class)
@Config(application = TestApp::class)
class MainActivityTest {

    @get:Rule
    var thrown = ExpectedException.none()!!

    private val presenter: MainPresenter = mock()
    private val googleApiClient: GoogleApiClient = mock()
    private lateinit var activityController: ActivityController<MainActivity>
    private lateinit var mainActivity: MainActivity

    @Before
    fun setUp(){
        activityController = Robolectric.buildActivity(MainActivity::class.java)
        mainActivity = activityController.get()
        mainActivity.presenter = presenter
        mainActivity.googleApiClient = googleApiClient
        activityController.setup()
    }

    @Test
    fun setupTest(){
        Assert.assertNotNull(mainActivity)
        Assert.assertEquals(presenter, mainActivity.presenter)
        Assert.assertEquals(googleApiClient, mainActivity.googleApiClient)
    }

    /**
     * Check correct layout inflation
     */
    @Test
    fun viewTest(){
        activityController.visible()
        val view: View = mainActivity.window.decorView
        val content: View = view.findViewById(android.R.id.content)

        Assert.assertNotNull(view)
        Assert.assertNotNull(content)
        Assert.assertNotNull(mainActivity.navigation)
        Assert.assertNotNull(mainActivity.main_fragment_layout)
    }

    /**
     * Test correct setup in onCreate()
     */
    @Test
    fun onCreateTest(){
        verify(presenter).create()
        verify(googleApiClient).connect()
    }

    /**
     * Test activity state relayed to presenter
     */
    @Test
    fun onPauseTest(){
        activityController.pause()
        verify(presenter).pause()
    }

    /**
     * Test activity state relayed to presenter
     */
    @Test
    fun onResumeTest(){
        verify(presenter).resume()
    }

    /**
     * Test correct Toast shown
     */
    @Test
    fun displayToastTest(){
        mainActivity.displayToast(R.string.app_name)

        Assert.assertEquals(mainActivity.getString(R.string.app_name),
                ShadowToast.getTextOfLatestToast())
        Assert.assertEquals(Toast.LENGTH_LONG, ShadowToast.getLatestToast().duration)
    }

    /**
     * Test event relayed to presenter
     */
    @Test
    fun onConnectedTest(){
        mainActivity.onConnected(Bundle())

        verify(presenter).gmsConnected()
    }

    /**
     * Test event relayed to presenter
     */
    @Test
    fun onConnectionSuspendedTest(){
        mainActivity.onConnectionSuspended(Random().nextInt())

        verify(presenter).gmsSuspended()
    }

    /**
     * Test event relayed to presenter
     */
    @Test
    fun onConnectionFailedTest(){
        mainActivity.onConnectionFailed(ConnectionResult(Random().nextInt(21)+1))

        verify(presenter).gmsFailed()
    }

    /**
     * Check correct intent lunched
     */
    @Test
    fun fallbackTurnGpsIntentTest(){
        mainActivity.fallbackTurnGpsIntent()

        val expectedIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        Assert.assertEquals(expectedIntent.toString(),
                shadowOf(mainActivity).nextStartedActivity.toString())
    }

    /**
     * Test native location provider location changed callback for non-null Location object
     */
    @Test
    fun onLocationChangedTest(){
        mainActivity.onLocationChanged(Location("dummyprovider"))

        verify(presenter).onLocationChanged(any())
    }

    /**
     * Test native location provider location changed callback for null Location object
     */
    @Test
    fun onLocationChangedNullLocationTest(){
        mainActivity.onLocationChanged(null)

        verify(presenter, Mockito.never()).onLocationChanged(any())
    }
}