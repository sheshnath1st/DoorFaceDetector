package com.cleveroad.arfacedetector.ui.screens.main.mlkit.face_detection_heplers

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import com.cleveroad.arfacedetector.ui.screens.main.mlkit.common.GraphicOverlay
import com.cleveroad.arfacedetector.utils.BitmapUtils.rotateBitmap
import com.google.ar.sceneform.math.Vector3
import com.google.firebase.ml.vision.face.FirebaseVisionFace
import com.google.firebase.ml.vision.face.FirebaseVisionFaceLandmark.*

/**
 * Graphic instance for rendering face position, orientation, and landmarks within an associated
 * graphic overlay view.
 */
class FaceGraphic(overlay: GraphicOverlay,
                  private val firebaseVisionFace: FirebaseVisionFace,
                  private val overlayBitmap: Bitmap?,
                  private val cameraFacing: Int) : GraphicOverlay.Graphic(overlay) {

    override fun draw(canvas: Canvas) {
        val leftEye = firebaseVisionFace.getLandmark(LEFT_EYE)?.position ?: return
        val rightEye = firebaseVisionFace.getLandmark(RIGHT_EYE)?.position ?: return
        val mouthBottom = firebaseVisionFace.getLandmark(MOUTH_BOTTOM)?.position ?: return

        val faceHeight = firebaseVisionFace.boundingBox.height() * 1.3F

        val direction =
                Vector3((rightEye.x + leftEye.x) / 2 - mouthBottom.x,
                        (rightEye.y + leftEye.y) / 2 - mouthBottom.y,
                        0F
                ).normalized()
        direction.x *= faceHeight
        direction.y *= faceHeight

        val x = translateX(mouthBottom.x + direction.x)
        val y = translateY(mouthBottom.y + direction.y)

        overlayBitmap?.let {
            rotateBitmap(it, cameraFacing, firebaseVisionFace.headEulerAngleY, firebaseVisionFace.headEulerAngleZ)
        }?.let {
            val imageEdgeSizeBasedOnFaceSizeX = it.width
            val imageEdgeSizeBasedOnFaceSizeY = it.height

            val left = (x - imageEdgeSizeBasedOnFaceSizeX).toInt()
            val top = (y - imageEdgeSizeBasedOnFaceSizeY).toInt()
            val right = (x + imageEdgeSizeBasedOnFaceSizeX).toInt()
            val bottom = (y + imageEdgeSizeBasedOnFaceSizeY).toInt()

            canvas.drawBitmap(it, null, Rect(left, top, right, bottom), null)

        }
    }
}