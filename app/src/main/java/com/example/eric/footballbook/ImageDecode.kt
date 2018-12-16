package com.example.eric.footballbook

import android.graphics.Bitmap
import org.opencv.android.Utils
import org.opencv.imgproc.Imgproc
import org.opencv.core.Mat

class ImageDecode {

    companion object {
        fun decodeImageToCode(image: Bitmap): String {
            var mat = Mat()
            Utils.bitmapToMat(image, mat)
            println()
            return "1234"
        }
    }

}