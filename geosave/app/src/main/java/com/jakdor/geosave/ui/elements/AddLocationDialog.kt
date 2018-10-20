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
import android.view.inputmethod.InputMethodManager
import com.jakdor.geosave.R
import com.jakdor.geosave.utils.GlideApp
import kotlinx.android.synthetic.main.dialog_add_location.*
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Toast

class AddLocationDialog(context: Context) : Dialog(context, R.style.FullscreenDialog) {

    lateinit var cancelButtonOnClickListener: View.OnClickListener
    lateinit var uploadButtonOnClickListener: View.OnClickListener

    private lateinit var indexRepoNamePair: ArrayList<Pair<Int, String>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_add_location)

        window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        dialog_add_location_repo_spinner.isEnabled = false
        dialog_add_location_upload_button.isEnabled = false
        dialog_add_location_spinner_loading_anim.visibility = View.VISIBLE
        animateLoading(dialog_add_location_spinner_loading_anim)

        if (::cancelButtonOnClickListener.isInitialized)
            dialog_add_location_cancel_button.setOnClickListener(cancelButtonOnClickListener)
        if (::uploadButtonOnClickListener.isInitialized)
            dialog_add_location_upload_button.setOnClickListener(uploadButtonOnClickListener)
    }

    /**
     * Load repos spinner with repos names
     */
    fun loadReposSpinner(indexRepoNamePair: ArrayList<Pair<Int, String>>, selectedIndex: Int){
        this.indexRepoNamePair = indexRepoNamePair

        if(!indexRepoNamePair.isEmpty()) {
            val repoNames = mutableListOf<String>()
            indexRepoNamePair.forEach {
                repoNames.add(it.second)
            }

            val spinnerAdapter =
                    ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, repoNames)
            spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            dialog_add_location_repo_spinner.adapter = spinnerAdapter

            for(i in 0 until indexRepoNamePair.size){
                if(indexRepoNamePair[i].first == selectedIndex)
                    dialog_add_location_repo_spinner.setSelection(i)
            }

            dialog_add_location_upload_button.isEnabled = true
        } else {
            dialog_add_location_upload_button.isEnabled = false
            Toast.makeText(context, context.getString(R.string.add_location_no_repos_toast),
                    Toast.LENGTH_LONG).show()
        }

        dialog_add_location_repo_spinner.isEnabled = true
        dialog_add_location_spinner_loading_anim.visibility = View.GONE
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
                hideKeyboard()
                dialog_add_location_loading_anim.visibility = View.VISIBLE
                dialog_add_location_cancel_button.visibility = View.GONE
                animateLoading(dialog_add_location_loading_anim)
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
    private fun animateLoading(view: ImageView) {
        GlideApp.with(context)
                .asGif()
                .load(R.drawable.load)
                .into(view)
    }

    /**
     * Hide soft keyboard
     */
    private fun hideKeyboard(){
        val view = currentFocus
        if(view != null){
            val inputManager =
                    context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputManager.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }
}
