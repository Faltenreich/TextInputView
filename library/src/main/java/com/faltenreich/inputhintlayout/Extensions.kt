package com.faltenreich.inputhintlayout

import android.content.Context
import android.content.res.ColorStateList
import android.os.Build
import android.util.TypedValue

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

fun Context.getColorStateListCompat(colorResId: Int): ColorStateList =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            resources.getColorStateList(colorResId, theme)
        } else {
            @Suppress("DEPRECATION")
            resources.getColorStateList(colorResId)
        }