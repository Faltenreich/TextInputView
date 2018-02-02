package com.faltenreich.inputhintlayout

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.Context
import android.os.Build
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.view.animation.Interpolator
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView

/**
 * Created by Faltenreich on 22.01.2018
 */

internal fun Any.tag(): String = javaClass.simpleName

internal fun Context.accentColor(): Int {
    val attr =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) android.R.attr.colorAccent
            else resources.getIdentifier("colorAccent", "attr", packageName)
    val outValue = TypedValue()
    theme.resolveAttribute(attr, outValue, true)
    return outValue.data
}

internal fun ViewGroup.views(): List<View> = (0 until childCount).map { getChildAt(it) }

internal fun TextView.setTextColor(colorTo: Int, durationMillis: Long, interpolator: Interpolator) {
    if (durationMillis > 0) {
        val colorFrom = textColors.defaultColor
        val animation = ValueAnimator.ofObject(ArgbEvaluator(), colorFrom, colorTo)
        animation.duration = durationMillis
        animation.interpolator = interpolator
        animation.addUpdateListener { animator -> setTextColor(animator.animatedValue as Int) }
        animation.start()
    } else {
        setTextColor(colorTo)
    }
}

internal fun View.setLayoutGravity(gravity: Int) {
    val newParams = layoutParams
    (newParams as? FrameLayout.LayoutParams)?.gravity = gravity
    (newParams as? LinearLayout.LayoutParams)?.gravity = gravity
    layoutParams = newParams
}

internal fun EditText.getTextWidth(line: Int = -1): Float {
    val input = if (line >= 0) getTextForLine(line) else text?.toString()
    return input?.let { paint.measureText(input) } ?: 0f
}

internal fun EditText.getTextForLine(line: Int): String? =
        layout?.let {
            val input = text?.toString()
            val lineCharacters = input?.length ?: 0
            val start = it.getLineStart(line)
            val end = it.getLineEnd(line)
            val isInBounds = start < end && start < lineCharacters && end <= lineCharacters
            if (isInBounds) input?.substring(start, end) else null
        }

internal fun EditText.getMaxLineCountCompat(): Int =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) maxLines else getMaxLineCountPreApi16()

internal fun EditText.getMaxLineCountPreApi16(): Int =
        try {
            val maximumField = text.javaClass.getDeclaredField("mMaximum")
            val maxModeField = text.javaClass.getDeclaredField("mMaxMode")
            maximumField?.isAccessible = true
            maxModeField?.isAccessible = true
            val maximum = maximumField?.getInt(text)
            val maxMode = maxModeField?.getInt(text)
            if (maxMode == 1) 1 else maximum ?: 1
        } catch (exception: Exception) {
            Log.e(tag(), exception.message)
            1
        }

internal fun View.setOffsetStart(offset: Float) {
    val newParams = layoutParams
    val margin = (-offset).toInt()
    (newParams as? ViewGroup.MarginLayoutParams?)?.let {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            it.marginEnd = margin
        } else {
            it.rightMargin = margin
        }
        layoutParams = it
    }
}