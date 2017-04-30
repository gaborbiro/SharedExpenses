package com.gaborbiro.sharedexpenses.tasks;

import com.gaborbiro.sharedexpenses.model.ExpenseItem;
import com.gaborbiro.sharedexpenses.ui.activity.GoogleApiScreen;
import com.gaborbiro.sharedexpenses.ui.activity.MainScreen;
import com.gaborbiro.sharedexpenses.ui.activity.ProgressScreen;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;

import java.io.IOException;

public class FetchExpensesTask extends BaseExpendesTask<Void, ExpenseItem[]> {

    public FetchExpensesTask(GoogleApiScreen googleApiScreen, ProgressScreen progressScreen, MainScreen mainScreen, GoogleAccountCredential credential) {
        super(googleApiScreen, progressScreen, mainScreen, credential);
    }

    @Override
    protected ExpenseItem[] work(Void... params) throws IOException {
        return service.fetchExpenses();
    }

    @Override
    protected void onPostExecute(ExpenseItem[] output) {
        super.onPostExecute(output);
        mainScreen.setExpenses(output);
    }
}