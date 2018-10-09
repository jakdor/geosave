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
import android.view.ViewGroup
import android.view.Window
import com.jakdor.geosave.R
import com.jakdor.geosave.utils.GlideApp
import kotlinx.android.synthetic.main.dialog_add_image.*

class AddImageDialog(context: Context): Dialog(context, R.style.FullscreenDialog) {

    var previewPicUrl = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_add_image)

        window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        GlideApp.with(context)
                .load(previewPicUrl)
                .placeholder(R.drawable.repo_icon_placeholder)
                .centerCrop()
                .circleCrop()
                .into(dialog_add_image_preview)
    }
}