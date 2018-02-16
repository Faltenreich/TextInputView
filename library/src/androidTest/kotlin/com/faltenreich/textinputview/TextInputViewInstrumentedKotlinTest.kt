package com.faltenreich.textinputview

import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import android.widget.EditText
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Created by Faltenreich on 16.02.2018
 */

@RunWith(AndroidJUnit4::class)
class TextInputViewInstrumentedKotlinTest {

    @Test
    fun testInitialization() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val editText = EditText(context)
        val textInputView = TextInputView(context, editText = editText)
        Assert.assertNotNull(textInputView)
    }
}