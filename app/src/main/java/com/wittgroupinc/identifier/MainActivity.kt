package com.wittgroupinc.identifier

import android.annotation.TargetApi
import android.graphics.Bitmap
import android.opengl.Visibility
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.support.annotation.ColorRes
import android.support.annotation.RequiresApi
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.AppCompatTextView
import android.text.TextUtils
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView

import com.wonderkiln.camerakit.CameraKitError
import com.wonderkiln.camerakit.CameraKitEvent
import com.wonderkiln.camerakit.CameraKitEventListener
import com.wonderkiln.camerakit.CameraKitImage
import com.wonderkiln.camerakit.CameraKitVideo
import kotlinx.android.synthetic.main.activity_main.*
import java.util.concurrent.Executors
import android.text.SpannableString
import android.text.Spannable
import android.text.style.RelativeSizeSpan
import android.view.Gravity
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast

/**
 * Created by Pawan Gupta on 26/05/18
 */

class MainActivity : AppCompatActivity() {


    private var colors = listOf<@ColorRes Int>(R.color.colorFirstResult, R.color.colorSecondResult, R.color.colorThirdResult)

    private var classifier: Classifier? = null

    private val executor = Executors.newSingleThreadExecutor()

    var countDownTimer = object : CountDownTimer(3500, 500) {


        override fun onTick(millisUntilFinished: Long) {
            timer.text = "${millisUntilFinished / 500}"

        }

        override fun onFinish() {
            timer.visibility = View.INVISIBLE
        }

    }


    @TargetApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        cameraView.addCameraKitListener(object : CameraKitEventListener {
            override fun onEvent(cameraKitEvent: CameraKitEvent) {

            }

            override fun onError(cameraKitError: CameraKitError) {

            }

            override fun onImage(cameraKitImage: CameraKitImage) {
                var bitmap = cameraKitImage.bitmap

                bitmap = Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE, false)

                val results = classifier?.recognizeImage(bitmap)
                if (results?.size!! > 0) {
                    results?.let { showResult(it, bitmap) }
                } else {
                   Utils.showToast(this@MainActivity, "Unable to detect...")
                }


            }

            override fun onVideo(cameraKitVideo: CameraKitVideo) {

            }
        })

        btnToggleCamera.setOnClickListener {
            cameraView.toggleFacing()

        }

        btnDetectObject.setOnClickListener {
            result_layout.visibility = View.INVISIBLE
            cameraView.captureImage()
            timer.visibility = View.VISIBLE


            countDownTimer.start()
            result_detail_layout.removeAllViews()

        }

        initTensorFlowAndLoadModel()
    }

    private fun showResult(results: List<Classifier.Recognition?>, bitmap: Bitmap) {
        imageViewResult.setImageBitmap(bitmap)

        for (i in 0 until results.size) {
            val tv = AppCompatTextView(this, null, R.attr.ResultTextView)
            val params: LinearLayout.LayoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            val margin = resources.getDimensionPixelSize(R.dimen.spacing_small)
            params.weight = 1.0f
            tv.gravity = Gravity.CENTER_VERTICAL
            if (i == 1 || i == 2) {
                params.setMargins(0, margin, 0, 0)
            } else {
                params.setMargins(0, 0, 0, 0)
            }
            tv.layoutParams = params
            val text = "${results?.get(i)?.title?.capitalize()} ${results?.get(i)?.confidence?.let { Utils.getPercentage(it) }}"
            val wordToSpan = SpannableString(text)
            val roundBackgroundSpan = RoundedBackgroundSpan(this, android.R.color.white, colors.get(i))
            wordToSpan.setSpan(roundBackgroundSpan, wordToSpan.length - 5, wordToSpan.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            tv.text = wordToSpan
            result_detail_layout.addView(tv)
        }

        result_layout.visibility = View.VISIBLE
    }

    override fun onResume() {
        super.onResume()
        cameraView.start()
    }

    override fun onPause() {
        cameraView.stop()
        super.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        executor.execute { classifier?.close() }
    }

    private fun initTensorFlowAndLoadModel() {
        executor.execute {
            try {
                classifier = TensorFlowImageClassifier.create(
                        assets,
                        MODEL_PATH,
                        LABEL_PATH,
                        INPUT_SIZE)
                makeButtonVisible()
            } catch (e: Exception) {
                throw RuntimeException("Error initializing TensorFlow!", e)
            }
        }
    }

    private fun makeButtonVisible() {
        runOnUiThread { btnDetectObject.visibility = View.VISIBLE }
    }

    companion object {

        private val MODEL_PATH = "mobilenet_quant_v1_224.tflite"
        private val LABEL_PATH = "labels.txt"
        private val INPUT_SIZE = 224
    }

}
