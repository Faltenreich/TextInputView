package com.faltenreich.inputhintlayout

import android.content.Context
import android.view.View
import android.view.ViewPropertyAnimator

/**
 * Created by Faltenreich on 08.02.2018
 */

class CustomInputHintLayoutKt(context: Context) : InputHintLayout(context) {

    override fun onCreateInAnimation(view: View): ViewPropertyAnimator {
        return super.onCreateInAnimation(view)
    }

    override fun onCreateOutAnimation(view: View): ViewPropertyAnimator {
        return super.onCreateOutAnimation(view)
    }
}