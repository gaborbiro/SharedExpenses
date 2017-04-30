package com.gaborbiro.sharedexpenses.service;

import android.os.AsyncTask;

import com.gaborbiro.sharedexpenses.AppPrefs;
import com.gaborbiro.sharedexpenses.UserPrefs;
import com.gaborbiro.sharedexpenses.api.ExpenseApi;
import com.gaborbiro.sharedexpenses.ui.activity.GoogleApiScreen;
import com.gaborbiro.sharedexpenses.ui.activity.ProgressScreen;
import com.gaborbiro.sharedexpenses.ui.activity.WebScreen;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;

import java.io.IOException;

public abstract class BaseExpensesTask<I, O> extends AsyncTask<I, Void, O> {

    protected AppPrefs appPrefs;
    protected UserPrefs userPrefs;
    protected GoogleApiScreen googleApiScreen;
    protected ProgressScreen progressScreen;
    protected WebScreen webScreen;
    protected ExpenseApi service;

    private IOException lastError;

    public BaseExpensesTask(AppPrefs appPrefs, UserPrefs userPrefs, GoogleApiScreen googleApiScreen, ProgressScreen progressScreen, WebScreen webScreen, ExpenseApi service) {
        this.appPrefs = appPrefs;
        this.userPrefs = userPrefs;
        this.googleApiScreen = googleApiScreen;
        this.progressScreen = progressScreen;
        this.webScreen = webScreen;
        this.service = service;
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
