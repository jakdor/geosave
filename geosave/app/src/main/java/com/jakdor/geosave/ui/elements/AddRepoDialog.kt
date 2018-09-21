/*
 * GeoSave - app for easy sharing and collaborating on GPS related data
 * Copyright (C) 2018  Jakub Dorda
 *
 * Software under GPLv3 licence - full copyright notice available at:
 * https://github.com/jakdor/geosave/blob/master/README.md
 */

package com.jakdor.geosave.ui.elements

import android.app.Dialog
import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.Observer
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.inputmethod.InputMethodManager
import com.jakdor.geosave.R
import com.jakdor.geosave.common.model.firebase.Repo
import com.jakdor.geosave.ui.locations.ReposBrowserViewModel
import com.jakdor.geosave.utils.GlideApp
import kotlinx.android.synthetic.main.dialog_add_repo.*

class AddRepoDialog(context: Context?,
                    private val lifecycleOwner: LifecycleOwner,
                    private val viewModel: ReposBrowserViewModel?):
        Dialog(context, R.style.FullscreenDialog) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_add_repo)

        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        setCanceledOnTouchOutside(false)

        dialog_add_repo_cancel_button.setOnClickListener { viewModel?.onAddRepoDialogCancelClicked() }
        dialog_add_repo_create_button.setOnClickListener { createNewRepoObj() }
        dialog_add_repo_radio_privacy_private.setOnClickListener {
            dialog_add_repo_privacy_icon.setImageResource(R.drawable.ic_padlock)
        }
        dialog_add_repo_radio_privacy_public.setOnClickListener {
            dialog_add_repo_privacy_icon.setImageResource(R.drawable.ic_padlock_unlock)
        }

        observeDialogLoadingStatus()
        observeDismissDialogRequest()
    }

    /**
     * Observe [ReposBrowserViewModel] dialogLoadingStatus
     */
    fun observeDialogLoadingStatus(){
        viewModel?.dialogLoadingStatus?.observe(lifecycleOwner, Observer {
            handleNewDialogLoadingStatus(it)
        })
    }

    /**
     * Handle new dialogLoadingStatus value
     */
    fun handleNewDialogLoadingStatus(status: Boolean?){
        if(status != null){
            when(status){
                true -> {
                    setCancelable(false)
                    dialog_add_repo_loading_anim.visibility = View.VISIBLE
                    animateLoading()
                }
                false -> {
                    setCancelable(true)
                    dialog_add_repo_loading_anim.visibility = View.GONE
                }
            }
        }
    }

    /**
     * Observe [ReposBrowserViewModel] dialogDismissDialog
     */
    fun observeDismissDialogRequest(){
        viewModel?.dialogDismissRequest?.observe(lifecycleOwner, Observer {
            handleNewDismissDialogRequestValue(it)
        })
    }

    /**
     * Handle new dismissDialogRequest value
     */
    fun handleNewDismissDialogRequestValue(dialogCode: Int?){
        if(dialogCode != null && dialogCode == 0) dismiss()
    }

    /**
     * Create new [Repo] object based on entered data, pass onto [ReposBrowserViewModel]
     */
    fun createNewRepoObj(){
        if(validateInputs()){
            hideKeyboard()
            val visibility = if(dialog_add_repo_radio_privacy_private.isChecked) 0 else 1
            val security = if(dialog_add_repo_radio_security_selected.isChecked) 0 else 1

            val repo = Repo(dialog_add_repo_name.text.toString(),
                    "",
                    dialog_add_repo_info.text.toString(),
                    "",
                    mutableListOf(),
                    visibility,
                    security,
                    mutableListOf())

            viewModel?.createNewRepo(repo)
        }
    }

    /**
     * Validate user input
     */
    fun validateInputs(): Boolean{
        dialog_add_repo_name_error.visibility = View.GONE

        if(dialog_add_repo_name.text.isEmpty()){
            dialog_add_repo_name_error.visibility = View.VISIBLE
            return false
        }
        return true
    }

    /**
     * Load loading gif
     */
    private fun animateLoading() {
        GlideApp.with(context)
                .asGif()
                .load(R.drawable.load)
                .into(dialog_add_repo_loading_anim)
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