package com.example.styleanime

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.RectF
import android.util.Log
import androidx.exifinterface.media.ExifInterface
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Collection of image reading and manipulation utilities in the form of static functions.
 * TODO: this class should be moved to the common code in the future
 */
abstract class ImageUtils {
    companion object {

        /**
         * Helper function used to convert an EXIF orientation enum into a transformation matrix
         * that can be applied to a bitmap.
         *
         * @param orientation - One of the constants from [ExifInterface]
         */
        private fun decodeExifOrientation(orientation: Int): Matrix {
            val matrix = Matrix()

            // Apply transformation corresponding to declared EXIF orientation
            when (orientation) {
                ExifInterface.ORIENTATION_NORMAL, ExifInterface.ORIENTATION_UNDEFINED -> Unit
                ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90F)
                ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180F)
                ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270F)
                ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.postScale(-1F, 1F)
                ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.postScale(1F, -1F)
                ExifInterface.ORIENTATION_TRANSPOSE -> {
                    matrix.postScale(-1F, 1F)
                    matrix.postRotate(270F)
                }
                ExifInterface.ORIENTATION_TRANSVERSE -> {
                    matrix.postScale(-1F, 1F)
                    matrix.postRotate(90F)
                }

                // Error out if the EXIF orientation is invalid
                else -> throw IllegalArgumentException("Invalid orientation: $orientation")
            }

            // Return the resulting matrix
            return matrix
        }

        /**
         * sets the Exif orientation of an image.
         * this method is used to fix the exit of pictures taken by the camera
         *
         * @param filePath - The image file to change
         * @param value - the orientation of the file
         */
        fun setExifOrientation(
            filePath: String,
            value: String
        ) {
            val exif = ExifInterface(filePath)
            exif.setAttribute(
                ExifInterface.TAG_ORIENTATION, value
            )
            exif.saveAttributes()
        }

        /** Transforms rotation and mirroring information into one of the [ExifInterface] constants */
        fun computeExifOrientation(rotationDegrees: Int, mirrored: Boolean) = when {
            rotationDegrees == 0 && !mirrored -> ExifInterface.ORIENTATION_NORMAL
            rotationDegrees == 0 && mirrored -> ExifInterface.ORIENTATION_FLIP_HORIZONTAL
            rotationDegrees == 180 && !mirrored -> ExifInterface.ORIENTATION_ROTATE_180
            rotationDegrees == 180 && mirrored -> ExifInterface.ORIENTATION_FLIP_VERTICAL
            rotationDegrees == 270 && mirrored -> ExifInterface.ORIENTATION_TRANSVERSE
            rotationDegrees == 90 && !mirrored -> ExifInterface.ORIENTATION_ROTATE_90
            rotationDegrees == 90 && mirrored -> ExifInterface.ORIENTATION_TRANSPOSE
            rotationDegrees == 270 && mirrored -> ExifInterface.ORIENTATION_ROTATE_270
            rotationDegrees == 270 && !mirrored -> ExifInterface.ORIENTATION_TRANSVERSE
            else -> ExifInterface.ORIENTATION_UNDEFINED
        }

        /**
         * Decode a bitmap from a file and apply the transformations described in its EXIF data
         *
         * @param file - The image file to be read using [BitmapFactory.decodeFile]
         */
        fun decodeBitmap(file: File): Bitmap {
            // First, decode EXIF data and retrieve transformation matrix
            val exif = ExifInterface(file.absolutePath)
            val transformation =
                decodeExifOrientation(
                    exif.getAttributeInt(
                        ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_ROTATE_90
                    )
                )

            // Read bitmap using factory methods, and transform it using EXIF data
            val bitmap = BitmapFactory.decodeFile(file.absolutePath)
            return Bitmap.createBitmap(
                BitmapFactory.decodeFile(file.absolutePath),
                0, 0, bitmap.width, bitmap.height, transformation, true
            )
        }

        fun scaleBitmapAndKeepRatio(
            targetBmp: Bitmap,
            reqHeightInPixels: Int,
            reqWidthInPixels: Int
        ): Bitmap {
            if (targetBmp.height == reqHeightInPixels && targetBmp.width == reqWidthInPixels) {
                return targetBmp
            }
            val width = targetBmp.width.coerceAtMost(targetBmp.height)
            val matrix = Matrix()
            matrix.setRectToRect(
                RectF(
                    0f, 0f,
                    width.toFloat(),
                    width.toFloat()
                ),
                RectF(
                    0f, 0f,
                    reqWidthInPixels.toFloat(),
                    reqHeightInPixels.toFloat()
                ),
                Matrix.ScaleToFit.FILL
            )
            Log.d("crop", targetBmp.width.toString() +", "+targetBmp.height.toString());
            return Bitmap.createBitmap(
                targetBmp, 0, 0,
                width,
                width, matrix, true
            )
        }

        fun bitmapToByteBuffer(
            bitmapIn: Bitmap,
            width: Int,
            height: Int,
            mean: Float = 0.0f,
            std: Float = 255.0f
        ): ByteBuffer {
            val bitmap = scaleBitmapAndKeepRatio(bitmapIn, width, height)
            val inputImage = ByteBuffer.allocateDirect(1 * width * height * 3 * 4)
            inputImage.order(ByteOrder.nativeOrder())
            inputImage.rewind()

            val intValues = IntArray(width * height)
            bitmap.getPixels(intValues, 0, width, 0, 0, width, height)
            var pixel = 0
            for (y in 0 until height) {
                for (x in 0 until width) {
                    val value = intValues[pixel++]

                    // Normalize channel values to [-1.0, 1.0]. This requirement varies by
                    // model. For example, some models might require values to be normalized
                    // to the range [0.0, 1.0] instead.
                    inputImage.putFloat(((value shr 16 and 0xFF) - mean) / std)
                    inputImage.putFloat(((value shr 8 and 0xFF) - mean) / std)
                    inputImage.putFloat(((value and 0xFF) - mean) / std)
                }
            }

            inputImage.rewind()
            return inputImage
        }

        fun createEmptyBitmap(imageWidth: Int, imageHeigth: Int, color: Int = 0): Bitmap {
            val ret = Bitmap.createBitmap(imageWidth, imageHeigth, Bitmap.Config.RGB_565)
            if (color != 0) {
                ret.eraseColor(color)
            }
            return ret
        }

        fun loadBitmapFromResources(context: Context, path: String): Bitmap {
            val inputStream = context.assets.open(path)
            return BitmapFactory.decodeStream(inputStream)
        }

        fun convertArrayToBitmap(
            imageArray: Array<Array<Array<FloatArray>>>,
            imageWidth: Int,
            imageHeight: Int
        ): Bitmap {
//      int color = (A & 0xff) << 24 | (B & 0xff) << 16 | (G & 0xff) << 8 | (R & 0xff);
            val conf = Bitmap.Config.ARGB_8888 // see other conf types
            val styledImage = Bitmap.createBitmap(imageWidth, imageHeight, conf)

            for (x in imageArray[0].indices) {
                for (y in imageArray[0][0].indices) {

                    val color = Color.rgb(
                        ((imageArray[0][x][y][0]+1)/2 * 255).toInt(),
                        ((imageArray[0][x][y][1]+1)/2 * 255).toInt(),
                        ((imageArray[0][x][y][2]+1)/2 * 255).toInt()
                    )

                    // this y, x is in the correct order!!!
                    styledImage.setPixel(y, x, color)
                }
            }
            return styledImage
        }
    }
}