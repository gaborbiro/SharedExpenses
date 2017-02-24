package com.gaborbiro.sharedexpenses.ui.screen;

import android.content.Intent;
import android.support.annotation.StringRes;

public interface GoogleApiScreen {

    void showProgress();

    void hideProgress();

    void error(String text);

    void toast(String message);

    void toast(@StringRes int message);

    void toast(@StringRes int message, Object... formatArgs);

    void showGooglePlayServicesAvailabilityErrorDialog(int connectionStatusCode);

    void requestAuthorization(Intent intent);

    boolean isGooglePlayServicesAvailable();

    void acquireGooglePlayServices();

    String getSelectedAccountName();

    void chooseGoogleAccount();
}
