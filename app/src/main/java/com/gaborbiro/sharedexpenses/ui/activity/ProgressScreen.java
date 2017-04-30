package com.gaborbiro.sharedexpenses.ui.activity;

import android.support.annotation.StringRes;

public interface ProgressScreen {

    void showProgress();

    void hideProgress();

    void error(String text);

    void toast(String message);

    void toast(@StringRes int message);

    void toast(@StringRes int message, Object... formatArgs);
}
