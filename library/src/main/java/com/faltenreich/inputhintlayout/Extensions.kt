package com.faltenreich.inputhintlayout

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.Context
import android.os.Build
import android.util.TypedValue
import android.view.View
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

fun TextView.setTextColor(colorTo: Int, durationMillis: Long) {
    val colorFrom = textColors.defaultColor
    val animation = ValueAnimator.ofObject(ArgbEvaluator(), colorFrom, colorTo)
    animation.duration = durationMillis
    animation.addUpdateListener { animator -> setTextColor(animator.animatedValue as Int) }
    animation.start()
}

fun View.alphaAnimation(fadeIn: Boolean, durationMillis: Long) {
    val target = if (fadeIn) 1f else 0f
    if (durationMillis > 0 && target != alpha) {
        animate().alpha(target).setDuration(durationMillis).start()
    } else {
        alpha = target
    }
}

fun View.layoutGravity(gravity: Int) {
    val newParams = layoutParams
    (newParams as? FrameLayout.LayoutParams)?.gravity = gravity
    (newParams as? LinearLayout.LayoutParams)?.gravity = gravity
    layoutParams = newParams
}