package com.gaborbiro.sharedexpenses.tasks;

import android.text.TextUtils;

import com.gaborbiro.sharedexpenses.UserPrefs;
import com.gaborbiro.sharedexpenses.AppPrefs;
import com.gaborbiro.sharedexpenses.ui.activity.GoogleApiScreen;
import com.gaborbiro.sharedexpenses.ui.activity.MainScreen;
import com.gaborbiro.sharedexpenses.ui.activity.ProgressScreen;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;

import java.io.IOException;
import java.util.Arrays;

public class FetchTenantNamesTask extends BaseExpendesTask<Void, String[]> {

    public FetchTenantNamesTask(GoogleApiScreen googleApiScreen, ProgressScreen progressScreen, MainScreen mainScreen, GoogleAccountCredential credential) {
        super(googleApiScreen, progressScreen, mainScreen, credential);
    }

    @Override
    protected String[] work(Void... params) throws IOException {
        return service.getTenantNames();
    }

    @Override
    protected void onPostExecute(String[] response) {
        super.onPostExecute(response);
        AppPrefs.setTenants(response);

        String selectedTenant = UserPrefs.getSelectedTenant();
        if (TextUtils.isEmpty(selectedTenant) || !TextUtils.isEmpty(selectedTenant) && Arrays.binarySearch(AppPrefs.getTenants(), selectedTenant) < 0) {
            mainScreen.chooseTenant();
        }
    }
}