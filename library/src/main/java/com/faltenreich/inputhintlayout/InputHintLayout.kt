package com.faltenreich.inputhintlayout

import android.animation.LayoutTransition
import android.animation.ValueAnimator
import android.content.Context
import android.content.res.ColorStateList
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout

/**
 * Created by Faltenreich on 21.01.2018
 */

private const val ANIMATION_DURATION_DEFAULT = 500

@Suppress("MemberVisibilityCanBePrivate")
class InputHintLayout @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private var customAnimationDuration: Int = 0
    private var customHintPadding: Int = 0
    private var customTextSize: Float = 0f
    private var customTextColor: ColorStateList? = null

    var animationDurationMillis = ANIMATION_DURATION_DEFAULT

    var hintPadding
        get() = hintView.paddingLeft
        set(value) { hintView.setPadding(value, hintView.paddingTop, hintView.paddingRight, hintView.paddingBottom) }

    var textSize
        get() = hintView.textSize
        set(value) { hintView.setTextSize(TypedValue.COMPLEX_UNIT_PX, editText.textSize) }

    var textColor
        get() = hintView.textColors
        set(value) { hintView.setTextColor(value) }

    private val editText: EditText by lazy { views.first { it is EditText } as EditText }

    private val hintView: InputHintView by lazy {
        val hintView = InputHintView(context)
        val layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        layoutParams.gravity = Gravity.CENTER_VERTICAL or Gravity.END
        hintView.layoutParams = layoutParams
        addView(hintView)
        hintView
    }

    init {
        attrs?.let {
            val typedArray = context.obtainStyledAttributes(it, R.styleable.InputHintLayout, 0, 0)
            customAnimationDuration = typedArray.getInt(R.styleable.InputHintLayout_animationDurationMillis, 0)
            customHintPadding = typedArray.getDimensionPixelSize(R.styleable.InputHintLayout_hintPadding, 0)
            customTextSize = typedArray.getFloat(R.styleable.InputHintLayout_android_textSize, 0f)
            customTextColor = typedArray.getColorStateList(R.styleable.InputHintLayout_android_textColor)
            typedArray.recycle()
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        initLayout()
    }

    private fun initLayout() {
        hintView.text = editText.hint

        animationDurationMillis = if (customAnimationDuration > 0) customAnimationDuration else ANIMATION_DURATION_DEFAULT
        hintPadding = if (customHintPadding > 0) customHintPadding else context.resources.getDimension(R.dimen.hint_padding_default).toInt()
        textSize = if (customTextSize > 0) customTextSize else editText.textSize
        textColor = if (customTextColor != null) customTextColor else editText.hintTextColors

        // TODO: Margin between EditText and HintView
        // TODO: Styling of HintView (inherit from EditText?)
        // TODO: Gravity of HintView
        // TODO: Transition

        if (!isInEditMode) {
            layoutTransition = LayoutTransition()

            editText.addTextChangedListener(object: TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
                override fun afterTextChanged(p0: Editable?) { invalidateHint() }
            })

            invalidateHint(false)
        }
    }

    private fun invalidateHint(animated: Boolean = true) {
        val showHint = editText.text.isNotEmpty()
        hintView.visibility = if (showHint) View.VISIBLE else View.INVISIBLE

        // TODO: Prevent animating twice
        if (animated) {
            val width = if (showHint) width - hintView.width else width
            val animator = ValueAnimator.ofInt(editText.width, width)
            animator.duration = animationDurationMillis.toLong()
            animator.addUpdateListener {
                editText.layoutParams.width = it.animatedValue as Int
                editText.requestLayout()
            }
            animator.start()
        }
    }

    private val ViewGroup.views: List<View>
        get() = (0 until childCount).map { getChildAt(it) }
}