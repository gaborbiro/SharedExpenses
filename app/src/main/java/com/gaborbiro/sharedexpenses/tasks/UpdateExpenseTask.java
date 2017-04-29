package com.gaborbiro.sharedexpenses.tasks;

import com.gaborbiro.sharedexpenses.R;
import com.gaborbiro.sharedexpenses.model.ExpenseItem;
import com.gaborbiro.sharedexpenses.ui.screen.MainScreen;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;

public class UpdateExpenseTask extends BaseSheetsTask<ExpenseItem, Integer> {

    public UpdateExpenseTask(MainScreen screen, GoogleAccountCredential credential) {
        super(screen, credential);
    }

    @Override
    protected Integer doInBackground(ExpenseItem... params) {
        int modifiedRowCount = 0;
        for (ExpenseItem expense : params) {
            try {
                service.updateExpense(expense);
                modifiedRowCount++;
            } catch (Exception e) {
                mLastError = e;
                cancel(true);
            }
        }
        return modifiedRowCount;
    }

    @Override
    protected void onPostExecute(Integer response) {
        super.onPostExecute(response);
        screen.toast(R.string.updated, response);
    }
}
