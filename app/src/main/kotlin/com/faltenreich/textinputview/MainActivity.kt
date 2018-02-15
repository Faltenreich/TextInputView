package com.faltenreich.textinputview

import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v7.app.AppCompatActivity
import android.text.InputType
import android.widget.EditText
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

/**
 * Created by Faltenreich on 21.01.2018
 */

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    private fun addTextInputView() {
        val editText = EditText(this)
        editText.maxLines = 2
        editText.setText("Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod")
        editText.hint = "Hint"
        val textInputView = TextInputView(this, editText = editText)
        layout.addView(textInputView)
    }

    private fun showcase() {
        editText.clearFocus()

        val input = "Just move it"

        var add = true
        var index = 0
        var wait = 0

        editText.inputType = InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS

        Handler().postDelayed({
            editText.requestFocus()
        }, 3500)

        val handler = Handler { message ->
            editText.setText(message.obj as String)
            editText.setSelection(editText.text.length)
            true
        }

        val timer = Timer()
        timer.scheduleAtFixedRate(object: TimerTask() {
            override fun run() {
                val text = input.substring(IntRange(0, index))
                val message = Message()
                message.obj = if (index >= 0) text else ""

                if (wait >= 0) {
                    wait--
                } else {
                    if (text.length == input.length) {
                        wait = 4
                    }

                    handler.sendMessage(message)

                    if (add) {
                        if (index < input.length - 1) {
                            index++
                        } else {
                            add = !add
                            index--
                        }
                    } else {
                        if (index < 0) {
                            timer.cancel()
                            timer.purge()
                        } else {
                            index--
                        }
                    }
                }
            }
        }, 4500, 140L)


        Handler().postDelayed({
            editText.clearFocus()
        }, 9100)
    }
}