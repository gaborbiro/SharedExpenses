package com.gaborbiro.sharedexpenses.tasks;

import android.text.TextUtils;

import com.gaborbiro.sharedexpenses.AppPrefs;
import com.gaborbiro.sharedexpenses.UserPrefs;
import com.gaborbiro.sharedexpenses.service.ExpenseApi;
import com.gaborbiro.sharedexpenses.ui.activity.GoogleApiScreen;
import com.gaborbiro.sharedexpenses.ui.activity.MainScreen;
import com.gaborbiro.sharedexpenses.ui.activity.ProgressScreen;

import java.io.IOException;
import java.util.Arrays;

import javax.inject.Inject;

public class FetchTenantNamesTask extends BaseExpensesTask<Void, String[]> {

    @Inject
    public FetchTenantNamesTask(AppPrefs appPrefs, UserPrefs userPrefs, GoogleApiScreen googleApiScreen, ProgressScreen progressScreen, MainScreen mainScreen, ExpenseApi expenseApi) {
        super(appPrefs, userPrefs, googleApiScreen, progressScreen, mainScreen, expenseApi);
    }


    @Override
    protected String[] work(Void... params) throws IOException {
        return service.getTenantNames();
    }

    @Override
    protected void onPostExecute(String[] response) {
        super.onPostExecute(response);
        appPrefs.setTenants(response);

        String selectedTenant = userPrefs.getSelectedTenant();
        if (TextUtils.isEmpty(selectedTenant) || !TextUtils.isEmpty(selectedTenant) && Arrays.binarySearch(appPrefs.getTenants(), selectedTenant) < 0) {
            mainScreen.chooseTenant();
        }
    }
}