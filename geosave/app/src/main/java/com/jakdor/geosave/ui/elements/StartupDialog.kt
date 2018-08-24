package com.jakdor.geosave.ui.elements

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.ViewGroup
import android.view.Window
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.jakdor.geosave.R
import com.jakdor.geosave.utils.GlideApp
import kotlinx.android.synthetic.main.dialog_first_startup.*

class StartupDialog(context: Context?) : Dialog(context, R.style.FullscreenDialog) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_first_startup)

        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        setCancelable(false)
        setCanceledOnTouchOutside(false)

        GlideApp.with(context)
                .load(R.drawable.placeholder)
                .fitCenter()
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(dialog_startup_image)
    }
}