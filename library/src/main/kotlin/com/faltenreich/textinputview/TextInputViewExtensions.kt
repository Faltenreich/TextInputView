package com.faltenreich.textinputview

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.animation.Interpolator
import android.widget.EditText
import android.widget.TextView



/**
 * Created by Faltenreich on 22.01.2018
 */

internal fun Any.tag(): String = javaClass.simpleName

internal fun ViewGroup.views(): List<View> = (0 until childCount).map { getChildAt(it) }

internal fun View.setOnGlobalLayoutChangeListener(action: () -> Unit) {
    viewTreeObserver.addOnGlobalLayoutListener(object: ViewTreeObserver.OnGlobalLayoutListener {
        override fun onGlobalLayout() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                viewTreeObserver.removeOnGlobalLayoutListener(this)
            } else {
                @Suppress("DEPRECATION")
                viewTreeObserver.removeGlobalOnLayoutListener(this)
            }
            action()
        }
    })
}

internal fun Context.accentColor(): Int {
    val attr =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) android.R.attr.colorAccent
            else resources.getIdentifier("colorAccent", "attr", packageName)
    val outValue = TypedValue()
    theme.resolveAttribute(attr, outValue, true)
    return outValue.data
}

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

internal fun EditText.getTextWidth(line: Int = -1): Float {
    val input = if (line >= 0) getTextForLine(line) else text?.toString()
    return input?.let { paint.measureText(input) } ?: 0f
}

private fun EditText.getTextForLine(line: Int): String? =
        layout?.let {
            val input = text?.toString()
            val lineCharacters = input?.length ?: 0
            val start = it.getLineStart(line)
            val end = it.getLineEnd(line)
            val isInBounds = start < end && start < lineCharacters && end <= lineCharacters
            if (isInBounds) input?.substring(start, end) else null
        }

private fun TextView.absoluteGravity() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) Gravity.getAbsoluteGravity(gravity, layoutDirection) else gravity

@SuppressLint("RtlHardcoded")
internal fun TextView.isGravityEnd(): Boolean = absoluteGravity() and Gravity.HORIZONTAL_GRAVITY_MASK == Gravity.RIGHT