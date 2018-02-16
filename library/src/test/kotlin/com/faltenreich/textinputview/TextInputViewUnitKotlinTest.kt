package com.faltenreich.textinputview

import junit.framework.Assert
import org.junit.Test

/**
 * Created by Faltenreich on 16.02.2018
 */

class TextInputViewUnitKotlinTest {

    @Test
    fun testTag() {
        Assert.assertEquals(tag(), TextInputViewUnitKotlinTest::class.java.simpleName)
    }
}