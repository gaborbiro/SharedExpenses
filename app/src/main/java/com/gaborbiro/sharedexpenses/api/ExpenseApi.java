package com.gaborbiro.sharedexpenses.api;

import com.gaborbiro.sharedexpenses.model.ExpenseItem;

import java.io.IOException;

public interface ExpenseApi {

    ExpenseItem[] fetchExpenses() throws IOException;

    void insertExpense(ExpenseItem expense) throws IOException;

    void updateExpense(ExpenseItem expense) throws IOException;

    void deleteExpense(int index) throws IOException;

    String[] getTenantNames() throws IOException;
}
