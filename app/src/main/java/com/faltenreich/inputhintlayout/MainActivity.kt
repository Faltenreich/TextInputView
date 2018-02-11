package com.faltenreich.inputhintlayout

import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v7.app.AppCompatActivity
import android.text.InputType
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

    private fun showcase() {
        val input = "Input"
        val delay = 5000L
        val speed = 200L

        var add = true
        var index = 0

        editText.inputType = InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS

        val handler = Handler { message ->
            editText.setText(message.obj as String)
            editText.setSelection(editText.text.length);
            true
        }

        val timer = Timer()
        timer.scheduleAtFixedRate(object: TimerTask() {
            override fun run() {
                val message = Message()
                message.obj = if (index >= 0) input.substring(IntRange(0, index)) else ""
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
        }, delay, speed)
    }
}