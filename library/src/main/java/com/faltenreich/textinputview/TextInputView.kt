package com.faltenreich.textinputview

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Interpolator
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.TextView

/**
 * Created by Faltenreich on 21.01.2018
 */

const val OVERLAP_ACTION_PUSH = 0
const val OVERLAP_ACTION_TOGGLE = 1

private const val ANIMATION_DURATION = 200L

@Suppress("MemberVisibilityCanBePrivate")
open class TextInputView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0, editText: EditText? = null) : FrameLayout(context, attrs, defStyleAttr) {

    private var customOverlapAction: Int = -1
    private var customTextSize: Float = -1f
    private var customTextColorNormal: Int = -1
    private var customTextColorSelected: Int = -1

    private var maxLineCount: Int = 1

    private val editText: EditText by lazy {
        try { views().first { it is EditText } as EditText }
        catch (exception: NoSuchElementException) { throw Exception("${tag()} requires an EditText as first child") } }

    private val hintView: TextView by lazy {
        val hintView = TextView(context)
        val layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        hintView.layoutParams = layoutParams

        // Replace the original hint of the wrapped EditText
        hintView.setPadding(this.editText.paddingLeft, this.editText.paddingTop, this.editText.paddingRight, this.editText.paddingBottom)
        hintView.text = this.editText.hint
        this.editText.hint = null

        addView(hintView)
        hintView
    }

    private var maxLineWidth: Int = 0
        get() = editText.width - hintView.width

    private var textColor: Int
        get() = hintView.textColors.defaultColor
        set(value) { hintView.setTextColor(value, ANIMATION_DURATION, interpolator) }

    var overlapAction: Int = -1

    var interpolator: Interpolator = AccelerateDecelerateInterpolator()

    var textSize: Float
        get() = hintView.textSize
        set(value) { hintView.setTextSize(TypedValue.COMPLEX_UNIT_PX, editText.textSize) }

    var textColorNormal: Int = -1
        set(value) {
            field = value
            invalidateHint()
        }

    var textColorSelected: Int = -1
        set(value) {
            field = value
            invalidateHint()
        }

    init {
        attrs?.let {
            val typedArray = context.obtainStyledAttributes(it, R.styleable.TextInputView, 0, 0)
            customOverlapAction = typedArray.getInt(R.styleable.TextInputView_overlapAction, -1)
            customTextSize = typedArray.getFloat(R.styleable.TextInputView_android_textSize, -1f)
            customTextColorNormal = typedArray.getColorStateList(R.styleable.TextInputView_android_textColor)?.defaultColor ?: -1
            customTextColorSelected = typedArray.getColorStateList(R.styleable.TextInputView_android_tint)?.defaultColor ?: -1
            typedArray.recycle()
        }
        editText?.let { addView(it) }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        initLayout()
    }

    private fun initLayout() {
        maxLineCount = editText.getMaxLineCountCompat()

        // Ensure input alignment on multiline
        if (maxLineCount > 1) {
            editText.gravity = Gravity.TOP
            hintView.setLayoutGravity(Gravity.BOTTOM)
        }

        overlapAction = if (customOverlapAction >= 0) customOverlapAction else OVERLAP_ACTION_PUSH
        textSize = if (customTextSize >= 0) customTextSize else editText.textSize
        textColorNormal = if (customTextColorNormal >= 0) customTextColorNormal else editText.hintTextColors.defaultColor
        textColorSelected = if (customTextColorSelected >= 0) customTextColorSelected else context.accentColor()

        if (!isInEditMode) {
            editText.onFocusChangeListener = OnFocusChangeListener { _, _ -> invalidateHint() }
            editText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(text: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun onTextChanged(text: CharSequence?, start: Int, count: Int, after: Int) { invalidateHint() }
                override fun afterTextChanged(editable: Editable?) {}
            })
            editText.setOnGlobalLayoutChangeListener { invalidateHint(false) }
        }
    }

    private fun invalidateHint(animate: Boolean = true) {
        val hasFocus = editText.hasFocus()
        val isEmpty = editText.text.isEmpty()

        textColor = if (hasFocus) textColorSelected else textColorNormal

        if (editText.lineCount == 1) {
            hintView.visibility = View.VISIBLE
            val offset =
                    if (isEmpty) { if (!hasFocus) 0F else maxLineWidth.toFloat() }
                    else { Math.max(editText.getTextWidth(editText.lineCount - 1), maxLineWidth.toFloat()) }
            val overlaps = offset > maxLineWidth
            when (overlapAction) {
                OVERLAP_ACTION_TOGGLE -> if (overlaps) hintView.visibility = View.GONE else View.VISIBLE
                OVERLAP_ACTION_PUSH -> hintView.animate().translationX(offset).setDuration(if (animate && !overlaps) ANIMATION_DURATION else 0).start()
            }
        } else {
            hintView.visibility = View.GONE
        }
    }
}