/*
 * GeoSave - app for easy sharing and collaborating on GPS related data
 * Copyright (C) 2018  Jakub Dorda
 *
 * Software under GPLv3 licence - full copyright notice available at:
 * https://github.com/jakdor/geosave/blob/master/README.md
 */

package com.jakdor.geosave.ui.main

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.jakdor.geosave.R
import com.jakdor.geosave.ui.gpsinfo.GpsInfoFragment
import javax.inject.Inject
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import timber.log.Timber
import androidx.core.app.ActivityCompat
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import android.widget.Toast
import android.content.IntentSender
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.provider.Settings
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.location.*
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationCallback
import com.google.firebase.auth.FirebaseAuth
import com.jakdor.geosave.common.model.UserLocation
import com.jakdor.geosave.common.repository.CameraRepository
import com.jakdor.geosave.ui.elements.StartupDialog
import com.jakdor.geosave.ui.locations.LocationsFragment
import com.jakdor.geosave.ui.map.MapFragment
import com.jakdor.geosave.ui.preferences.PreferencesFragment
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.dialog_first_startup.*
import pl.aprilapps.easyphotopicker.DefaultCallback
import pl.aprilapps.easyphotopicker.EasyImage
import java.io.File
import java.lang.ref.WeakReference

class MainActivity : AppCompatActivity(),
        HasSupportFragmentInjector,
        MainContract.MainView,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener{

    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Fragment>

    @Inject
    lateinit var presenter: MainPresenter

    @Inject
    lateinit var googleApiClient: GoogleApiClient

    private lateinit var locationCallbackSafeWrapper: LocationCallbackSafeWrapper
    private lateinit var fallbackLocationManager: LocationManager
    private var fallbackLocationProvider: String? = null

    private lateinit var startupDialog: StartupDialog

    private val fragmentMap: MutableMap<String, Fragment> = mutableMapOf()

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

    override fun supportFragmentInjector(): DispatchingAndroidInjector<Fragment> {
        return dispatchingAndroidInjector
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //restore presenter after screen rotation
        val savedPresenter = lastCustomNonConfigurationInstance
        if(savedPresenter != null){
            presenter = savedPresenter as MainPresenter
            presenter.bindView(this)
        }

        setUp()
    }

    /**
     * Separated from onCreate() for testing - call to super in onCreate() executes dagger injections
     */
    fun setUp(){
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)

        presenter.create()
        googleApiClient.connect()
    }

    override fun onStart() {
        super.onStart()
        presenter.start()
    }

    override fun onPause() {
        super.onPause()
        presenter.pause()
        presenter.unbindView()
    }

    override fun onResume() {
        super.onResume()
        presenter.bindView(this)
        presenter.resume()
    }

    override fun onDestroy() {
        super.onDestroy()
        fragmentMap.clear()
        googleApiClient.unregisterConnectionCallbacks(this)
        googleApiClient.unregisterConnectionFailedListener(this)
    }

    /**
     * Overrider back behaviour when returning from [PreferencesFragment]
     */
    override fun onBackPressed() {
        if(!presenter.switchBackFromPreferenceFragment())
            super.onBackPressed()
    }

    /**
     * Retain presenter instance between screen rotation
     */
    override fun onRetainCustomNonConfigurationInstance(): Any {
        if(::startupDialog.isInitialized && startupDialog.isShowing) startupDialog.dismiss()
        return presenter
    }

    /**
     * Inflate options menu
     */
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_options, menu)
        return true
    }

    /**
     * Handle click on option menu item
     */
    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when(item?.itemId){
            R.id.menu_options_add -> {
                presenter.onAddOptionClicked()
                true
            }
            R.id.menu_options_preferences -> {
                presenter.onPreferencesOptionClicked()
                true
            }
            R.id.menu_options_share -> {
                presenter.onShareOptionClicked()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    /**
     * Create or reattach [GpsInfoFragment]
     */
    override fun switchToGpsInfoFragment() {
        if (!fragmentMap.containsKey(GpsInfoFragment.CLASS_TAG)) {
            fragmentMap[GpsInfoFragment.CLASS_TAG] = GpsInfoFragment.newInstance()
            supportFragmentManager.beginTransaction()
                    .add(R.id.main_fragment_layout, fragmentMap[GpsInfoFragment.CLASS_TAG]!!,
                            GpsInfoFragment.CLASS_TAG)
                    .commit()
            Timber.i("Created %s", GpsInfoFragment.CLASS_TAG)
        }
       
        hideAllFragments()
        
        supportFragmentManager.beginTransaction()
                .show(fragmentMap[GpsInfoFragment.CLASS_TAG]!!)
                .commit()

        supportActionBar?.setTitle(R.string.title_gps_info)
        
        Timber.i("Attached %s", GpsInfoFragment.CLASS_TAG)
    }

    /**
     * Create or reattach [MapFragment]
     */
    override fun switchToMapFragment() {
        if (!fragmentMap.containsKey(MapFragment.CLASS_TAG)) {
            fragmentMap[MapFragment.CLASS_TAG] = MapFragment.newInstance()
            supportFragmentManager.beginTransaction()
                    .add(R.id.main_fragment_layout, fragmentMap[MapFragment.CLASS_TAG]!!,
                            MapFragment.CLASS_TAG)
                    .commit()
            Timber.i("Created %s", MapFragment.CLASS_TAG)
        }

        hideAllFragments()

        supportFragmentManager.beginTransaction()
                .show(fragmentMap[MapFragment.CLASS_TAG]!!)
                .commit()

        supportActionBar?.setTitle(R.string.title_map)

        Timber.i("Attached %s", MapFragment.CLASS_TAG)
    }

    /**
     * Create or reattach [LocationsFragment]
     */
    override fun switchToLocationsFragment() {
        if (!fragmentMap.containsKey(LocationsFragment.CLASS_TAG)) {
            fragmentMap[LocationsFragment.CLASS_TAG] = LocationsFragment.newInstance()
            supportFragmentManager.beginTransaction()
                    .add(R.id.main_fragment_layout, fragmentMap[LocationsFragment.CLASS_TAG]!!,
                            LocationsFragment.CLASS_TAG)
                    .commit()
            Timber.i("Created %s", LocationsFragment.CLASS_TAG)
        }

        hideAllFragments()

        supportFragmentManager.beginTransaction()
                .show(fragmentMap[LocationsFragment.CLASS_TAG]!!)
                .commit()

        supportActionBar?.setTitle(R.string.title_locations_short)

        Timber.i("Attached %s", LocationsFragment.CLASS_TAG)
    }

    /**
     * Create or reattach [PreferencesFragment]
     */
    override fun switchToPreferencesFragment() {
        if (!fragmentMap.containsKey(PreferencesFragment.CLASS_TAG)) {
            fragmentMap[PreferencesFragment.CLASS_TAG] = PreferencesFragment.newInstance()
            supportFragmentManager.beginTransaction()
                    .add(R.id.main_fragment_layout, fragmentMap[PreferencesFragment.CLASS_TAG]!!,
                            PreferencesFragment.CLASS_TAG)
                    .commit()
            Timber.i("Created %s", PreferencesFragment.CLASS_TAG)
        }

        hideAllFragments()

        supportFragmentManager.beginTransaction()
                .show(fragmentMap[PreferencesFragment.CLASS_TAG]!!)
                .commit()

        supportActionBar?.setTitle(R.string.title_preferences)

        Timber.i("Attached %s", PreferencesFragment.CLASS_TAG)
    }

    /**
     * Hide all fragments stored by supportFragmentManager
     */
    private fun hideAllFragments(){
        for(fragment in supportFragmentManager.fragments){
            supportFragmentManager.beginTransaction()
                    .hide(fragment)
                    .commit()
        }
    }

    /**
     * Display toast with provided resource string id
     */
    override fun displayToast(strId: Int) {
        Toast.makeText(this, strId, Toast.LENGTH_LONG).show()
    }

    /**
     * Display [StartupDialog]
     */
    override fun displayFirstStartupDialog() {
        startupDialog = StartupDialog(this)
        startupDialog.show()

        startupDialog.dialog_startup_yes_button.setOnClickListener {
            presenter.onFirstStartupDialogResult(true)
            startupDialog.dismiss()
        }

        startupDialog.dialog_startup_no_button.setOnClickListener {
            presenter.onFirstStartupDialogResult(false)
            startupDialog.dismiss()
        }
    }

    /**
     * Lunch share intent
     */
    override fun shareIntent(text: String) {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share_intent_subject))
        intent.putExtra(Intent.EXTRA_TEXT, text)
        startActivity(intent)
    }

    override fun onConnected(p0: Bundle?) {
        presenter.gmsConnected()
    }

    override fun onConnectionSuspended(p0: Int) {
        presenter.gmsSuspended()
    }

    override fun onConnectionFailed(p0: ConnectionResult) {
        Timber.wtf("GMS failed to connect")
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
                        listPermissionsNeeded.toTypedArray(), REQUEST_ID_LOCATION)
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
        when(requestCode){
            REQUEST_ID_LOCATION -> { //check ACCESS_FINE_LOCATION
                val permissionLocation = ContextCompat.checkSelfPermission(this@MainActivity,
                        Manifest.permission.ACCESS_FINE_LOCATION)
                if (permissionLocation == PackageManager.PERMISSION_GRANTED) {
                    Timber.i("Permissions granted by user")
                    //fix for onRequestPermissionsResult called before onResume
                    presenter.bindView(this)
                    presenter.permissionsGranted(true)
                } else {
                    Timber.e("Permissions declined by user")
                    presenter.bindView(this)
                    presenter.permissionsGranted(false)
                }
            }
            REQUEST_ID_STORAGE -> { //check WRITE_EXTERNAL_STORAGE
                val permissionStorage = ContextCompat.checkSelfPermission(this@MainActivity,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                if (permissionStorage == PackageManager.PERMISSION_GRANTED) {
                    Timber.i("Permissions granted by user")
                    presenter.bindView(this)
                    presenter.cameraPermissionsGranted(true)
                } else {
                    Timber.e("Permissions declined by user")
                    presenter.bindView(this)
                    presenter.cameraPermissionsGranted(false)
                }
            }
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

                locationCallbackSafeWrapper = LocationCallbackSafeWrapper(presenter)

                LocationServices.getFusedLocationProviderClient(this)
                        .requestLocationUpdates(locationRequest, locationCallbackSafeWrapper, null)

                Timber.i("add location callback")

                val builder = LocationSettingsRequest.Builder()
                        .addLocationRequest(locationRequest)
                builder.setAlwaysShow(true)
                val result = LocationServices.getSettingsClient(this).checkLocationSettings(builder.build())

                result.addOnCompleteListener {
                    try {
                        Timber.i("Location updates init")
                        ContextCompat.checkSelfPermission(this@MainActivity,
                                Manifest.permission.ACCESS_FINE_LOCATION)
                        it.getResult(ApiException::class.java)

                        presenter.gmsLocationUpdatesActive()

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
                }
            }
        }
    }

    /**
     * Remove location callback
     */
    override fun stopLocationUpdates() {
        LocationServices.getFusedLocationProviderClient(this)
                .removeLocationUpdates(locationCallbackSafeWrapper)
        Timber.i("Removed location callback")
    }

    /**
     * - Handle GPS auto enable result
     * - Handle Firebase sign-in result
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when(requestCode){
            REQUEST_CHECK_SETTINGS_GPS -> { //GPS auto enable
                when(resultCode){
                    Activity.RESULT_OK -> presenter.gmsGpsEnableDialog(true)
                    Activity.RESULT_CANCELED -> presenter.gmsGpsEnableDialog(false)
                }
            }
            RC_SIGN_IN -> { //Firebase sign-in
                presenter.bindView(this) //workaround for onActivityResult called before resume
                val response = IdpResponse.fromResultIntent(data)
                if (resultCode == RESULT_OK) { // Successfully signed in
                    val user = FirebaseAuth.getInstance().currentUser
                    Timber.i("Firebase sign-in success: %s\n providerId: %s",
                            user?.displayName, user?.providerId)
                    presenter.firebaseSignIn(true)
                } else {
                    Timber.e("Firebase sign-in intent failed with %s code",
                            response?.error?.errorCode)
                    presenter.firebaseSignIn(false)
                }
            }
        }

        EasyImage.handleActivityResult(
                requestCode, resultCode, data, this, object: DefaultCallback() {
            override fun onImagesPicked(p0: MutableList<File>, p1: EasyImage.ImageSource?, p2: Int){
                presenter.onCameraResult(p0.last())
                Timber.i("Got camera request intent result")
            }

            override fun onCanceled(source: EasyImage.ImageSource?, type: Int) {
                super.onCanceled(source, type)
                Timber.i("User canceled photo request")
            }

            override fun onImagePickerError(
                    e: Exception?, source: EasyImage.ImageSource?, type: Int) {
                Timber.e("Photo request error: %s", e.toString())
            }
        })
    }

    /**
     * Fallback native GPS enable dialog listener
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
        if (!fallbackLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Timber.e("GPS turned off")
            val gpsDialogBuilder = AlertDialog.Builder(this)
            gpsDialogBuilder.setMessage(getString(R.string.gps_fallback_dialog_msg))
                    .setPositiveButton(getString(R.string.gps_fallback_dialog_yes), fallbackGpsDialogListener)
                    .setNegativeButton(getString(R.string.gps_fallback_dialog_no), fallbackGpsDialogListener)
                    .show()
        }
    }

    /**
     * GMS connection fail - lunch native GPS settings
     */
    override fun fallbackTurnGpsIntent(){
        Timber.i("Lunching GPS settings")
        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        startActivity(intent)
    }

    /**
     * Fallback native LocationManager location updates
     */
    override fun onLocationChanged(p0: Location?) {
        if(p0 != null) {
            Timber.i(p0.toString())
            presenter.onLocationChanged(UserLocation(p0))
        } else {
            Timber.e("onLocationChanged() location is null")
        }
    }

    override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {
    }

    override fun onProviderEnabled(p0: String?) {
    }

    override fun onProviderDisabled(p0: String?) {
    }

    /**
     * Fallback GMS connection fail - setup native LocationManager
     */
    override fun fallbackLocationManagerSetup() {
        fallbackLocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        fallbackLocationProvider = fallbackLocationManager.getBestProvider(Criteria(), false)
    }

    /**
     * Fallback GMS connection fail - create native location updates
     */
    override fun fallbackStartLocationUpdates() {
        try {
            //location update if min 2m distance
            fallbackLocationManager.requestLocationUpdates(
                    fallbackLocationProvider, 3000, 2.0f, this)
            presenter.fallbackLocationUpdatesActive()
        } catch (e: SecurityException){
            Timber.e("Unauthorised call for location updates request")
        }
    }

    /**
     * Fallback GMS connection fail - stop native location updates
     */
    override fun fallbackStopLocationUpdates() {
        fallbackLocationManager.removeUpdates(this)
    }

    /**
     * Lunch firebase sign-in activity
     */
    override fun firebaseSignInIntent() {
        val providers = listOf(
                AuthUI.IdpConfig.EmailBuilder().build(),
                AuthUI.IdpConfig.GoogleBuilder().build()
        )

        startActivityForResult(AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .setLogo(R.mipmap.ic_splash)
                .build(),
                RC_SIGN_IN)

        AuthUI.getInstance()
    }

    /**
     * Wrapper for GMS [LocationCallback] mitigating memory leak,
     * this is known since version 8.1.0, thx Google
     */
    private class LocationCallbackSafeWrapper
    internal constructor(presenter: MainPresenter): LocationCallback() {
        private val weakRef: WeakReference<MainPresenter> = WeakReference(presenter)

        override fun onLocationResult(locationResult: LocationResult?) {
            for (location in locationResult!!.locations) {
                Timber.i(location.toString())
                if (weakRef.get() != null) {
                    weakRef.get()?.onLocationChanged(UserLocation(location))
                }
            }
        }
    }

    /**
     * Check runtime camera permissions
     */
    override fun checkCameraPermissions() {
        val permissionStorage = ContextCompat.checkSelfPermission(this@MainActivity,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
        val listPermissionsNeeded = ArrayList<String>()
        if (permissionStorage != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            if (!listPermissionsNeeded.isEmpty()) {
                Timber.e("Permissions required, sending request")
                ActivityCompat.requestPermissions(this,
                        listPermissionsNeeded.toTypedArray(), REQUEST_ID_STORAGE)
            }
        } else {
            Timber.i("Permissions already granted")
            presenter.cameraPermissionsGranted(true)
        }
    }

    /**
     * Handle camera request
     */
    override fun cameraRequest(cameraFeature: CameraRepository.CameraFeature) {
        Timber.i("Handling camera request")

        when(cameraFeature){
            CameraRepository.CameraFeature.TAKE_PHOTO -> {
                EasyImage.openCameraForImage(this, 0)
            }
            CameraRepository.CameraFeature.GET_GALLERY -> {
                EasyImage.openGallery(this, 0)
            }
            CameraRepository.CameraFeature.GET_DOCUMENTS -> {
                EasyImage.openDocuments(this, 0)
            }
        }
    }

    companion object {
        private const val REQUEST_CHECK_SETTINGS_GPS=0x1
        private const val REQUEST_ID_LOCATION=0x2
        private const val REQUEST_ID_STORAGE=0x3
        private const val RC_SIGN_IN = 123
    }
}
