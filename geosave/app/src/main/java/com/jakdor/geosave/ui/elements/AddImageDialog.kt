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
import kotlinx.android.synthetic.main.dialog_add_image.*
import pl.aprilapps.easyphotopicker.EasyImage
import java.io.File

class AddImageDialog(context: Context): Dialog(context, R.style.FullscreenDialog) {

    lateinit var cancelButtonOnClickListener: View.OnClickListener
    lateinit var uploadButtonOnClickListener: View.OnClickListener
    lateinit var cameraButtonOnClickListener: View.OnClickListener
    lateinit var browseButtonOnClickListener: View.OnClickListener
    lateinit var browseFilesButtonClickListener: View.OnClickListener

    var previewPicUrl = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_add_image)

        window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        if(::cancelButtonOnClickListener.isInitialized)
            dialog_add_image_cancel_button.setOnClickListener(cancelButtonOnClickListener)
        if(::uploadButtonOnClickListener.isInitialized)
            dialog_add_image_upload_button.setOnClickListener(uploadButtonOnClickListener)
        if(::cameraButtonOnClickListener.isInitialized)
            dialog_add_image_camera_button.setOnClickListener(cameraButtonOnClickListener)
        if(::browseButtonOnClickListener.isInitialized)
            dialog_add_image_browse_button.setOnClickListener(browseButtonOnClickListener)
        if(::browseFilesButtonClickListener.isInitialized)
            dialog_add_image_browse_files_button.setOnClickListener(browseFilesButtonClickListener)

        //Device has no app that handles gallery intent
        if (!EasyImage.canDeviceHandleGallery(context)) {
            dialog_add_image_browse_button.visibility = View.GONE
        }

        GlideApp.with(context)
                .load(previewPicUrl)
                .placeholder(R.drawable.repo_icon_placeholder)
                .centerCrop()
                .circleCrop()
                .into(dialog_add_image_preview)
    }

    /**
     * Load preview picture into dialog_add_image_preview from file handle
     */
    fun loadPrevievImageView(picFile: File){
        GlideApp.with(context)
                .load(picFile)
                .placeholder(R.drawable.repo_icon_placeholder)
                .centerCrop()
                .circleCrop()
                .into(dialog_add_image_preview)
    }

    /**
     * Handle new dialogLoadingStatus value
     */
    fun dialogLoadingStatus(status: Boolean?){
        if(status != null){
            when(status){
                true -> {
                    setCancelable(false)
                    dialog_add_image_loading_anim.visibility = View.VISIBLE
                    dialog_add_image_cancel_button.visibility = View.GONE
                    animateLoading()
                }
                false -> {
                    setCancelable(true)
                    dialog_add_image_loading_anim.visibility = View.GONE
                    dialog_add_image_cancel_button.visibility = View.VISIBLE
                }
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
                .into(dialog_add_image_loading_anim)
    }
}