package com.faltenreich.inputhintlayout;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewPropertyAnimator;

import org.jetbrains.annotations.NotNull;

/**
 * Created by Faltenreich on 08.02.2018
 */

public class CustomInputHintLayout extends InputHintLayout {

    public CustomInputHintLayout(@NotNull Context context) {
        super(context);
    }

    @NonNull
    @Override
    public ViewPropertyAnimator onCreateInAnimation(@NotNull View view) {
        return super.onCreateInAnimation(view);
    }

    @NonNull
    @Override
    public ViewPropertyAnimator onCreateOutAnimation(@NotNull View view) {
        return super.onCreateOutAnimation(view);
    }
}
