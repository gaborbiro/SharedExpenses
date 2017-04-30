package com.gaborbiro.sharedexpenses.tasks;

import com.gaborbiro.sharedexpenses.R;
import com.gaborbiro.sharedexpenses.model.ExpenseItem;
import com.gaborbiro.sharedexpenses.ui.activity.GoogleApiScreen;
import com.gaborbiro.sharedexpenses.ui.activity.MainScreen;
import com.gaborbiro.sharedexpenses.ui.activity.ProgressScreen;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;

import java.io.IOException;

public class DeleteExpensesTask extends BaseExpendesTask<ExpenseItem, Integer> {
    public DeleteExpensesTask(GoogleApiScreen googleApiScreen, ProgressScreen progressScreen, MainScreen mainScreen, GoogleAccountCredential credential) {
        super(googleApiScreen, progressScreen, mainScreen, credential);
    }

    @Override
    protected Integer work(ExpenseItem... params) throws IOException {
        int modifiedRowCount = 0;
        for (ExpenseItem expense : params) {
            service.deleteExpense(expense.index);
            modifiedRowCount++;
        }
        return modifiedRowCount;
    }

    @Override
    protected void onPostExecute(Integer response) {
        super.onPostExecute(response);
        progressScreen.toast(R.string.deleted, response);
        mainScreen.update();
    }
}
