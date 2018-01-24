package com.faltenreich.inputhintlayout

import android.os.Bundle
import android.support.v4.widget.TextViewCompat
import android.support.v7.app.AppCompatActivity
import android.widget.EditText

/**
 * Created by Faltenreich on 21.01.2018
 */
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        TextViewCompat.getMaxLines(EditText(this))
    }
}