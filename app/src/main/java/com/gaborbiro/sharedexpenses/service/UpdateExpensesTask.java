package com.gaborbiro.sharedexpenses.service;

import com.gaborbiro.sharedexpenses.AppPrefs;
import com.gaborbiro.sharedexpenses.R;
import com.gaborbiro.sharedexpenses.UserPrefs;
import com.gaborbiro.sharedexpenses.model.ExpenseItem;
import com.gaborbiro.sharedexpenses.api.ExpenseApi;
import com.gaborbiro.sharedexpenses.ui.activity.GoogleApiScreen;
import com.gaborbiro.sharedexpenses.ui.activity.ProgressScreen;
import com.gaborbiro.sharedexpenses.ui.activity.WebScreen;

import java.io.IOException;

import javax.inject.Inject;

public class UpdateExpensesTask extends BaseExpensesTask<ExpenseItem, Integer> {

    @Inject
    public UpdateExpensesTask(AppPrefs appPrefs, UserPrefs userPrefs, GoogleApiScreen googleApiScreen, ProgressScreen progressScreen, WebScreen webScreen, ExpenseApi expenseApi) {
        super(appPrefs, userPrefs, googleApiScreen, progressScreen, webScreen, expenseApi);
    }

    @Override
    protected Integer work(ExpenseItem... params) throws IOException {
        int modifiedRowCount = 0;
        for (ExpenseItem expense : params) {
            expenseApi.updateExpense(expense);
            modifiedRowCount++;
        }
        return modifiedRowCount;
    }

    @Override
    protected void onPostExecute(Integer response) {
        super.onPostExecute(response);
        progressScreen.toast(R.string.updated, response);
        webScreen.update();
    }
}
