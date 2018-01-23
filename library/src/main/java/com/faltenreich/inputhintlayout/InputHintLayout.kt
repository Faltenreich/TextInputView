package com.faltenreich.inputhintlayout

import android.content.Context
import android.graphics.Color
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout

/**
 * Created by Faltenreich on 21.01.2018
 */

private const val ANIMATION_DURATION_DEFAULT = 500L

@Suppress("MemberVisibilityCanBePrivate")
class InputHintLayout @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private var customAnimationDuration: Long = 0
    private var customGravity: Int = 0
    private var customTextSize: Float = 0f
    private var customTextColorNormal: Int = 0
    private var customTextColorSelected: Int = 0

    var animationDurationMillis: Long = ANIMATION_DURATION_DEFAULT

    var gravity: Int
        get() = (hintView.layoutParams as FrameLayout.LayoutParams).gravity
        set(value) { hintView.layoutGravity(value) }

    var textSize: Float
        get() = hintView.textSize
        set(value) { hintView.setTextSize(TypedValue.COMPLEX_UNIT_PX, editText.textSize) }

    private var textColorNormal: Int = Color.GRAY
    private var textColorSelected: Int = Color.BLACK

    var textColor: Int
        get() = hintView.textColors.defaultColor
        set(value) {
            hintView.setTextColor(value, ANIMATION_DURATION_DEFAULT)
        }

    private val editText: EditText by lazy { views.first { it is EditText } as EditText }

    private val hintView: InputHintView by lazy {
        val hintView = InputHintView(context)
        val layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        hintView.layoutParams = layoutParams
        addView(hintView)
        hintView
    }

    init {
        attrs?.let {
            val typedArray = context.obtainStyledAttributes(it, R.styleable.InputHintLayout, 0, 0)
            customAnimationDuration = typedArray.getInt(R.styleable.InputHintLayout_animationDurationMillis, 0).toLong()
            customGravity = typedArray.getInt(R.styleable.InputHintLayout_android_gravity, 0)
            customTextSize = typedArray.getFloat(R.styleable.InputHintLayout_android_textSize, 0f)
            customTextColorNormal = typedArray.getColorStateList(R.styleable.InputHintLayout_android_textColor)?.defaultColor ?: 0
            customTextColorSelected = typedArray.getColorStateList(R.styleable.InputHintLayout_android_tint)?.defaultColor ?: 0
            typedArray.recycle()
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        initLayout()
    }

    private fun initLayout() {
        hintView.setPadding(editText.paddingLeft, editText.paddingTop, editText.paddingRight, editText.paddingBottom)
        hintView.text = editText.hint

        animationDurationMillis = if (customAnimationDuration > 0) customAnimationDuration else ANIMATION_DURATION_DEFAULT
        gravity = if (customGravity > 0) customGravity else Gravity.END
        textSize = if (customTextSize > 0) customTextSize else editText.textSize
        textColorNormal = if (customTextColorNormal > 0) customTextColorNormal else editText.hintTextColors.defaultColor
        textColorSelected = if (customTextColorSelected > 0) customTextColorSelected else context.accentColor()

        // TODO: Multiline Gravity
        // TODO: Transition

        if (!isInEditMode) {
            editText.addTextChangedListener(object: TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
                override fun afterTextChanged(p0: Editable?) { invalidateHint() }
            })

            editText.onFocusChangeListener = OnFocusChangeListener { _, hasFocus ->
                textColor = if (hasFocus) textColorSelected else textColorNormal
            }

            hintView.alpha = 0f
            invalidateHint()
        }
    }

    private fun invalidateHint() {
        val showHint = editText.text.isNotEmpty()
        hintView.alphaAnimation(showHint, animationDurationMillis)
    }

    private val ViewGroup.views: List<View>
        get() = (0 until childCount).map { getChildAt(it) }
}