package com.gaborbiro.sharedexpenses.tasks;

import android.text.TextUtils;

import com.gaborbiro.sharedexpenses.UserPrefs;
import com.gaborbiro.sharedexpenses.model.Tenants;
import com.gaborbiro.sharedexpenses.ui.screen.MainScreen;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;

import java.io.IOException;
import java.util.Arrays;

public class FetchTenantNamesTask extends BaseSheetsTask<Void, String[]> {

    public FetchTenantNamesTask(MainScreen screen, GoogleAccountCredential credential) {
        super(screen, credential);
    }

    @Override
    protected String[] doInBackground(Void... params) {
        try {
            return service.getTenantNames();
        } catch (IOException e) {
            e.printStackTrace();
            return new String[0];
        }
    }

    @Override
    protected void onPostExecute(String[] response) {
        super.onPostExecute(response);
        Tenants.setTenants(response);

        String selectedTenant = UserPrefs.getSelectedTenant();
        if (TextUtils.isEmpty(selectedTenant) || !TextUtils.isEmpty(selectedTenant) && Arrays.binarySearch(Tenants.getTenants(), selectedTenant) < 0) {
            screen.chooseTenant();
        }
    }
}