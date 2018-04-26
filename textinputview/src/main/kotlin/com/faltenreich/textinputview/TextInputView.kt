package com.faltenreich.textinputview

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.TextView

/**
 * Created by Faltenreich on 21.01.2018
 */

const val OVERLAP_ACTION_PUSH = 0
const val OVERLAP_ACTION_TOGGLE = 1

private const val ANIMATION_DURATION = 200L
private const val HINT_PADDING = 8F

private enum class Alignment {
    LEFT,
    CENTER,
    RIGHT
}

@Suppress("MemberVisibilityCanBePrivate")
open class TextInputView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0,
        editText: EditText? = null
) : FrameLayout(context, attrs, defStyleAttr) {

    constructor(editText: EditText) : this(editText.context, null, 0, editText)

    private val isRtl: Boolean by lazy { context.isRtl() }
    private var isInitialized = false

    private var customOverlapAction: Int = -1
    private var customTextSize: Float = -1f
    private var customTextColorNormal: Int = -1
    private var customTextColorSelected: Int = -1

    private val editText: EditText by lazy {
        try { views().first { it is EditText } as EditText }
        catch (exception: NoSuchElementException) { throw Exception("${tag()} requires an EditText as first child") } }

    private val hintView: TextView by lazy {
        val hintView = TextView(context)
        val layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, this.editText.verticalGravity())
        hintView.layoutParams = layoutParams

        // Replace the original hint of the wrapped EditText
        hintView.setPadding(this.editText.paddingLeft, this.editText.paddingTop, this.editText.paddingRight, this.editText.paddingBottom)
        hintView.text = this.editText.hint
        this.editText.hint = null

        addView(hintView)
        hintView
    }

    private val hintPadding by lazy { TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, HINT_PADDING, resources.displayMetrics) }

    var overlapAction: Int = -1

    var textSize: Float
        get() = hintView.textSize
        set(value) {
            hintView.setTextSize(TypedValue.COMPLEX_UNIT_PX, editText.textSize)
            invalidateHint(false)
        }

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
            editText.setOnGlobalLayoutChangeListener {
                isInitialized = true
                invalidateHint(false)
            }
        }
    }

    private fun invalidateHint(shouldAnimate: Boolean = true) {
        if (isInitialized) {
            val hasFocus = editText.hasFocus()
            val isEmpty = editText.text.isEmpty()

            val textColor = if (hasFocus) textColorSelected else textColorNormal
            hintView.setTextColor(textColor, ANIMATION_DURATION, AccelerateDecelerateInterpolator())

            val alignmentIdle = when {
                editText.isGravityRight() -> Alignment.RIGHT
                editText.isGravityCenter() -> Alignment.CENTER
                else -> Alignment.LEFT
            }
            val alignmentActive = if (alignmentIdle == Alignment.RIGHT) Alignment.LEFT else Alignment.RIGHT

            val offsetStart = (if (isRtl) editText.endOffset() else editText.startOffset()).toFloat()
            val offsetRight = (if (isRtl) editText.startOffset() else editText.endOffset()).toFloat()
            val offsetThreshold = if (alignmentActive == Alignment.LEFT) offsetStart else editText.width - hintView.width - offsetRight

            val offset =
                    if (isEmpty && !hasFocus) {
                        when (alignmentIdle) {
                            Alignment.RIGHT -> editText.width - hintView.width - offsetRight
                            Alignment.CENTER -> (editText.width - hintView.width).toFloat() / 2
                            Alignment.LEFT -> offsetStart
                        }
                    } else {
                        val offsetActive = when (alignmentIdle) {
                            Alignment.RIGHT -> editText.width - hintView.width - hintPadding - editText.getTextWidth(editText.lineCount - 1) - offsetRight
                            Alignment.CENTER -> ((editText.width + editText.getTextWidth(editText.lineCount - 1)) / 2) - hintPadding
                            Alignment.LEFT -> offsetStart + editText.getTextWidth(editText.lineCount - 1) + hintPadding
                        }
                        if (alignmentActive == Alignment.LEFT) Math.min(offsetThreshold, offsetActive)
                        else Math.max(offsetThreshold, offsetActive)
                    }

            val overlaps = if (alignmentActive == Alignment.LEFT) offset < offsetThreshold else offset > offsetThreshold
            val shrink = if (alignmentActive == Alignment.LEFT) offset > hintView.translationX else offset < hintView.translationX

            val offsetLocalized = if (isRtl) -offset else offset
            val visibility =
                    when (overlapAction) {
                        OVERLAP_ACTION_TOGGLE -> if (overlaps) View.GONE else View.VISIBLE
                        OVERLAP_ACTION_PUSH -> {
                            val animate = shouldAnimate && (isEmpty || shrink)
                            val duration = if (animate) ANIMATION_DURATION else 0
                            hintView.clearAnimation()
                            hintView.animate().translationX(offsetLocalized).setDuration(duration).start()
                            View.VISIBLE
                        }
                        else -> View.VISIBLE
                    }
            hintView.visibility = if (editText.lineCount > 1) View.GONE else visibility
        }
    }
}