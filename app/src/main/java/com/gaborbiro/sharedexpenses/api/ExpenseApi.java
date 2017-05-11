package com.gaborbiro.sharedexpenses.api;

import com.gaborbiro.sharedexpenses.SpreadsheetException;
import com.gaborbiro.sharedexpenses.model.ExpenseItem;

import java.io.IOException;

public interface ExpenseApi {

    ExpenseItem[] fetchExpenses() throws IOException, SpreadsheetException;

    void insertExpense(ExpenseItem expense) throws IOException;

    void updateExpense(ExpenseItem expense, ExpenseItem original) throws Exception;

    void deleteExpense(ExpenseItem expense) throws Exception;

    String[] getTenantNames() throws IOException;
}
