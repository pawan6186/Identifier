package com.wittgroupinc.identifier

import android.content.Context
import android.view.Gravity
import android.widget.Toast

/**
 * Created by Pawan Gupta on 25/05/18
 */
class Utils {
    companion object {
        fun getPercentage(number: Float): String {
            return String.format("%.1f%%", number * 100.0f)
        }

        fun showToast(context: Context,msg: String) {
            val toast = Toast.makeText(context, msg, Toast.LENGTH_SHORT)
            toast.setGravity(Gravity.CENTER, 0, 0)
            toast.show()
        }
    }


}
