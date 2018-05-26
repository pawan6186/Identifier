package com.wittgroupinc.identifier

import android.annotation.SuppressLint
import android.content.res.AssetManager
import android.graphics.Bitmap
import android.util.Log

import org.tensorflow.lite.Interpreter
import java.io.*

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.util.ArrayList
import java.util.Comparator
import java.util.PriorityQueue
import kotlin.experimental.and

/**
 * Created by Pawan Gupta on 26/05/18.
 */

class TensorFlowImageClassifier private constructor() : Classifier {

    private var interpreter: Interpreter? = null
    private var inputSize: Int = 0
    private var labelList: List<String> = emptyList()

    override fun recognizeImage(bitmap: Bitmap): List<Classifier.Recognition> {
        val byteBuffer = convertBitmapToByteBuffer(bitmap)
        val result = Array(1, { ByteArray(labelList.size) })
        interpreter?.run(byteBuffer, result)
        return getSortedResult(result)
    }

    override fun close() {
        interpreter?.close()
        interpreter = null
    }

    @Throws(IOException::class)
    private fun loadModelFile(assetManager: AssetManager, modelPath: String): MappedByteBuffer {
        val fileDescriptor = assetManager.openFd(modelPath)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    @Throws(IOException::class)
    private fun loadLabelList(assetManager: AssetManager, labelPath: String): List<String> {
        return assetManager.open(labelPath).bufferedReader().useLines { it.toList() }

    }

    private fun convertBitmapToByteBuffer(bitmap: Bitmap): ByteBuffer {
        val byteBuffer = ByteBuffer.allocateDirect(BATCH_SIZE * inputSize * inputSize * PIXEL_SIZE)
        byteBuffer.order(ByteOrder.nativeOrder())
        val intValues = IntArray(inputSize * inputSize)
        bitmap.getPixels(intValues, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
        var pixel = 0
        for (i in 0 until inputSize) {
            for (j in 0 until inputSize) {
                val `val` = intValues[pixel++]
                byteBuffer.put((`val` shr 16 and 0xFF).toByte())
                byteBuffer.put((`val` shr 8 and 0xFF).toByte())
                byteBuffer.put((`val` and 0xFF).toByte())
            }
        }
        return byteBuffer
    }

    @SuppressLint("DefaultLocale")
    private fun getSortedResult(labelProbArray: Array<ByteArray>): List<Classifier.Recognition> {

        val recognitions = ArrayList<Classifier.Recognition>()
        for (i in 0 until labelList.size){
            Log.d("byte",labelProbArray[0][i].toString())
            val confidence = (labelProbArray[0][i] and 0xff.toByte()) / 255.0f
            if (confidence > THRESHOLD) {
                recognitions.add(Classifier.Recognition(i.toString(), labelList.get(i), confidence))
            }
        }

        return recognitions.sortedBy{ it.confidence }.takeLast(MAX_RESULTS).reversed()
    }

    companion object {

        private val MAX_RESULTS = 3
        private val BATCH_SIZE = 1
        private val PIXEL_SIZE = 3
        private val THRESHOLD = 0.1f

        @Throws(IOException::class)
        internal fun create(assetManager: AssetManager,
                            modelPath: String,
                            labelPath: String,
                            inputSize: Int): Classifier {

            val classifier = TensorFlowImageClassifier()
            classifier.interpreter = Interpreter(classifier.loadModelFile(assetManager, modelPath))
            classifier.labelList = classifier.loadLabelList(assetManager, labelPath)
            classifier.inputSize = inputSize

            return classifier
        }
    }

}
