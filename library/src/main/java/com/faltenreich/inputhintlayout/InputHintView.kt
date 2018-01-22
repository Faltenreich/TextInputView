package com.faltenreich.inputhintlayout

import android.content.Context
import android.util.AttributeSet
import android.widget.TextView

/**
 * Created by Faltenreich on 22.01.2018
 */

internal class InputHintView(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : TextView(context, attrs, defStyleAttr) {

    init {
        isFocusable = true
        isFocusableInTouchMode = true
    }
}