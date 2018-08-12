package com.jakdor.geosave.ui.elements

import android.content.Context
import android.support.v7.widget.CardView
import android.util.AttributeSet
import android.view.View

/**
 * Class extending [CardView] making height always match width
 */
class SquareCardView : CardView {

    constructor(context: Context) : super(context) {}
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {}
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int)
            : super(context, attrs, defStyleAttr) {}

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = View.MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = View.MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = View.MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = View.MeasureSpec.getSize(heightMeasureSpec)

        val size: Int = if (widthMode == View.MeasureSpec.EXACTLY && widthSize > 0) {
            widthSize
        } else if (heightMode == View.MeasureSpec.EXACTLY && heightSize > 0) {
            heightSize
        } else {
            if (widthSize < heightSize) widthSize else heightSize
        }

        val finalMeasureSpec = View.MeasureSpec.makeMeasureSpec(size, View.MeasureSpec.EXACTLY)
        super.onMeasure(finalMeasureSpec, finalMeasureSpec)
    }
}
