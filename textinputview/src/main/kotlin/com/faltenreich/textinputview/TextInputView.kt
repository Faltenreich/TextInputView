@file:Suppress("MemberVisibilityCanBePrivate")

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
import kotlin.math.max
import kotlin.math.min

open class TextInputView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    editText: EditText? = null
) : FrameLayout(context, attrs, defStyleAttr) {

    constructor(editText: EditText) : this(editText.context, null, 0, editText)

    private val isRtl: Boolean by lazy { context.isRtl() }
    private var isInitialized = false

    private var customOverlapAction: Int? = null
    private var customTextSize: Float? = null
    private var customTextColorNormal: Int? = null
    private var customTextColorSelected: Int? = null

    private lateinit var editText: EditText
    private lateinit var hintView: TextView
    private val hintPadding by lazy {
        TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            HINT_PADDING,
            resources.displayMetrics
        )
    }

    /**
     * Action when the embedded EditText overlaps the hint (one of: {@link OVERLAP_ACTION_TOGGLE}, {@link OVERLAP_ACTION_PUSH})
     */
    var overlapAction: Int = -1
        set(value) {
            field = value
            customOverlapAction = value
            invalidateHint()
        }

    /**
     * Text size of the hint (in px)
     */
    var textSize: Float = -1f
        set(value) {
            field = value
            customTextSize = value
            invalidateHint()
        }

    /**
     * Text color of the unfocused EditText (in @ColorInt)
     */
    var textColorNormal: Int = -1
        set(value) {
            field = value
            customTextColorNormal = value
            invalidateHint()
        }

    /**
     * Text color of the focused EditText (in @ColorInt)
     */
    var textColorSelected: Int = -1
        set(value) {
            field = value
            customTextColorSelected = value
            invalidateHint()
        }

    init {
        initCustomAttributes(attrs)
        editText?.apply { addView(this) }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (!isInEditMode) {
            initEditText()
            initHint()
            initAttributes()
        }
    }

    private fun initCustomAttributes(attrs: AttributeSet?) {
        attrs?.apply {
            val typedArray = context.obtainStyledAttributes(this, R.styleable.TextInputView, 0, 0)
            customOverlapAction =
                typedArray.getInt(R.styleable.TextInputView_overlapAction, -1).takeIf { it >= 0 }
            customTextSize = typedArray.getFloat(R.styleable.TextInputView_android_textSize, -1f)
                .takeIf { it >= 0 }
            customTextColorNormal =
                typedArray.getColorStateList(R.styleable.TextInputView_android_textColor)?.defaultColor
            customTextColorSelected =
                typedArray.getColorStateList(R.styleable.TextInputView_android_tint)?.defaultColor
            typedArray.recycle()
        }
    }

    private fun initAttributes() {
        overlapAction = customOverlapAction ?: OVERLAP_ACTION_PUSH
        textSize = customTextSize ?: editText.textSize
        textColorNormal = customTextColorNormal ?: editText.hintTextColors.defaultColor
        textColorSelected = customTextColorSelected ?: context.accentColor()
    }

    private fun initEditText() {
        editText = try {
            views().first { it is EditText } as EditText
        } catch (exception: NoSuchElementException) {
            throw Exception("${tag()} requires an EditText as first child")
        }
        editText.onFocusChangeListener = OnFocusChangeListener { _, _ -> invalidateHint() }
        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                text: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
            }

            override fun onTextChanged(text: CharSequence?, start: Int, count: Int, after: Int) {
                invalidateHint()
            }

            override fun afterTextChanged(editable: Editable?) {}
        })
        editText.setOnGlobalLayoutChangeListener {
            isInitialized = true
            invalidateHint(false)
        }
    }

    private fun initHint() {
        hintView = TextView(context)
        val layoutParams = LayoutParams(
            LayoutParams.WRAP_CONTENT,
            LayoutParams.WRAP_CONTENT,
            editText.verticalGravity()
        )
        hintView.layoutParams = layoutParams

        // Replace the original hint of the wrapped EditText
        hintView.setPadding(
            editText.paddingLeft,
            editText.paddingTop,
            editText.paddingRight,
            editText.paddingBottom
        )
        hintView.text = editText.hint
        editText.hint = null

        addView(hintView)
    }

    private fun invalidateHint(shouldAnimate: Boolean = true) {
        if (isInitialized) {
            val hasFocus = editText.hasFocus()
            val isEmpty = editText.text.isEmpty()

            val textColor = if (hasFocus) textColorSelected else textColorNormal
            hintView.setTextColor(textColor, ANIMATION_DURATION, AccelerateDecelerateInterpolator())
            hintView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize)
            hintView.measure(0, 0)

            val alignmentIdle = when {
                editText.isGravityRight() -> ALIGNMENT_END
                editText.isGravityCenter() -> ALIGNMENT_CENTER
                else -> ALIGNMENT_START
            }
            val alignmentActive =
                if (alignmentIdle == ALIGNMENT_END) ALIGNMENT_START else ALIGNMENT_END

            val editTextWidth = editText.width
            val hintWidth = hintView.measuredWidth

            val offsetStart =
                (if (isRtl) editText.endOffset() else editText.startOffset()).toFloat()
            val offsetRight =
                (if (isRtl) editText.startOffset() else editText.endOffset()).toFloat()
            val offsetThreshold =
                if (alignmentActive == ALIGNMENT_START) offsetStart else editTextWidth - hintWidth - offsetRight

            val offset =
                if (isEmpty && !hasFocus) {
                    when (alignmentIdle) {
                        ALIGNMENT_END -> editTextWidth - hintWidth - offsetRight
                        ALIGNMENT_CENTER -> (editTextWidth - hintWidth).toFloat() / 2
                        else -> offsetStart
                    }
                } else {
                    val offsetActive = when (alignmentIdle) {
                        ALIGNMENT_END -> editTextWidth - hintWidth - hintPadding - editText.getTextWidth(
                            editText.lineCount - 1
                        ) - offsetRight
                        ALIGNMENT_CENTER -> ((editTextWidth + editText.getTextWidth(editText.lineCount - 1)) / 2) - hintPadding
                        else -> offsetStart + editText.getTextWidth(editText.lineCount - 1) + hintPadding
                    }
                    if (alignmentActive == ALIGNMENT_START) min(offsetThreshold, offsetActive)
                    else max(offsetThreshold, offsetActive)
                }

            val overlaps =
                if (alignmentActive == ALIGNMENT_START) offset < offsetThreshold else offset > offsetThreshold
            val shrink =
                if (alignmentActive == ALIGNMENT_START) offset > hintView.translationX else offset < hintView.translationX

            val offsetLocalized = if (isRtl) -offset else offset
            val visibility =
                when (overlapAction) {
                    OVERLAP_ACTION_TOGGLE -> if (overlaps) View.GONE else View.VISIBLE
                    OVERLAP_ACTION_PUSH -> {
                        val animate = shouldAnimate && (isEmpty || shrink)
                        val duration = if (animate) ANIMATION_DURATION else 0
                        hintView.clearAnimation()
                        hintView.animate().translationX(offsetLocalized).setDuration(duration)
                            .start()
                        View.VISIBLE
                    }
                    else -> View.VISIBLE
                }
            hintView.visibility = if (editText.lineCount > 1) View.GONE else visibility
        }
    }

    companion object {

        private const val OVERLAP_ACTION_PUSH = 0
        private const val OVERLAP_ACTION_TOGGLE = 1

        private const val ALIGNMENT_START = 0
        private const val ALIGNMENT_CENTER = 1
        private const val ALIGNMENT_END = 2

        private const val ANIMATION_DURATION = 200L

        private const val HINT_PADDING = 8F
    }
}