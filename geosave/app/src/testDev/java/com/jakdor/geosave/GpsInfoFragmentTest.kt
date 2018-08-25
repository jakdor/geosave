package com.jakdor.geosave

import android.arch.lifecycle.ViewModelProvider
import android.content.ClipboardManager
import android.content.Context
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import com.jakdor.geosave.common.model.UserLocation
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
import java.util.*
import android.widget.Toast
import com.jakdor.geosave.utils.TestApp
import com.jakdor.geosave.utils.TestUtils
import org.robolectric.RuntimeEnvironment
import org.robolectric.shadows.ShadowToast


@RunWith(RobolectricTestRunner::class)
@Config(application = TestApp::class)
class GpsInfoFragmentTest {

    @get:Rule
    private val expectedException = ExpectedException.none()

    private val viewModelFactory = mock<ViewModelProvider.Factory>()
    private val viewModel = mock<GpsInfoViewModel>()

    private val gpsInfoFragment = GpsInfoFragment.newInstance()

    private lateinit var views: Array<View?>

    @Before
    fun setUp(){
        gpsInfoFragment.viewModelFactory = viewModelFactory
        gpsInfoFragment.viewModel = viewModel
        startFragment(gpsInfoFragment)

        gpsInfoFragment.binding.executePendingBindings()

        val view = gpsInfoFragment.view
        views = arrayOf(
                view?.findViewById(R.id.position_info_card),
                view?.findViewById(R.id.altitude_info_card),
                view?.findViewById(R.id.accuracy_info_card),
                view?.findViewById(R.id.speed_info_card),
                view?.findViewById(R.id.bearing_info_card),
                view?.findViewById(R.id.provider_info_card)
        )
    }

    /**
     * Layout inflation test
     */
    @Test
    fun viewTest(){
        val view = gpsInfoFragment.view
        Assert.assertNotNull(view)

        val titles: Array<String> = arrayOf(
                gpsInfoFragment.getString(R.string.position_title),
                gpsInfoFragment.getString(R.string.altitude_title),
                gpsInfoFragment.getString(R.string.accuracy_title),
                gpsInfoFragment.getString(R.string.speed_title),
                gpsInfoFragment.getString(R.string.bearing_title),
                gpsInfoFragment.getString(R.string.provider_title)
        )

        for(card in views){
            Assert.assertNotNull(card)

            val title = card!!.findViewById<TextView>(R.id.title)
            val field = card.findViewById<TextView>(R.id.field)
            val copyButton = card.findViewById<ImageButton>(R.id.copy_button)

            Assert.assertNotNull(title)
            Assert.assertNotNull(field)
            Assert.assertNotNull(copyButton)

            Assert.assertEquals(titles[views.indexOf(card)], title.text)

            if(views.indexOf(card) <= 1){
                Assert.assertEquals(View.VISIBLE, copyButton.visibility)
            } else {
                Assert.assertEquals(View.GONE, copyButton.visibility)
            }
        }
    }

    /**
     * Check layout init state
     */
    @Test
    fun layoutInitTest() {
        gpsInfoFragment.layoutInit()
        gpsInfoFragment.binding.executePendingBindings()

        val unknownStr = gpsInfoFragment.getString(R.string.value_unknown)

        for (view in views) {
            Assert.assertEquals(unknownStr, view?.findViewById<TextView>(R.id.field)?.text)
        }
    }

    /**
     * Check correct view formatting for provided [UserLocation] object
     */
    @Test
    fun handleUserLocationTest() {
        val random = Random()
        val testUserLocation = UserLocation(random.nextDouble(), random.nextDouble(),
                random.nextDouble(), random.nextFloat(), "Fused",
                random.nextFloat(), random.nextFloat())

        gpsInfoFragment.handleUserLocation(testUserLocation)
        gpsInfoFragment.binding.executePendingBindings()

        val pos = String.format(
                Locale.US, "%f, %f", testUserLocation.latitude, testUserLocation.longitude)
        
        var alt: String? = null
        val prov: String
        
        if(testUserLocation.altitude != 0.0){
            alt = String.format("%.2f m", testUserLocation.altitude)
            prov = gpsInfoFragment.getString(R.string.provider_gps)
        } else {
            prov = gpsInfoFragment.getString(R.string.provider_gsm)
        }

        val acc = String.format("%.2f m", testUserLocation.accuracy)
        val speed = String.format("%.2f m/s", testUserLocation.speed)
        val bearing = String.format("%.2f\u00b0", testUserLocation.bearing)

        val expectedStr: Array<String?> = arrayOf(pos, alt, acc, speed, bearing, prov)

        for (view in views) {
            Assert.assertEquals(expectedStr[views.indexOf(view)],
                    view?.findViewById<TextView>(R.id.field)?.text)
        }
    }

    /**
     * Check if string copied to clipboard and Toast displayed
     */
    @Test
    fun handleClipboardCopyTest(){
        val testStr = TestUtils.randomString()

        gpsInfoFragment.handleClipboardCopy(testStr)

        val clipboardManager = RuntimeEnvironment.application.getSystemService(
                Context.CLIPBOARD_SERVICE) as ClipboardManager

        Assert.assertEquals(testStr, clipboardManager.primaryClip.getItemAt(0).text.toString())
        Assert.assertEquals(gpsInfoFragment.getString(R.string.clipboard_toast),
                ShadowToast.getTextOfLatestToast())
        Assert.assertEquals(Toast.LENGTH_SHORT, ShadowToast.getLatestToast().duration)
    }
}