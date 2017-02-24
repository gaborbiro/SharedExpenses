package com.gaborbiro.sharedexpenses.tasks;

import android.os.AsyncTask;

import com.gaborbiro.sharedexpenses.service.ExpenseApi;
import com.gaborbiro.sharedexpenses.ui.screen.MainScreen;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;

abstract class BaseSheetsTask<I, O> extends AsyncTask<I, Void, O> {

    MainScreen screen;
    ExpenseApi service;
    Exception mLastError = null;

    BaseSheetsTask(MainScreen screen, GoogleAccountCredential credential) {
        this.screen = screen;
        this.service = new ExpenseApi(credential);
    }

    @Override
    protected void onPreExecute() {
        screen.showProgress();
    }

    @Override
    protected void onPostExecute(O response) {
        screen.hideProgress();
    }

    @Override
    protected void onCancelled() {
        screen.hideProgress();
        if (mLastError != null) {
            if (mLastError instanceof GooglePlayServicesAvailabilityIOException) {
                screen.showGooglePlayServicesAvailabilityErrorDialog(
                        ((GooglePlayServicesAvailabilityIOException) mLastError)
                                .getConnectionStatusCode());
            } else if (mLastError instanceof UserRecoverableAuthIOException) {
                screen.requestAuthorization(((UserRecoverableAuthIOException) mLastError).getIntent());
            } else {
                screen.error("The following error occurred:\n" + mLastError.getMessage());
            }
        } else {
            screen.error("Request cancelled.");
        }
    }
}
