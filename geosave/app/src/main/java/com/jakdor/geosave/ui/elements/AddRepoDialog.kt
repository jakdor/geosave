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
import android.view.ViewGroup
import android.view.Window
import com.jakdor.geosave.R
import com.jakdor.geosave.ui.locations.ReposBrowserViewModel
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
        dialog_add_repo_create_button.setOnClickListener { viewModel?.onAddRepoDialogCreateClicked() }

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
                true -> setCancelable(false)
                false -> setCancelable(true)
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
}