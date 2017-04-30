package com.gaborbiro.sharedexpenses.ui.activity;

import android.content.Intent;

public interface GoogleApiScreen {

    void showGooglePlayServicesAvailabilityErrorDialog(int connectionStatusCode);

    void requestAuthorization(Intent intent);

    boolean isGooglePlayServicesAvailable();

    void acquireGooglePlayServices();

    String getSelectedAccountName();

    void chooseGoogleAccount();
}
