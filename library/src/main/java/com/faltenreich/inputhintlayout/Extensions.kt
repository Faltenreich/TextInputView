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

fun Any.tag() = javaClass.simpleName

fun Context.accentColor(): Int {
    val attr =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) android.R.attr.colorAccent
            else resources.getIdentifier("colorAccent", "attr", packageName)
    val outValue = TypedValue()
    theme.resolveAttribute(attr, outValue, true)
    return outValue.data
}

fun ViewGroup.views(): List<View> = (0 until childCount).map { getChildAt(it) }

fun TextView.setTextColor(colorTo: Int, durationMillis: Long, interpolator: Interpolator) {
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

fun View.alphaAnimation(fadeIn: Boolean, durationMillis: Long, interpolator: Interpolator) {
    val target = if (fadeIn) 1f else 0f
    if (durationMillis > 0 && target != alpha) {
        animate().alpha(target).setDuration(durationMillis).setInterpolator(interpolator).start()
    } else {
        alpha = target
    }
}

fun View.setLayoutGravity(gravity: Int) {
    val newParams = layoutParams
    (newParams as? FrameLayout.LayoutParams)?.gravity = gravity
    (newParams as? LinearLayout.LayoutParams)?.gravity = gravity
    layoutParams = newParams
}

fun EditText.getTextWidth(line: Int = -1): Float {
    val input = if (line >= 0) getTextForLine(line) else text?.toString()
    return input?.let { paint.measureText(input) } ?: 0f
}

fun EditText.getTextForLine(line: Int): String? =
        layout?.let {
            val input = text?.toString()
            val lineCharacters = input?.length ?: 0
            val start = it.getLineStart(line)
            val end = it.getLineEnd(line)
            val isInBounds = start < end && start < lineCharacters && end <= lineCharacters
            if (isInBounds) input?.substring(start, end) else null
        }

fun EditText.getTextWidth(range: IntRange): Float {
    val input = getTextForRange(range)
    return input?.let { paint.measureText(input) } ?: 0f
}

fun EditText.getTextForRange(range: IntRange): String? =
        layout?.let {
            val input = text?.toString()
            val lineCharacters = input?.length ?: 0
            val start = range.start
            val end = range.endInclusive
            val isInBounds = start < end && start < lineCharacters && end <= lineCharacters
            if (isInBounds) input?.substring(start, end) else null
        }

fun EditText.getMaxLineCountCompat(): Int {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) maxLines else getMaxLineCountPreApi16()
}

fun EditText.getMaxLineCountPreApi16(): Int {
    try {
        val maximumField = text.javaClass.getDeclaredField("mMaximum")
        val maxModeField = text.javaClass.getDeclaredField("mMaxMode")

        maximumField?.isAccessible = true
        maxModeField?.isAccessible = true

        val maximum = maximumField?.getInt(text)
        val maxMode = maxModeField?.getInt(text)

        return if (maxMode == 1) 1 else maximum ?: 1

    } catch (exception: NoSuchFieldException) {
        Log.e(tag(), exception.message)
    } catch (exception: IllegalArgumentException) {
        Log.e(tag(), exception.message)
    } catch (exception: IllegalAccessException) {
        Log.e(tag(), exception.message)
    }
    return 1
}