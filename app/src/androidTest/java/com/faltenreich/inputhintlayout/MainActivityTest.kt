package com.faltenreich.inputhintlayout

import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.action.ViewActions.replaceText
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.ViewMatchers.*
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import android.view.View
import android.view.ViewGroup
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.allOf
import org.hamcrest.TypeSafeMatcher
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@Suppress("RedundantVisibilityModifier", "unused")
@RunWith(AndroidJUnit4::class)
class MainActivityTest {

    @get:Rule
    public var activityTestRule = ActivityTestRule(MainActivity::class.java)

    private val editText by lazy { onView(childAtPosition(allOf(withId(R.id.input_hint_layout)), 0)) }
    private val hintView by lazy { onView(childAtPosition(allOf(withId(R.id.input_hint_layout)), 1)) }

    @Test
    fun testHintTextMatches() {
        val hint = "InputHintLayout"
        editText.check(matches(withHint(hint)))
        hintView.check(matches(withText(hint)))
    }

    @Test
    fun testHintTogglesVisibility() {
        editText.perform(click())
        editText.perform(replaceText("Test"))
    }

    private fun childAtPosition(parentMatcher: Matcher<View>, position: Int) =
            object : TypeSafeMatcher<View>() {
                override fun describeTo(description: Description) {
                    description.appendText("Child at position $position in parent ")
                    parentMatcher.describeTo(description)
                }
                override fun matchesSafely(view: View) = (view.parent as? ViewGroup)?.let { parentMatcher.matches(it) && view == it.getChildAt(position) } == true
            }
}