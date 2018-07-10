package com.jakdor.geosave.ui.gpsinfo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.jakdor.geosave.R
import dagger.android.support.DaggerFragment
import javax.inject.Inject

/**
 * Fragment displaying GPS info
 */
class GpsInfoFragment: DaggerFragment(), GpsInfoContract.GpsInfoView {

    @Inject
    lateinit var presenter: GpsInfoPresenter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        presenter.start()
        return inflater.inflate(R.layout.fragment_gps_info, container, false)
    }

    override fun onPause() {
        super.onPause()
        presenter.pause()
    }

    override fun onResume() {
        super.onResume()
        presenter.resume()
    }

    companion object {

        const val CLASS_TAG = "GpsInfoFragment"

        fun newInstance(): GpsInfoFragment{
            val args = Bundle()
            val fragment = GpsInfoFragment()
            fragment.arguments = args
            return fragment
        }
    }
}