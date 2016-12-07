package com.gaborbiro.sharedexpenses.view;

import android.os.AsyncTask;

import com.gaborbiro.sharedexpenses.Constants;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;

public abstract class BaseExpenseTask<I, O> extends AsyncTask<I, Void, O> {

    private MainView view;
    private GoogleApiPresenter googleApiPresenter;
    com.google.api.services.sheets.v4.Sheets service = null;
    Exception mLastError = null;

    public BaseExpenseTask(MainView view, GoogleApiPresenter googleApiPresenter) {
        this.view = view;
        this.googleApiPresenter = googleApiPresenter;
        this.service = getSheetsService();
    }

    public com.google.api.services.sheets.v4.Sheets getSheetsService() {
        HttpTransport transport = AndroidHttp.newCompatibleTransport();
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
        return new com.google.api.services.sheets.v4.Sheets.Builder(
                transport, jsonFactory, googleApiPresenter.getCredential())
                .setApplicationName(Constants.GOOGLE_APP_NAME)
                .build();
    }

    @Override
    protected void onPreExecute() {
        view.showProgress();
    }

    @Override
    protected void onPostExecute(O response) {
        view.hideProgress();
    }

    @Override
    protected void onCancelled() {
        view.hideProgress();
        if (mLastError != null) {
            if (mLastError instanceof GooglePlayServicesAvailabilityIOException) {
                googleApiPresenter.showGooglePlayServicesAvailabilityErrorDialog(
                        ((GooglePlayServicesAvailabilityIOException) mLastError)
                                .getConnectionStatusCode());
            } else if (mLastError instanceof UserRecoverableAuthIOException) {
                view.startActivityForResult(
                        ((UserRecoverableAuthIOException) mLastError).getIntent(),
                        GoogleApiPresenter.REQUEST_AUTHORIZATION);
            } else {
                view.setOutput("The following error occurred:\n"
                        + mLastError.getMessage());
            }
        } else {
            view.setOutput("Request cancelled.");
        }
    }
}
