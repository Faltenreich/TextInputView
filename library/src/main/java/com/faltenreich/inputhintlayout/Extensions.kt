package com.faltenreich.inputhintlayout

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.Context
import android.os.Build
import android.util.TypedValue
import android.view.View
import android.view.animation.Interpolator
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView

/**
 * Created by Faltenreich on 22.01.2018
 */

fun Context.accentColor(): Int {
    val attr =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) android.R.attr.colorAccent
            else resources.getIdentifier("colorAccent", "attr", packageName)
    val outValue = TypedValue()
    theme.resolveAttribute(attr, outValue, true)
    return outValue.data
}

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