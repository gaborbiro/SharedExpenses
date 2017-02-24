package com.gaborbiro.sharedexpenses.tasks;

import com.gaborbiro.sharedexpenses.model.ExpenseItem;
import com.gaborbiro.sharedexpenses.ui.HtmlUtil;
import com.gaborbiro.sharedexpenses.ui.screen.MainScreen;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;

public class FetchExpensesTask extends BaseSheetsTask<Void, ExpenseItem[]> {

    public FetchExpensesTask(MainScreen screen, GoogleAccountCredential credential) {
        super(screen, credential);
    }

    @Override
    protected ExpenseItem[] doInBackground(Void... params) {
        try {
            return service.getExpenses();
        } catch (Exception e) {
            mLastError = e;
            cancel(true);
            return null;
        }
    }

    @Override
    protected void onPostExecute(ExpenseItem[] output) {
        super.onPostExecute(output);
        if (output == null || output.length == 0) {
            screen.data("Empty");
        } else {
            screen.data(HtmlUtil.getHtmlTableFromExpense(output));
        }
    }
}