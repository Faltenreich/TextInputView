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

const val ANIMATION_STYLE_TOGGLE = 0
const val ANIMATION_STYLE_ANIMATE = 1
const val ANIMATION_STYLE_PUSH = 2

private const val ANIMATION_STATUS_IDLE = 0
private const val ANIMATION_STATUS_ANIMATING_IN = 1
private const val ANIMATION_STATUS_ANIMATING_OUT = 2

private const val ANIMATION_DURATION_DEFAULT = 300L
private const val MOVE_ANIMATION_DEFAULT = ANIMATION_STYLE_ANIMATE
private const val OVERLAP_ANIMATION_DEFAULT = ANIMATION_STYLE_TOGGLE

@Suppress("MemberVisibilityCanBePrivate")
open class InputHintLayout @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private var customMoveAnimation: Int = -1
    private var customOverlapAnimation: Int = -1
    private var customAnimationDuration: Long = -1
    private var customTextSize: Float = -1f
    private var customTextColorNormal: Int = -1
    private var customTextColorSelected: Int = -1

    private var currentAnimationStatus = ANIMATION_STATUS_IDLE
    private var pendingAnimationStatus = ANIMATION_STATUS_IDLE

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

    var moveAnimation: Int = -1

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

            customMoveAnimation = typedArray.getInt(R.styleable.InputHintLayout_moveAnimation, -1)
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

        maxLineCount = editText.getMaxLineCountCompat()

        // Ensure input alignment on multiline
        if (maxLineCount > 1) {
            editText.gravity = Gravity.TOP
        }

        moveAnimation = if (customMoveAnimation >= 0) customMoveAnimation else MOVE_ANIMATION_DEFAULT
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

            hintView.alpha = if (editText.text.isNotEmpty()) 1f else 0f
        }
    }

    private fun onAttachEditText(): EditText =
            try {
                views().first { it is EditText } as EditText
            } catch (exception: NoSuchElementException) {
                throw Exception("${tag()} requires an EditText as first child")
            }

    private fun onCreateHintView(): TextView {
        val hintView = InputHintView(context)
        val layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        hintView.layoutParams = layoutParams
        return hintView
    }

    open fun onCreateInAnimation(view: View): ViewPropertyAnimator = view.animate().alpha(1f)

    open fun onCreateOutAnimation(view: View): ViewPropertyAnimator = view.animate().alpha(0f)

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
            ANIMATION_STYLE_ANIMATE -> onInputAnimateWhenReady(!overlaps)
            ANIMATION_STYLE_PUSH -> hintView.setOffsetStart(offset)
        }
    }

    private fun onInputTextIsEmptyChanged(isVisible: Boolean) {
        when (moveAnimation) {
            ANIMATION_STYLE_TOGGLE -> hintView.visibility = if (isVisible) View.VISIBLE else View.INVISIBLE
            ANIMATION_STYLE_ANIMATE -> onInputAnimateWhenReady(isVisible)
        }
    }

    private fun onInputAnimateWhenReady(animateIn: Boolean) {
        pendingAnimationStatus = ANIMATION_STATUS_IDLE
        when (currentAnimationStatus) {
            ANIMATION_STATUS_IDLE -> onInputAnimate(animateIn)
            ANIMATION_STATUS_ANIMATING_IN -> if (!animateIn) pendingAnimationStatus = ANIMATION_STATUS_ANIMATING_OUT
            ANIMATION_STATUS_ANIMATING_OUT -> if (animateIn) pendingAnimationStatus = ANIMATION_STATUS_ANIMATING_IN
        }
    }

    private fun onInputAnimate(
            animateIn: Boolean,
            animator: ViewPropertyAnimator = if (animateIn) onCreateInAnimation(hintView) else onCreateOutAnimation(hintView)) {
        animator.duration = animationDurationMillis
        animator.interpolator = interpolator
        animator.setListener(object: Animator.AnimatorListener {
            override fun onAnimationStart(animator: Animator?) {
                currentAnimationStatus = if (animateIn) ANIMATION_STATUS_ANIMATING_IN else ANIMATION_STATUS_ANIMATING_OUT
                Log.d(tag(), "Started animation $currentAnimationStatus")
            }
            override fun onAnimationEnd(animator: Animator?) {
                Log.d(tag(), "Finished animation $currentAnimationStatus")
                currentAnimationStatus = ANIMATION_STATUS_IDLE
                when (pendingAnimationStatus) {
                    ANIMATION_STATUS_ANIMATING_IN -> onInputTextIsEmptyChanged(true)
                    ANIMATION_STATUS_ANIMATING_OUT -> onInputTextIsEmptyChanged(false)
                    else -> Unit
                }
            }
            override fun onAnimationCancel(animator: Animator?) {
                currentAnimationStatus = ANIMATION_STATUS_IDLE
            }
            override fun onAnimationRepeat(animator: Animator?) {
                currentAnimationStatus = if (animateIn) ANIMATION_STATUS_ANIMATING_IN else ANIMATION_STATUS_ANIMATING_OUT
            }
        })
        animator.start()
    }
}