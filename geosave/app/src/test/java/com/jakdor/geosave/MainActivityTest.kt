package com.jakdor.geosave

import android.view.View
import com.google.android.gms.common.api.GoogleApiClient
import com.jakdor.geosave.ui.gpsinfo.GpsInfoFragment
import com.jakdor.geosave.ui.main.MainActivity
import com.jakdor.geosave.ui.main.MainPresenter
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import kotlinx.android.synthetic.main.activity_main.*
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.android.controller.ActivityController

@RunWith(RobolectricTestRunner::class)
class MainActivityTest {

    @get:Rule
    var thrown = ExpectedException.none()

    private val presenter: MainPresenter = mock()
    private val googleApiClient: GoogleApiClient = mock()
    private lateinit var activityController: ActivityController<MainActivity>
    private lateinit var mainActivity: MainActivity

    @Before
    fun setUp(){
        activityController = Robolectric.buildActivity(MainActivity::class.java).create()
        mainActivity = activityController.get()
        mainActivity.presenter = presenter
        mainActivity.googleApiClient = googleApiClient
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
        mainActivity.setUp()
        verify(presenter).start()
        verify(googleApiClient).connect()
    }

    /**
     * Check if [GpsInfoFragment] loaded initially in [MainActivity.main_fragment_layout]
     */
    @Test
    fun defaultFragmentLoadedTest(){
        activityController.visible()

        Assert.assertEquals(GpsInfoFragment::class.java.canonicalName,
                mainActivity.supportFragmentManager.findFragmentById(
                        R.id.main_fragment_layout)::class.java.canonicalName)
    }

}