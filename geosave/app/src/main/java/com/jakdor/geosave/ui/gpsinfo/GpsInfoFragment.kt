package com.jakdor.geosave.ui.gpsinfo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_gps_info.*
import com.jakdor.geosave.R
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.gps_info_card.view.*
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

    override fun setPositionTitle(resId: Int) {
        pos_info_card.title.setText(resId)
    }

    override fun setPositionField(posStr: String) {
        pos_info_card.field.text = posStr
    }

    override fun setAltitudeTile(resId: Int) {
        altitude_info_card.title.setText(resId)
    }

    override fun setAltitudeField(altStr: String) {
        altitude_info_card.field.text = altStr
    }

    override fun setAccuracyTitle(resId: Int) {
        accuracy_info_card.title.setText(resId)
    }

    override fun setAccuracyField(accStr: String) {
        accuracy_info_card.field.text = accStr
    }

    override fun setSpeedTitle(resId: Int) {
        speed_info_card.title.setText(resId)
    }

    override fun setSpeedField(speedStr: String) {
        speed_info_card.field.text = speedStr
    }

    override fun setBearingTitle(resId: Int) {
        bearing_info_card.title.setText(resId)
    }

    override fun setBearingField(bearStr: String) {
        bearing_info_card.field.text = bearStr
    }

    override fun setProviderTitle(resId: Int) {
        provider_info_card.title.setText(resId)
    }

    override fun setProviderField(resId: Int) {
        provider_info_card.field.setText(resId)
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