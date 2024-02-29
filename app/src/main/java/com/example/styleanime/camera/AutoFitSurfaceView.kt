package com.example.styleanime.camera

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.SurfaceView
import kotlin.math.roundToInt

// Custom SurfaceView for auto-fitting camera preview
class AutoFitSurfaceView(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : SurfaceView(context, attrs, defStyle) {

    // Aspect ratio of the camera frames
    private var aspectRatio = 0f
    private var widthDiff = 0 // Difference in width after adjusting for aspect ratio
    private var heightDiff = 0 // Difference in height after adjusting for aspect ratio
    private var requestLayout = false // Flag to request a layout update

    // Set the aspect ratio based on the camera preview size
    fun setAspectRatio(width: Int, height: Int) {
        require(width > 0 && height > 0) { "Size cannot be negative" }
        aspectRatio = width.toFloat() / height.toFloat()
        requestLayout() // Request a layout update when the aspect ratio changes
    }

    // Measure the view size, considering the aspect ratio for camera frames
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = MeasureSpec.getSize(heightMeasureSpec)
        if (aspectRatio == 0f) {
            setMeasuredDimension(width, height) // Set dimensions directly if no aspect ratio is set
        } else {
            // Performs center-crop transformation of the camera frames based on aspect ratio
            val newWidth: Int
            val newHeight: Int
            if (width < height * aspectRatio) {
                newHeight = height
                newWidth = (height / aspectRatio).roundToInt()
            } else {
                newWidth = width
                newHeight = (width / aspectRatio).roundToInt()
            }

            Log.d(TAG, "Measured dimensions set: $newWidth x $newHeight")
            widthDiff = width - newWidth
            heightDiff = height - newHeight
            requestLayout = true // Set flag to true to request a layout update
            setMeasuredDimension(newWidth, newHeight)
        }
    }

    // Layout the view with adjustments for center-crop based on aspect ratio
    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        if (requestLayout) {
            requestLayout = false
            // Adjust layout with respect to the calculated differences
            layout(
                widthDiff / 2,
                heightDiff / 2,
                right + (widthDiff / 2),
                bottom + (heightDiff / 2)
            )
        }
        super.onLayout(changed, left, top, right, bottom)
    }

    companion object {
        private val TAG = AutoFitSurfaceView::class.java.simpleName
    }
}
