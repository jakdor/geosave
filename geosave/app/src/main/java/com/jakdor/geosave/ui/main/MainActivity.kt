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
import kotlinx.android.synthetic.main.activity_main.*
import javax.inject.Inject
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import timber.log.Timber
import android.support.v4.app.ActivityCompat
import android.content.pm.PackageManager
import android.support.v4.content.ContextCompat
import android.widget.Toast
import android.content.IntentSender
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.provider.Settings
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
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
import com.jakdor.geosave.ui.elements.StartupDialog
import com.jakdor.geosave.ui.map.MapFragment
import com.jakdor.geosave.ui.preferences.PreferencesFragment
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import kotlinx.android.synthetic.main.dialog_first_startup.*

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

    private lateinit var fallbackLocationManager: LocationManager
    private var fallbackLocationProvider: String? = null

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

        if(savedInstanceState != null){
            presenter = savedPresenter
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
        presenter.resume()
        presenter.bindView(this)
    }

    /**
     * Overrider back behaviour when returning from [PreferencesFragment]
     */
    override fun onBackPressed() {
        if(!presenter.switchBackFromPreferenceFragment())
            super.onBackPressed()
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        savedPresenter = presenter
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
            R.id.menu_option_preferences -> {
                presenter.onPreferencesOptionClicked()
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
            Timber.i("Created %s", GpsInfoFragment.CLASS_TAG)
        }
       
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.main_fragment_layout,
                        fragmentMap[GpsInfoFragment.CLASS_TAG], GpsInfoFragment.CLASS_TAG)
                .commit()
        
        Timber.i("Attached %s", GpsInfoFragment.CLASS_TAG)
    }

    /**
     * Create or reattach [MapFragment]
     */
    override fun switchToMapFragment() {
        if (!fragmentMap.containsKey(MapFragment.CLASS_TAG)) {
            fragmentMap[MapFragment.CLASS_TAG] = MapFragment.newInstance()
            Timber.i("Created %s", MapFragment.CLASS_TAG)
        }

        supportFragmentManager
                .beginTransaction()
                .replace(R.id.main_fragment_layout,
                        fragmentMap[MapFragment.CLASS_TAG], MapFragment.CLASS_TAG)
                .commit()

        Timber.i("Attached %s", MapFragment.CLASS_TAG)
    }

    override fun switchToLocationsFragment() {
    }

    /**
     * Create or reattach [PreferencesFragment]
     */
    override fun switchToPreferencesFragment() {
        if (!fragmentMap.containsKey(PreferencesFragment.CLASS_TAG)) {
            fragmentMap[PreferencesFragment.CLASS_TAG] = PreferencesFragment.newInstance()
            Timber.i("Created %s", PreferencesFragment.CLASS_TAG)
        }

        supportFragmentManager
                .beginTransaction()
                .replace(R.id.main_fragment_layout,
                        fragmentMap[PreferencesFragment.CLASS_TAG], PreferencesFragment.CLASS_TAG)
                .commit()

        Timber.i("Attached %s", PreferencesFragment.CLASS_TAG)
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
        val dialog = StartupDialog(this)
        dialog.show()

        dialog.dialog_startup_yes_button.setOnClickListener {
            presenter.onFirstStartupDialogResult(true)
            dialog.dismiss()
        }

        dialog.dialog_startup_no_button.setOnClickListener {
            presenter.onFirstStartupDialogResult(false)
            dialog.dismiss()
        }
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
            presenter.bindView(this) //fix for onRequestPermissionsResult called before onResume
            presenter.permissionsGranted(true)
        } else {
            Timber.e("Permissions declined by user")
            presenter.bindView(this) //fix for onRequestPermissionsResult called before onResume
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
                presenter.onLocationChanged(UserLocation(location))
            }
        }
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
                .setLogo(R.mipmap.ic_launcher_round)
                .build(),
                RC_SIGN_IN)

        AuthUI.getInstance()
    }

    companion object {
        private lateinit var savedPresenter: MainPresenter
        private const val REQUEST_CHECK_SETTINGS_GPS=0x1
        private const val REQUEST_ID_MULTIPLE_PERMISSIONS=0x2
        private const val RC_SIGN_IN = 123
    }
}
