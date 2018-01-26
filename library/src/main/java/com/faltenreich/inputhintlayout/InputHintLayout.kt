package com.faltenreich.inputhintlayout

import android.animation.Animator
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.ViewPropertyAnimator
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Interpolator
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.TextView

/**
 * Created by Faltenreich on 21.01.2018
 */

const val OVERLAP_ANIMATION_TOGGLE = 0
const val OVERLAP_ANIMATION_FADE = 1
const val OVERLAP_ANIMATION_PUSH = 2

const val HIDE_ANIMATION_STATUS_IDLE = 0
const val HIDE_ANIMATION_STATUS_ANIMATING_IN = 1
const val HIDE_ANIMATION_STATUS_ANIMATION_OUT = 2

private const val ANIMATION_DURATION_DEFAULT = 300L
private const val OVERLAP_ANIMATION_DEFAULT = OVERLAP_ANIMATION_TOGGLE

@Suppress("MemberVisibilityCanBePrivate")
class InputHintLayout @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private var customOverlapAnimation: Int = -1
    private var customAnimationDuration: Long = -1
    private var customTextSize: Float = -1f
    private var customTextColorNormal: Int = -1
    private var customTextColorSelected: Int = -1

    private var currentAnimationStatus = HIDE_ANIMATION_STATUS_IDLE
    private var pendingAnimationStatus = HIDE_ANIMATION_STATUS_IDLE

    private var maxLineCount: Int = 1

    private val editText: EditText by lazy { onAttachEditText() }

    private val hintView: TextView by lazy {
        val hintView = onCreateHintView()
        addView(hintView)
        hintView.setPadding(editText.paddingLeft, editText.paddingTop, editText.paddingRight, editText.paddingBottom)
        hintView.setLayoutGravity(Gravity.BOTTOM or Gravity.END) // TODO: Rtl
        hintView.text = editText.hint
        hintView
    }

    private var maxLineWidth: Int = 0
        get() = editText.width - hintView.width - hintView.paddingLeft - hintView.paddingRight

    private var textColor: Int
        get() = hintView.textColors.defaultColor
        set(value) { hintView.setTextColor(value, animationDurationMillis, interpolator) }

    var overlapAnimation: Int = -1

    var animationDurationMillis: Long = -1L

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
        if (editText.gravity != Gravity.TOP) {
            Log.d(tag(), "Gravity gets overwritten to Gravity.TOP in order to support multiline text")
        }

        editText.gravity = Gravity.TOP
        maxLineCount = editText.getMaxLineCountCompat()

        overlapAnimation = if (customOverlapAnimation >= 0) customOverlapAnimation else OVERLAP_ANIMATION_DEFAULT
        animationDurationMillis = if (customAnimationDuration >= 0) customAnimationDuration else ANIMATION_DURATION_DEFAULT
        textSize = if (customTextSize >= 0) customTextSize else editText.textSize
        textColorNormal = if (customTextColorNormal >= 0) customTextColorNormal else editText.hintTextColors.defaultColor
        textColorSelected = if (customTextColorSelected >= 0) customTextColorSelected else context.accentColor()

        if (!isInEditMode) {
            editText.addTextChangedListener(object: TextWatcher {
                override fun beforeTextChanged(text: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun onTextChanged(text: CharSequence?, start: Int, count: Int, after: Int) {
                    onInputTextChanged()

                    val firstWord = start == 0
                    val inBounds = text?.length ?: 0 <= 1
                    val emptied = count == 1 && after == 0
                    val unemptied = count == 0 && after == 1
                    if (firstWord && inBounds && (emptied || unemptied)) {
                        onInputTextIsEmptyChanged(after == 1)
                    }
                }
                override fun afterTextChanged(editable: Editable?) {}
            })

            editText.onFocusChangeListener = OnFocusChangeListener { _, _ -> onInputFocusChanged() }

            val showHint = editText.text.isNotEmpty()
            hintView.alpha = if (showHint) 1f else 0f
        }
    }

    private fun onAttachEditText(): EditText =
            try {
                views().first { it is EditText } as EditText
            } catch (exception: NoSuchElementException) {
                throw Exception("${tag()} requires an EditText as first child")
            }

    fun onCreateHintView(): TextView {
        val hintView = InputHintView(context)
        val layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        hintView.layoutParams = layoutParams
        return hintView
    }

    private fun onCreateInAlphaAnimation(view: View): ViewPropertyAnimator = view.animate().alpha(1f)

    fun onCreateInAnimation(view: View): ViewPropertyAnimator = onCreateInAlphaAnimation(view)

    private fun onCreateOutAlphaAnimation(view: View): ViewPropertyAnimator = view.animate().alpha(0f)

    fun onCreateOutAnimation(view: View): ViewPropertyAnimator = onCreateOutAlphaAnimation(view)

    private fun onInputFocusChanged() {
        textColor = if (editText.hasFocus()) textColorSelected else textColorNormal
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
                onInputTextChangedOutOfBounds(currentLineWidth - maxLineWidth)
            }
        } else {
            onInputTextChangedInBounds()
        }
    }

    fun onInputTextChangedOutOfBounds(offset: Float) {
        when (overlapAnimation) {
            OVERLAP_ANIMATION_TOGGLE -> hintView.visibility = View.INVISIBLE
            OVERLAP_ANIMATION_FADE -> hintView.visibility = View.INVISIBLE // TODO
            OVERLAP_ANIMATION_PUSH -> hintView.setOffsetStart(offset)
        }
    }

    fun onInputTextChangedInBounds() {
        when (overlapAnimation) {
            OVERLAP_ANIMATION_TOGGLE -> hintView.visibility = View.VISIBLE
            OVERLAP_ANIMATION_FADE -> hintView.visibility = View.VISIBLE // TODO
            OVERLAP_ANIMATION_PUSH -> hintView.setOffsetStart(0f)
        }
    }

    private fun onInputTextIsEmptyChanged(isVisible: Boolean) {
        pendingAnimationStatus = HIDE_ANIMATION_STATUS_IDLE
        when (currentAnimationStatus) {
            HIDE_ANIMATION_STATUS_IDLE -> onInputVisibilityChange(isVisible)
            HIDE_ANIMATION_STATUS_ANIMATING_IN -> if (!isVisible) pendingAnimationStatus = HIDE_ANIMATION_STATUS_ANIMATION_OUT
            HIDE_ANIMATION_STATUS_ANIMATION_OUT -> { if (isVisible) pendingAnimationStatus = HIDE_ANIMATION_STATUS_ANIMATING_IN }
        }
    }

    private fun onInputVisibilityChange(
            animateIn: Boolean,
            animator: ViewPropertyAnimator = if (animateIn) onCreateInAnimation(hintView) else onCreateOutAnimation(hintView)) {
        animator.duration = animationDurationMillis
        animator.interpolator = interpolator
        animator.setListener(object: Animator.AnimatorListener {
            override fun onAnimationStart(animator: Animator?) {
                currentAnimationStatus = if (animateIn) HIDE_ANIMATION_STATUS_ANIMATING_IN else HIDE_ANIMATION_STATUS_ANIMATION_OUT
                Log.d(tag(), "Started animation $currentAnimationStatus")
            }
            override fun onAnimationEnd(animator: Animator?) {
                Log.d(tag(), "Finished animation $currentAnimationStatus")
                currentAnimationStatus = HIDE_ANIMATION_STATUS_IDLE
                when (pendingAnimationStatus) {
                    HIDE_ANIMATION_STATUS_ANIMATING_IN -> onInputTextIsEmptyChanged(true)
                    HIDE_ANIMATION_STATUS_ANIMATION_OUT -> onInputTextIsEmptyChanged(false)
                    else -> Unit
                }
            }
            override fun onAnimationCancel(animator: Animator?) {
                currentAnimationStatus = HIDE_ANIMATION_STATUS_IDLE
            }
            override fun onAnimationRepeat(animator: Animator?) {
                currentAnimationStatus = if (animateIn) HIDE_ANIMATION_STATUS_ANIMATING_IN else HIDE_ANIMATION_STATUS_ANIMATION_OUT
            }
        })
        animator.start()
    }
}