package com.hms.referenceapp.huaweilens.qr.view

import android.content.Context
import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Handler
import android.util.AttributeSet
import android.view.View
import com.hms.referenceapp.huaweilens.R

class ScanningView : View {
    private val paint = Paint()
    private var mPosY = 0
    private var runAnimation = true
    private var showLine = true
    private var handlerInit: Handler? = null
    private var refreshRunnable: Runnable? = null
    private var isGoingDown = true
    private var mHeight = 0
    private val DELAY = 0

    constructor(context: Context?) : super(context) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init()
    }

    private fun init() {
        paint.color = getResources().getColor(R.color.white);
        paint.strokeWidth = 6.0f //make sure add stroke width otherwise line not display
        handlerInit = Handler()
        refreshRunnable = Runnable { refreshView() }
    }

    public override fun onDraw(canvas: Canvas) {
        mHeight = canvas.height
        if (showLine) {
            canvas.drawLine(0f, mPosY.toFloat(), canvas.width.toFloat(), mPosY.toFloat(), paint)
        }
        if (runAnimation) {
            handler!!.postDelayed(refreshRunnable!!, DELAY.toLong())
        }
    }

    fun startAnimation() {
        runAnimation = true
        showLine = true
        this.invalidate()
    }

    fun stopAnimation() {
        runAnimation = false
        showLine = false
        reset()
        this.invalidate()
    }

    private fun reset() {
        mPosY = 0
        isGoingDown = true
    }

    private fun refreshView() {
        //Update new position of the line
        if (isGoingDown) {
            mPosY += 5
            if (mPosY > mHeight) {
                //We invert the direction of the animation
                mPosY = mHeight
                isGoingDown = false
            } else {
                if (mPosY > mHeight)
                mPosY -= 5
                if (mPosY < 0) {
                    //We invert the direction of the animation
                    mPosY = 0
                    isGoingDown = true
                }

            }
        }

        if(mHeight == mPosY)
            mPosY = 0

        this.invalidate()
    }
}