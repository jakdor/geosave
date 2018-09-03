package com.jakdor.geosave.ui.gpsinfo

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.jakdor.geosave.R
import com.jakdor.geosave.di.InjectableFragment
import com.jakdor.geosave.common.model.UserLocation
import com.jakdor.geosave.databinding.FragmentGpsInfoBinding
import javax.inject.Inject
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import com.jakdor.geosave.common.repository.LocationConverter
import com.jakdor.geosave.common.repository.SharedPreferencesRepository

/**
 * Fragment displaying GPS info
 */
class GpsInfoFragment: Fragment(), InjectableFragment {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    var viewModel: GpsInfoViewModel? = null
    lateinit var binding: FragmentGpsInfoBinding

    private var preferencesMap: Map<String, Int>? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(
                inflater, R.layout.fragment_gps_info, container, false)
        layoutInit()
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        if(viewModel == null){
            viewModel = ViewModelProviders.of(this, viewModelFactory)
                    .get(GpsInfoViewModel::class.java)
        }

        binding.viewModel = viewModel
        viewModel?.requestUserLocationUpdates()
        observeUserLocation()
        observeClipboardCopyQueue()
        observePreferencesMap()
    }

    override fun onResume() {
        super.onResume()
        viewModel?.requestPreferencesUpdate()
    }

    /**
     * Set initial value for fields (can't be set in xml for some reason)
     */
    fun layoutInit(){
        binding.position = getString(R.string.value_unknown)
        binding.altitude = getString(R.string.value_unknown)
        binding.accuracy = getString(R.string.value_unknown)
        binding.speed = getString(R.string.value_unknown)
        binding.bearing = getString(R.string.value_unknown)
        binding.provider = getString(R.string.value_unknown)
    }

    /**
     * Observe [GpsInfoViewModel] for updates on location [MutableLiveData] stream
     */
    fun observeUserLocation(){
        viewModel?.location?.observe(this, Observer {
            if(it != null && preferencesMap != null) handleUserLocation(it, preferencesMap!!)
        })
    }

    /**
     * Handle new [UserLocation] object
     * Pass formatted String variables instead of model to layout for better testability
     */
    fun handleUserLocation(location: UserLocation, preferences: Map<String, Int>){

        //location
        when(preferences[SharedPreferencesRepository.locationUnits]){
            0 -> { //decimal
                binding.position =
                        LocationConverter.decimalFormat(location.latitude, location.longitude)
            }
            1 -> { //sexigesimal
                binding.position = LocationConverter.dmsFormat(location.latitude, location.longitude)
            }
            2 -> { //decimal degrees
                binding.position =
                        LocationConverter.decimalDegreesFormat(location.latitude, location.longitude)
            }
            3 -> { //degrees decimal minutes
                binding.position =
                        LocationConverter.dmFormat(location.latitude, location.longitude)
            }
        }

        //altitude
        if(location.altitude != -999.0){
            when(preferences[SharedPreferencesRepository.altUnits]) {
                0 -> { //meters
                    binding.altitude = String.format("%.2f m", location.altitude)
                }
                1 -> { //kilometers
                    binding.altitude = String.format("%.4f km", location.altitude / 1000.0)
                }
                2 -> { //feats
                    binding.altitude = String.format("%.2f ft", location.altitude * 3.2808399)
                }
                3 -> { //land miles
                    binding.altitude = String.format("%.6f mi", location.altitude * 0.000621371192)
                }
                4 -> { //nautical miles
                    binding.altitude = String.format("%.6f nmi", location.altitude * 0.000539956803)
                }
            }
            binding.provider = getString(R.string.provider_gps)
        } else {
            binding.provider = getString(R.string.provider_gsm)
        }

        //accuracy
        when(preferences[SharedPreferencesRepository.accUnits]){
            0 -> { //meters
                binding.accuracy = String.format("%.2f m", location.accuracy)
            }
            1 -> { //kilometers
                binding.accuracy = String.format("%.4f km", location.accuracy / 1000.0)
            }
            2 -> { //feats
                binding.accuracy = String.format("%.2f ft", location.accuracy * 3.2808399)
            }
            3 -> { //land miles
                binding.accuracy = String.format("%.6f mi", location.accuracy * 0.000621371192)
            }
            4 -> { //nautical miles
                binding.accuracy = String.format("%.6f nmi", location.accuracy * 0.000539956803)
            }
        }

        //speed
        when(preferences[SharedPreferencesRepository.speedUnits]){
            0 -> { //m/s
                binding.speed = String.format("%.2f m/s", location.speed)
            }
            1 -> { //km/h
                binding.speed = String.format("%.2f km/h", location.speed * 3.6)
            }
            2 -> { //ft/s
                binding.speed = String.format("%.2f ft/s", location.speed * 3.2808399)
            }
            3 -> { //mph
                binding.speed = String.format("%.2f mph", location.speed * 2.23693629)
            }
        }

        //bearing
        binding.bearing = String.format("%.2f\u00b0", location.bearing)
    }

    /**
     * Observe [GpsInfoViewModel] for updates on clipboardCopyQueue [MutableLiveData] stream
     */
    fun observeClipboardCopyQueue(){
        viewModel?.clipboardCopyQueue?.observe(this, Observer {
           handleClipboardCopy(it)
        })
    }

    /**
     * Copy to clipboard string received from [GpsInfoViewModel] after user click on copy button
     * Set null value to clipboardCopyQueue [MutableLiveData] to prevent re-handling after
     * screen rotation
     */
    fun handleClipboardCopy(text: String?){
        if(text != null) {
            val clipboard = activity?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText(getString(R.string.clipboard_label), text)
            clipboard.primaryClip = clip

            Toast.makeText(activity, getString(R.string.clipboard_toast), Toast.LENGTH_SHORT).show()

            viewModel?.clipboardCopyQueue?.value = null
        }
    }

    /**
     * Observe [GpsInfoViewModel] preferences [MutableLiveData] stream for updates on user preferences
     */
    fun observePreferencesMap(){
        viewModel?.preferences?.observe(this, Observer {
            handlePreferencesMap(it)
        })
    }

    /**
     * Store preferences update values locally
     */
    fun handlePreferencesMap(map: MutableMap<String, Int>?){
        if(map != null){
            this.preferencesMap = map
        }
    }

    companion object {

        const val CLASS_TAG = "GpsInfoFragment"

        fun newInstance(): GpsInfoFragment{
            val args = Bundle()
            val fragment = GpsInfoFragment()
            fragment.arguments = args
            fragment.retainInstance = true
            return fragment
        }
    }
}