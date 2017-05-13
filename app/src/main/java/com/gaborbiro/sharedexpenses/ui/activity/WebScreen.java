package com.gaborbiro.sharedexpenses.ui.activity;

import android.content.IntentSender;

public interface WebScreen {

    void chooseTenant();

    void update();

    void intent(IntentSender intentSender);
}
