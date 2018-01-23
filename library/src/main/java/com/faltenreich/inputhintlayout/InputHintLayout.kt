package com.faltenreich.inputhintlayout

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Interpolator
import android.widget.EditText
import android.widget.FrameLayout

/**
 * Created by Faltenreich on 21.01.2018
 */

private const val ANIMATION_DURATION_DEFAULT = 300L

@Suppress("MemberVisibilityCanBePrivate")
class InputHintLayout @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private var customAnimationDuration: Long = 0
    private var customTextSize: Float = 0f
    private var customTextColorNormal: Int = 0
    private var customTextColorSelected: Int = 0

    private val editText: EditText by lazy { views.first { it is EditText } as EditText }

    private val hintView: InputHintView by lazy {
        val hintView = InputHintView(context)
        val layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        hintView.layoutParams = layoutParams
        addView(hintView)
        hintView
    }

    private var maxLineWidth: Int = 0
        get() = editText.width - hintView.width - hintView.paddingLeft - hintView.paddingRight

    private var textColor: Int
        get() = hintView.textColors.defaultColor
        set(value) { hintView.setTextColor(value, animationDurationMillis, interpolator) }

    var animationDurationMillis: Long = ANIMATION_DURATION_DEFAULT

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
            val typedArray = context.obtainStyledAttributes(it, R.styleable.InputHintLayout, 0, 0)
            customAnimationDuration = typedArray.getInt(R.styleable.InputHintLayout_animationDurationMillis, -1).toLong()
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
        editText.gravity= Gravity.TOP

        hintView.setPadding(editText.paddingLeft, editText.paddingTop, editText.paddingRight, editText.paddingBottom)
        hintView.setLayoutGravity(Gravity.BOTTOM or Gravity.END) // TODO: Rtl
        hintView.text = editText.hint

        animationDurationMillis = if (customAnimationDuration >= 0) customAnimationDuration else ANIMATION_DURATION_DEFAULT
        textSize = if (customTextSize >= 0) customTextSize else editText.textSize
        textColorNormal = if (customTextColorNormal >= 0) customTextColorNormal else editText.hintTextColors.defaultColor
        textColorSelected = if (customTextColorSelected >= 0) customTextColorSelected else context.accentColor()

        // TODO: Transition

        if (!isInEditMode) {
            editText.addTextChangedListener(object: TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
                override fun afterTextChanged(p0: Editable?) { invalidateHint() }
            })

            editText.onFocusChangeListener = OnFocusChangeListener { _, _ -> invalidateHint() }

            val showHint = editText.text.isNotEmpty()
            hintView.alpha = if (showHint) 1f else 0f
        }
    }

    private fun invalidateHint() {
        textColor = if (editText.hasFocus()) textColorSelected else textColorNormal

        val showHint = editText.text.isNotEmpty()
        hintView.alphaAnimation(showHint, animationDurationMillis, interpolator)

        val currentLineWidth = editText.getTextWidth(editText.lineCount - 1)
        val exceededLineWidth = currentLineWidth > maxLineWidth
        if (exceededLineWidth) {
            // TODO: Regard maxLines
            // FIXME: setLines() on exceeding line
            editText.setLines(editText.lineCount + 1)
        }
    }

    private val ViewGroup.views: List<View>
        get() = (0 until childCount).map { getChildAt(it) }
}