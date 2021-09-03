/*
 * Copyright 2020. Explore in HMS. All rights reserved.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 */

/**
 * Copyright 2020. Explore in HMS. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
@file:Suppress("DEPRECATION")
@file:SuppressLint("ExifInterface")
package com.hms.referenceapp.huaweilens.trt.util

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.util.Log
import com.hms.referenceapp.huaweilens.common.utils.GetFilePath
import java.io.IOException
import kotlin.math.roundToInt

object BitmapUtils {
    private const val TAG = "BitmapUtils"

    fun recycleBitmap(vararg bitmaps: Bitmap?) {
        for (bitmap in bitmaps) {
            if (bitmap != null && !bitmap.isRecycled) {
                bitmap.recycle()
                //   bitmap = null
            }
        }
    }

    fun loadFromPath(activity: Activity, uri: Uri, width: Int, height: Int): Bitmap {
        val myPath= GetFilePath()
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        val path = myPath.getPath(activity, uri)
        BitmapFactory.decodeFile(path, options)
        val sampleSize = calculateInSampleSize(options, width, height)
        options.inSampleSize = sampleSize
        options.inJustDecodeBounds = false
        val bitmap = zoomImage(BitmapFactory.decodeFile(path, options), width, height)
        return rotateBitmap(bitmap, getRotationAngle(path))
    }

    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        val width = options.outWidth
        val height = options.outHeight
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            // Calculate height and required height scale.
            val heightRatio = (height.toFloat() / reqHeight.toFloat()).roundToInt()
            // Calculate width and required width scale.
            val widthRatio = (width.toFloat() / reqWidth.toFloat()).roundToInt()
            // Take the larger of the values.
            inSampleSize = if (heightRatio > widthRatio) heightRatio else widthRatio
        }
        return inSampleSize
    }

    // Scale pictures to screen width.
    private fun zoomImage(imageBitmap: Bitmap, targetWidth: Int, maxHeight: Int): Bitmap {
        val scaleFactor =
            (imageBitmap.width.toFloat() / targetWidth.toFloat()).coerceAtLeast(imageBitmap.height.toFloat() / maxHeight.toFloat())
        return Bitmap.createScaledBitmap(
            imageBitmap,
            (imageBitmap.width / scaleFactor).toInt(),
            (imageBitmap.height / scaleFactor).toInt(),
            true
        )
    }

    /**
     * Get the rotation angle of the photo.
     *
     * @param path photo path.
     * @return angle.
     */
    private fun getRotationAngle(path: String?): Int {
        var rotation = 0
        try {
            val exifInterface = ExifInterface(path!!)
            val orientation = exifInterface.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> rotation = 90
                ExifInterface.ORIENTATION_ROTATE_180 -> rotation = 180
                ExifInterface.ORIENTATION_ROTATE_270 -> rotation = 270
            }
        } catch (e: IOException) {
            Log.e(TAG, "Failed to get rotation: " + e.message)
        }
        return rotation
    }

    private fun rotateBitmap(bitmap: Bitmap, angle: Int): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(angle.toFloat())
        var result: Bitmap? = null
        try {
            result = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        } catch (e: OutOfMemoryError) {
            Log.e(TAG, "Failed to rotate bitmap: " + e.message)
        }
        return result ?: bitmap
    }
}
