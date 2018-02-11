package com.faltenreich.inputhintlayout

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

const val ANIMATION_STYLE_TOGGLE = 0
const val ANIMATION_STYLE_PUSH = 1

private const val ANIMATION_DURATION = 200L

@Suppress("MemberVisibilityCanBePrivate")
open class InputHintLayout @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private var customOverlapAnimation: Int = -1
    private var customTextSize: Float = -1f
    private var customTextColorNormal: Int = -1
    private var customTextColorSelected: Int = -1

    private var maxLineCount: Int = 1

    private val editText: EditText by lazy { onAttachEditText() }

    private val hintView: TextView by lazy { onCreateHintView() }

    private var maxLineWidth: Int = 0
        get() = editText.width - hintView.width

    private var textColor: Int
        get() = hintView.textColors.defaultColor
        set(value) { hintView.setTextColor(value, ANIMATION_DURATION, interpolator) }

    var overlapAnimation: Int = -1

    var interpolator: Interpolator = AccelerateDecelerateInterpolator()

    var textSize: Float
        get() = hintView.textSize
        set(value) { hintView.setTextSize(TypedValue.COMPLEX_UNIT_PX, editText.textSize) }

    var textColorNormal: Int = -1
        set(value) {
            field = value
            onInputFocusChanged()
        }

    var textColorSelected: Int = -1
        set(value) {
            field = value
            onInputFocusChanged()
        }

    init {
        attrs?.let {
            val typedArray = context.obtainStyledAttributes(it, R.styleable.InputHintLayout, 0, 0)
            customOverlapAnimation = typedArray.getInt(R.styleable.InputHintLayout_overlapAnimation, -1)
            customTextSize = typedArray.getFloat(R.styleable.InputHintLayout_android_textSize, -1f)
            customTextColorNormal = typedArray.getColorStateList(R.styleable.InputHintLayout_android_textColor)?.defaultColor ?: -1
            customTextColorSelected = typedArray.getColorStateList(R.styleable.InputHintLayout_android_tint)?.defaultColor ?: -1
            typedArray.recycle()
        }
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

        overlapAnimation = if (customOverlapAnimation >= 0) customOverlapAnimation else ANIMATION_STYLE_TOGGLE
        textSize = if (customTextSize >= 0) customTextSize else editText.textSize
        textColorNormal = if (customTextColorNormal >= 0) customTextColorNormal else editText.hintTextColors.defaultColor
        textColorSelected = if (customTextColorSelected >= 0) customTextColorSelected else context.accentColor()

        if (!isInEditMode) {
            editText.onFocusChangeListener = OnFocusChangeListener { _, _ -> onInputFocusChanged() }
            editText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(text: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun onTextChanged(text: CharSequence?, start: Int, count: Int, after: Int) { onInputTextChanged() }
                override fun afterTextChanged(editable: Editable?) {}
            })
        }
    }

    private fun onAttachEditText(): EditText =
            try {
                views().first { it is EditText } as EditText
            } catch (exception: NoSuchElementException) {
                throw Exception("${tag()} requires an EditText as first child")
            }

    private fun onCreateHintView(): TextView {
        val hintView = TextView(context)
        val layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        hintView.layoutParams = layoutParams

        // Replace the original hint of the wrapped EditText
        hintView.setPadding(editText.paddingLeft, editText.paddingTop, editText.paddingRight, editText.paddingBottom)
        hintView.text = editText.hint
        editText.hint = null

        addView(hintView)
        return hintView
    }

    private fun onInputFocusChanged() {
        val hasFocus = editText.hasFocus()
        val isEmpty = editText.text.isEmpty()

        textColor = if (hasFocus) textColorSelected else textColorNormal

        val position = if (!hasFocus && isEmpty) 0 else maxLineWidth
        hintView.animate().translationX(position.toFloat()).setDuration(ANIMATION_DURATION).start()
    }

    private fun onInputTextChanged() {
        // Ensure correct line count on shrinking text
        if (editText.lineCount <= maxLineCount) {
            editText.setLines(editText.lineCount)
        }

        val currentLineWidth = editText.getTextWidth(editText.lineCount - 1)
        val exceededLineWidth = currentLineWidth > maxLineWidth
        if (exceededLineWidth) {
            if (editText.lineCount < maxLineCount) {
                editText.setLines(editText.lineCount + 1)
            } else {
                onInputTextOffsetChanged(currentLineWidth - maxLineWidth)
            }
        } else {
            onInputTextOffsetChanged(0f)
        }
    }

    private fun onInputTextOffsetChanged(offset: Float) {
        val overlaps = offset > 0
        when (overlapAnimation) {
            ANIMATION_STYLE_TOGGLE -> hintView.visibility = if (overlaps) View.INVISIBLE else View.VISIBLE
            ANIMATION_STYLE_PUSH -> hintView.setOffsetStart(offset)
        }
    }
}