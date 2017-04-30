package com.gaborbiro.sharedexpenses.tasks;

import android.os.AsyncTask;

import com.gaborbiro.sharedexpenses.service.ExpenseApi;
import com.gaborbiro.sharedexpenses.service.ExpenseApiImpl;
import com.gaborbiro.sharedexpenses.ui.activity.GoogleApiScreen;
import com.gaborbiro.sharedexpenses.ui.activity.MainScreen;
import com.gaborbiro.sharedexpenses.ui.activity.ProgressScreen;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;

import java.io.IOException;

abstract class BaseExpendesTask<I, O> extends AsyncTask<I, Void, O> {

    ProgressScreen progressScreen;
    GoogleApiScreen googleApiScreen;
    MainScreen mainScreen;
    ExpenseApi service;
    private IOException lastError;

    BaseExpendesTask(GoogleApiScreen googleApiScreen, ProgressScreen progressScreen, MainScreen mainScreen, GoogleAccountCredential credential) {
        this.googleApiScreen = googleApiScreen;
        this.progressScreen = progressScreen;
        this.mainScreen = mainScreen;
        this.service = new ExpenseApiImpl(credential);
    }

    @Override
    protected void onPreExecute() {
        progressScreen.showProgress();
    }

    @Override
    protected final O doInBackground(I... params) {
        try {
            return work(params);
        } catch (IOException e) {
            lastError = e;
            cancel(true);
        }
        return null;
    }

    protected abstract O work(I... params) throws IOException;

    @Override
    protected void onPostExecute(O response) {
        progressScreen.hideProgress();
    }

    @Override
    protected void onCancelled() {
        progressScreen.hideProgress();
        if (lastError != null) {
            if (lastError instanceof GooglePlayServicesAvailabilityIOException) {
                googleApiScreen.showGooglePlayServicesAvailabilityErrorDialog(
                        ((GooglePlayServicesAvailabilityIOException) lastError)
                                .getConnectionStatusCode());
            } else if (lastError instanceof UserRecoverableAuthIOException) {
                googleApiScreen.requestAuthorization(((UserRecoverableAuthIOException) lastError).getIntent());
            } else {
                progressScreen.error("The following error occurred:\n" + lastError.getMessage());
            }
        } else {
            progressScreen.error("Request cancelled.");
        }
    }
}
