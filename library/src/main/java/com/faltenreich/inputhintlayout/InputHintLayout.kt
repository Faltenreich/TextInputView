package com.faltenreich.inputhintlayout

import android.animation.LayoutTransition
import android.animation.ValueAnimator
import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.TextView

/**
 * Created by Faltenreich on 21.01.2018
 */

private const val animationDuration = 500L

class InputHintLayout : FrameLayout {

    @JvmOverloads
    constructor(
            context: Context,
            attrs: AttributeSet? = null,
            defStyleAttr: Int = 0
    ) : super(context, attrs, defStyleAttr)

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(
            context: Context,
            attrs: AttributeSet? = null,
            defStyleAttr: Int = 0,
            defStyleRes: Int = 0
    ) : super(context, attrs, defStyleAttr, defStyleRes)

    private val editText: EditText by lazy { views.first { it is EditText } as EditText }

    private val hintView: TextView by lazy {
        val hintView = TextView(context)
        val layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        layoutParams.gravity = Gravity.CENTER_VERTICAL or Gravity.END
        hintView.layoutParams = layoutParams
        addView(hintView)
        hintView
    }

    init {
        readAttributes()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        initLayout()
    }

    private fun readAttributes() {
        // TODO: Margin between EditText and HintView
        // TODO: Styling of HintView (inherit from EditText?)
        // TODO: Gravity of HintView
        // TODO: Transition
    }

    private fun initLayout() {
        layoutTransition = LayoutTransition()

        hintView.text = editText.hint

        editText.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun afterTextChanged(p0: Editable?) { invalidateHint() }
        })

        invalidateHint(false)
    }

    private fun invalidateHint(animated: Boolean = true) {
        val showHint = editText.text.isNotEmpty()
        hintView.visibility = if (showHint) View.VISIBLE else View.INVISIBLE

        if (animated) {
            val width = if (showHint) width - hintView.width else width
            val animator = ValueAnimator.ofInt(editText.width, width)
            animator.duration = animationDuration
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