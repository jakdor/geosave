package com.jakdor.geosave.ui.main

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import com.jakdor.geosave.R
import com.jakdor.geosave.ui.gpsinfo.GpsInfoFragment
import dagger.android.support.DaggerAppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import javax.inject.Inject
import com.crashlytics.android.Crashlytics
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationListener
import io.fabric.sdk.android.Fabric
import timber.log.Timber
import android.support.v4.app.ActivityCompat
import android.content.pm.PackageManager
import android.support.v4.content.ContextCompat
import android.widget.Toast
import com.google.android.gms.location.LocationSettingsStatusCodes
import android.content.IntentSender
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationRequest

class MainActivity : DaggerAppCompatActivity(),
        MainContract.MainView,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener{

    @Inject
    lateinit var presenter: MainPresenter

    @Inject
    lateinit var googleApiClient: GoogleApiClient

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Fabric.with(this, Crashlytics()) //todo move to splash
        setContentView(R.layout.activity_main)

        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)

        presenter.start()
        googleApiClient.connect()
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

    /**
     * Display toast with provided resource string id
     */
    override fun displayToast(strId: Int) {
        Toast.makeText(this, strId, Toast.LENGTH_LONG).show()
    }

    override fun onConnected(p0: Bundle?) {
        presenter.gmsConnected()
    }

    override fun onConnectionSuspended(p0: Int) {
       presenter.gmsSuspended()
    }

    override fun onConnectionFailed(p0: ConnectionResult) {
        presenter.gmsFailed()
    }

    /**
     * Received new location update
     */
    override fun onLocationChanged(p0: Location?) {
        Timber.i(p0.toString())
        presenter.gmsLocationChanged()
    }

    /**
     * Check if app has required permissions, if not call permissions request
     */
    override fun checkPermissions() {
        val permissionLocation = ContextCompat.checkSelfPermission(this@MainActivity,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
        val listPermissionsNeeded = ArrayList<String>()
        if (permissionLocation != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(android.Manifest.permission.ACCESS_FINE_LOCATION)
            if (!listPermissionsNeeded.isEmpty()) {
                Timber.e("Permissions required, sending request")
                ActivityCompat.requestPermissions(this,
                        listPermissionsNeeded.toTypedArray(), REQUEST_ID_MULTIPLE_PERMISSIONS)
            }
        } else {
            Timber.i("Permissions already granted")
            presenter.permissionsGranted(true)
        }
    }

    /**
     * Handle received/declined permissions
     */
    override fun onRequestPermissionsResult(
            requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        val permissionLocation = ContextCompat.checkSelfPermission(this@MainActivity,
                Manifest.permission.ACCESS_FINE_LOCATION)
        if (permissionLocation == PackageManager.PERMISSION_GRANTED) {
            Timber.i("Permissions granted by user")
            presenter.permissionsGranted(true)
        } else {
            Timber.e("Permissions declined by user")
            presenter.permissionsGranted(false)
        }
    }

    /**
     * Setup GPS location updates
     */
    override fun gmsSetupLocationUpdates() {
        if (googleApiClient.isConnected) {
            val permissionLocation = ContextCompat.checkSelfPermission(this@MainActivity,
                    Manifest.permission.ACCESS_FINE_LOCATION)
            if (permissionLocation == PackageManager.PERMISSION_GRANTED) {
                val locationRequest = LocationRequest()
                locationRequest.interval = 3000
                locationRequest.fastestInterval = 3000
                locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                val builder = LocationSettingsRequest.Builder()
                        .addLocationRequest(locationRequest)
                builder.setAlwaysShow(true)
                LocationServices.FusedLocationApi
                        .requestLocationUpdates(googleApiClient, locationRequest, this)
                val result = LocationServices.SettingsApi
                        .checkLocationSettings(googleApiClient, builder.build())
                result.setResultCallback({
                    val status = it.status
                    when (status.statusCode) {
                        LocationSettingsStatusCodes.SUCCESS -> {
                            Timber.i("Location updates init")
                            ContextCompat.checkSelfPermission(this@MainActivity,
                                    Manifest.permission.ACCESS_FINE_LOCATION)
                        }
                        LocationSettingsStatusCodes.RESOLUTION_REQUIRED ->
                            try { //Enable GPS automatically
                                status.startResolutionForResult(this@MainActivity,
                                        REQUEST_CHECK_SETTINGS_GPS)
                            } catch (e: IntentSender.SendIntentException) {
                                Timber.e("User declined GPS enable dialog")
                            }

                        LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                            Timber.e("Unable to turn on GPS automatically")
                        }
                    }
                })
            }
        }
    }

    /**
     * Handle GPS auto enable result
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when(requestCode){
            REQUEST_CHECK_SETTINGS_GPS -> {
                when(resultCode){
                    Activity.RESULT_OK -> presenter.gpsEnableDialog(true)
                    Activity.RESULT_CANCELED -> presenter.gpsEnableDialog(false)
                }
            }
        }
    }

    companion object {
        private const val REQUEST_CHECK_SETTINGS_GPS=0x1
        private const val REQUEST_ID_MULTIPLE_PERMISSIONS=0x2
    }
}
