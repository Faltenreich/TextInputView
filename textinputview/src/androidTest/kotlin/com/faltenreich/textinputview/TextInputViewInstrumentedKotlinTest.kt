package com.faltenreich.textinputview

import android.content.Context
import android.view.Gravity
import android.view.View
import android.widget.EditText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TextInputViewInstrumentedKotlinTest {

    private lateinit var context: Context
    private lateinit var editText: EditText
    private lateinit var textInputView: TextInputView

    @Before
    fun init() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        editText = EditText(context)
        textInputView = TextInputView(editText)
    }

    @Test
    fun testInitialization() {
        Assert.assertNotNull(textInputView)
    }

    @Test
    fun testGravity() {
        editText.gravity = Gravity.END
        Assert.assertEquals(editText.isGravityRight(), true)
        Assert.assertEquals(editText.isGravityCenter(), false)
        editText.gravity = Gravity.START
    }

    @Test
    fun testRtl() {
        val isRtl = context.resources.configuration.layoutDirection == View.LAYOUT_DIRECTION_RTL
        Assert.assertEquals(isRtl, context.isRtl())
    }

    @Test
    fun testTextWidth() {
        Assert.assertEquals(editText.getTextWidth(0), 0F)
    }
}