package com.faltenreich.textinputview;

import android.content.Context;
import android.widget.EditText;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class TextInputViewInstrumentedJavaTest {

    @Test
    public void testInitialization() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        EditText editText = new EditText(context);
        TextInputView textInputView = new TextInputView(editText);
        Assert.assertNotNull(textInputView);
    }
}
