package com.jakdor.geosave

import android.arch.lifecycle.ViewModelProvider
import com.jakdor.geosave.ui.gpsinfo.GpsInfoFragment
import com.jakdor.geosave.ui.gpsinfo.GpsInfoViewModel
import com.nhaarman.mockito_kotlin.mock
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.support.v4.SupportFragmentTestUtil.startFragment

@RunWith(RobolectricTestRunner::class)
@Config(application = TestApp::class)
class GpsInfoFragmentTest {

    @get:Rule
    private val expectedException = ExpectedException.none()

    private val viewModelFactory = mock<ViewModelProvider.Factory>()
    private val viewModel = mock<GpsInfoViewModel>()

    private val gpsInfoFragment = GpsInfoFragment.newInstance()

    @Before
    fun setUp(){
        gpsInfoFragment.viewModelFactory = viewModelFactory
        gpsInfoFragment.viewModel = viewModel
        startFragment(gpsInfoFragment)
    }

    /**
     * Check layout inflation and init state
     */
    @Test
    fun layoutInitTest(){
        gpsInfoFragment.layoutInit()

        val unknownStr = gpsInfoFragment.getString(R.string.value_unknown)
        Assert.assertEquals(unknownStr, gpsInfoFragment.binding.position)
        Assert.assertEquals(unknownStr, gpsInfoFragment.binding.altitude)
        Assert.assertEquals(unknownStr, gpsInfoFragment.binding.accuracy)
        Assert.assertEquals(unknownStr, gpsInfoFragment.binding.speed)
        Assert.assertEquals(unknownStr, gpsInfoFragment.binding.bearing)
        Assert.assertEquals(unknownStr, gpsInfoFragment.binding.provider)
    }
}