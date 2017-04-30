package com.gaborbiro.sharedexpenses.tasks;

import com.gaborbiro.sharedexpenses.AppPrefs;
import com.gaborbiro.sharedexpenses.UserPrefs;
import com.gaborbiro.sharedexpenses.model.ExpenseItem;
import com.gaborbiro.sharedexpenses.service.ExpenseApi;
import com.gaborbiro.sharedexpenses.ui.activity.GoogleApiScreen;
import com.gaborbiro.sharedexpenses.ui.activity.ProgressScreen;
import com.gaborbiro.sharedexpenses.ui.activity.WebScreen;

import java.io.IOException;

import javax.inject.Inject;

public class FetchExpensesTask extends BaseExpensesTask<Void, ExpenseItem[]> {

    @Inject
    public FetchExpensesTask(AppPrefs appPrefs, UserPrefs userPrefs, GoogleApiScreen googleApiScreen, ProgressScreen progressScreen, WebScreen webScreen, ExpenseApi expenseApi) {
        super(appPrefs, userPrefs, googleApiScreen, progressScreen, webScreen, expenseApi);
    }

    @Override
    protected ExpenseItem[] work(Void... params) throws IOException {
        return service.fetchExpenses();
    }

    @Override
    protected void onPostExecute(ExpenseItem[] output) {
        super.onPostExecute(output);
        webScreen.setExpenses(output);
    }
}