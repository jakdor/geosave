package com.jakdor.geosave.ui.main

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
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
import io.fabric.sdk.android.Fabric
import timber.log.Timber
import android.support.v4.app.ActivityCompat
import android.content.pm.PackageManager
import android.support.v4.content.ContextCompat
import android.widget.Toast
import android.content.IntentSender
import android.location.LocationManager
import android.provider.Settings
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.location.*
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationCallback

class MainActivity : DaggerAppCompatActivity(),
        MainContract.MainView,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{

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

    override fun onPause() {
        super.onPause()
        presenter.pause()
    }

    override fun onResume() {
        super.onResume()
        presenter.resume()
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

                LocationServices.getFusedLocationProviderClient(this)
                        .requestLocationUpdates(locationRequest, locationCallback, null)

                Timber.i("add location callback")

                presenter.gmsLocationUpdatesActive()

                val builder = LocationSettingsRequest.Builder()
                        .addLocationRequest(locationRequest)
                builder.setAlwaysShow(true)
                val result = LocationServices.getSettingsClient(this).checkLocationSettings(builder.build())

                result.addOnCompleteListener({
                    try {
                        Timber.i("Location updates init")
                        ContextCompat.checkSelfPermission(this@MainActivity,
                                    Manifest.permission.ACCESS_FINE_LOCATION)
                        it.getResult(ApiException::class.java)
                    } catch (e: ApiException){
                        when (e.statusCode) {
                            LocationSettingsStatusCodes.RESOLUTION_REQUIRED ->
                                try { //Enable GPS automatically
                                    val resolvable = e as ResolvableApiException
                                    resolvable.startResolutionForResult(this@MainActivity,
                                            REQUEST_CHECK_SETTINGS_GPS)
                                } catch (e: IntentSender.SendIntentException) {
                                    Timber.e("User declined GPS enable dialog")
                                }

                            LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                                Timber.e("Unable to turn on GPS automatically")
                                presenter.fallbackGpsAutoEnableFailed()
                            }
                        }
                    } catch (e: Exception) {
                        Timber.wtf(e)
                        presenter.fallbackGpsAutoEnableFailed()
                    }
                })
            }
        }
    }

    /**
     * Remove location callback
     */
    override fun stopLocationUpdates() {
        LocationServices.getFusedLocationProviderClient(this).removeLocationUpdates(locationCallback)
        Timber.i("Removed location callback")
    }

    /**
     * GMS new location callback
     */
    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult?) {
            for (location in locationResult!!.locations) {
                Timber.i(location.toString())
                presenter.gmsLocationChanged()
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
                    Activity.RESULT_OK -> presenter.gmsGpsEnableDialog(true)
                    Activity.RESULT_CANCELED -> presenter.gmsGpsEnableDialog(false)
                }
            }
        }
    }

    /**
     * Fallback GPS enable dialog listener
     */
    private val fallbackGpsDialogListener: DialogInterface.OnClickListener
            = DialogInterface.OnClickListener { _, which ->
        when (which) {
            DialogInterface.BUTTON_POSITIVE -> {
                presenter.fallbackGpsDialogUserResponse(true)
            }
            DialogInterface.BUTTON_NEGATIVE -> {
                presenter.fallbackGpsDialogUserResponse(false)
            }
        }
    }

    /**
     * GMS connection fail - check GPS enabled, handle situation if gps offline
     */
    override fun fallbackCheckGps(){
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Timber.e("GPS turned off")
            val gpsDialogBuilder = AlertDialog.Builder(this)
            gpsDialogBuilder.setMessage(getString(R.string.gps_fallback_dialog_msg))
                    .setPositiveButton(getString(R.string.gps_fallback_dialog_yes), fallbackGpsDialogListener)
                    .setNegativeButton(getString(R.string.gps_fallback_dialog_no), fallbackGpsDialogListener)
                    .show()
        }
    }

    /**
     * GMS connection fail - lunch GPS settings
     */
    override fun fallbackTurnGpsIntent(){
        Timber.i("Lunching GPS settings")
        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        startActivity(intent)
    }

    companion object {
        private const val REQUEST_CHECK_SETTINGS_GPS=0x1
        private const val REQUEST_ID_MULTIPLE_PERMISSIONS=0x2
    }
}
