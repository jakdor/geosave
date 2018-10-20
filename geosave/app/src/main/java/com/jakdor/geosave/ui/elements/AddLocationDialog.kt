/*
 * GeoSave - app for easy sharing and collaborating on GPS related data
 * Copyright (C) 2018  Jakub Dorda
 *
 * Software under GPLv3 licence - full copyright notice available at:
 * https://github.com/jakdor/geosave/blob/master/README.md
 */

package com.jakdor.geosave.ui.elements

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.Window
import com.jakdor.geosave.R
import com.jakdor.geosave.utils.GlideApp
import kotlinx.android.synthetic.main.dialog_add_location.*
import android.widget.ArrayAdapter

class AddLocationDialog(context: Context) : Dialog(context, R.style.FullscreenDialog) {

    lateinit var cancelButtonOnClickListener: View.OnClickListener
    lateinit var uploadButtonOnClickListener: View.OnClickListener

    private lateinit var indexRepoNamePair: ArrayList<Pair<Int, String>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_add_location)

        window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        if (::cancelButtonOnClickListener.isInitialized)
            dialog_add_location_cancel_button.setOnClickListener(cancelButtonOnClickListener)
        if (::uploadButtonOnClickListener.isInitialized)
            dialog_add_location_upload_button.setOnClickListener(uploadButtonOnClickListener)
    }

    /**
     * Load repos spinner with repos names
     */
    fun loadReposSpinner(indexRepoNamePair: ArrayList<Pair<Int, String>>){
        this.indexRepoNamePair = indexRepoNamePair

        val repoNames = mutableListOf<String>()
        indexRepoNamePair.forEach {
            repoNames.add(it.second)
        }

        val adapter = ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, repoNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        dialog_add_location_repo_spinner.adapter = adapter
    }

    /**
     * Return selected repo index
     */
    fun getSelectedRepoIndex(): Int {
        return indexRepoNamePair[dialog_add_location_repo_spinner.selectedItemPosition].first
    }

    /**
     * Change dialog loading status
     */
    fun dialogLoadingStatus(status: Boolean) {
        when (status) {
            true -> {
                setCancelable(false)
                dialog_add_location_loading_anim.visibility = View.VISIBLE
                dialog_add_location_cancel_button.visibility = View.GONE
                animateLoading()
            }
            false -> {
                setCancelable(true)
                dialog_add_location_loading_anim.visibility = View.GONE
                dialog_add_location_cancel_button.visibility = View.VISIBLE
            }
        }
    }

    /**
     * Load loading gif
     */
    private fun animateLoading() {
        GlideApp.with(context)
                .asGif()
                .load(R.drawable.load)
                .into(dialog_add_location_loading_anim)
    }
}
