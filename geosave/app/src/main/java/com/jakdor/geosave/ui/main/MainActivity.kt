package com.jakdor.geosave.ui.main

import android.Manifest
import android.app.AlertDialog
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import com.jakdor.geosave.R
import com.jakdor.geosave.ui.gpsinfo.GpsInfoFragment
import dagger.android.support.DaggerAppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import javax.inject.Inject
import com.crashlytics.android.Crashlytics
import com.jakdor.geosave.service.gps.GpsListenerService
import com.jakdor.geosave.service.gps.GpsService
import io.fabric.sdk.android.Fabric
import timber.log.Timber
import android.content.DialogInterface
import android.widget.Toast


class MainActivity : DaggerAppCompatActivity(), MainContract.MainView, ServiceConnection {

    @Inject
    lateinit var presenter: MainPresenter

    private var serviceBound: Boolean = false
    private lateinit var gpsListenerService: GpsListenerService

    private val mOnNavigationItemSelectedListener
            = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_gps_info -> {
                presenter.onGpsInfoTabClicked()
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_map -> {
                presenter.onMapTabClicked()
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_locations -> {
                presenter.onLocationsTabClicked()
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) { //todo rotation handling
        super.onCreate(savedInstanceState)
        Fabric.with(this, Crashlytics()) //todo move to splash
        setContentView(R.layout.activity_main)

        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)

        val intent = Intent(this, GpsService::class.java)
        serviceBound = bindService(intent, this, Context.BIND_AUTO_CREATE)

        presenter.start()
    }

    override fun onStop() {
        super.onStop()
        try {
            if (serviceBound) {
                unbindService(this)
            }
        } catch (e: Exception){
           Timber.e("GpsLocationService: %s", e.message)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.unbindView()
    }

    /**
     * Request location permission
     * The result of the permission request is handled by a callback, onRequestPermissionsResult()
     */
    override fun getLocationPermission() {
        if (ContextCompat.checkSelfPermission(applicationContext,
                        android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            Timber.i("GPS permission already received")
            startGpsListener()
        } else {
            Timber.i("GPS permission request")
            ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    GpsListenerService.PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION)
        }
    }

    /**
     * Handle received permission
     */
    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<out String>,
                                            grantResults: IntArray) {
        if(requestCode == GpsListenerService.PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION){
            //If request is cancelled, the result arrays are empty
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startGpsListener()
                return
            }
        }
        presenter.gpsPermissionStatus(false)
    }

    /**
     * Start [GpsListenerService] operation after receiving permissions
     */
    private fun startGpsListener(){
        gpsListenerService.locationManager =
                getSystemService(Context.LOCATION_SERVICE) as LocationManager
        presenter.gpsPermissionStatus(true)
    }

    var gpsDialogListener: DialogInterface.OnClickListener
            = DialogInterface.OnClickListener { _, which ->
        when (which) {
            DialogInterface.BUTTON_POSITIVE -> {
                presenter.gpsDialogUserResponse(true)
            }
            DialogInterface.BUTTON_NEGATIVE -> {
                presenter.gpsDialogUserResponse(false)
            }
        }
    }

    /**
     * Check GPS enabled, handle situation if gps offline
     */
    override fun checkGps(){
        if (!gpsListenerService.locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Timber.e("GPS turned off")
            val gpsDialogBuilder = AlertDialog.Builder(this)
            gpsDialogBuilder.setMessage(getString(R.string.gps_dialog_msg))
                    .setPositiveButton(getString(R.string.gps_dialog_yes), gpsDialogListener)
                    .setNegativeButton(getString(R.string.gps_dialog_no), gpsDialogListener)
                    .show()
        }
    }

    /**
     * Lunch GPS settings
     */
    override fun turnGpsIntent(){
        Timber.i("Lunching GPS settings")
        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        startActivity(intent)
    }

    override fun gpsDialogNoResponse() {
        Timber.wtf("User is a god damn moron")
        Toast.makeText(this, getString(R.string.gps_dialog_no_toast),
                Toast.LENGTH_LONG).show()
    }

    /**
     * Create or reattach [GpsInfoFragment]
     */
    override fun switchToGpsInfoFragment() {
        if (supportFragmentManager.findFragmentByTag(GpsInfoFragment.CLASS_TAG) == null) {
            supportFragmentManager
                    .beginTransaction()
                    .add(R.id.mainFragmentLayout, //todo check add vs replace
                            GpsInfoFragment.newInstance(), GpsInfoFragment.CLASS_TAG)
                    .commit()
            Timber.i("Created %s", GpsInfoFragment.CLASS_TAG)
        } else {
            supportFragmentManager
                    .beginTransaction()
                    .attach(supportFragmentManager.findFragmentByTag(GpsInfoFragment.CLASS_TAG))
                    .commit()
            Timber.i("Reattached %s", GpsInfoFragment.CLASS_TAG)
        }
    }

    override fun switchToMapFragment() {
    }

    override fun switchToLocationsFragment() {
    }

    override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {
        gpsListenerService = (p1 as GpsService.LocalBinder).service
        presenter.attachService(gpsListenerService)
    }

    override fun onServiceDisconnected(p0: ComponentName?) {
        presenter.detachService()
    }
}
