package com.gaborbiro.sharedexpenses.tasks;

import android.content.Intent;
import android.support.annotation.StringRes;
import android.util.Log;

import com.gaborbiro.sharedexpenses.ui.screen.MainScreen;

public class DummyScreen implements MainScreen {
    @Override
    public void chooseTenant() {
    }

    @Override
    public void data(String data) {
        Log.d("DummyScreen", data);
    }

    @Override
    public void showProgress() {
    }

    @Override
    public void hideProgress() {
    }

    @Override
    public void error(String text) {
    }

    @Override
    public void toast(String message) {
    }

    @Override
    public void toast(@StringRes int message) {
    }

    @Override
    public void toast(@StringRes int message, Object... formatArgs) {
    }

    @Override
    public void showGooglePlayServicesAvailabilityErrorDialog(int connectionStatusCode) {
    }

    @Override
    public void requestAuthorization(Intent intent) {
    }

    @Override
    public boolean isGooglePlayServicesAvailable() {
        return false;
    }

    @Override
    public void acquireGooglePlayServices() {
    }

    @Override
    public String getSelectedAccountName() {
        return null;
    }

    @Override
    public void chooseGoogleAccount() {
    }
}
