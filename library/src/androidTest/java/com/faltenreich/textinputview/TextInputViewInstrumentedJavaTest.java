package com.faltenreich.textinputview;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.widget.EditText;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Created by Faltenreich on 16.02.2018
 */

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
