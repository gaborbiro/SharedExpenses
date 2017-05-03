package com.gaborbiro.sharedexpenses.service;

import com.gaborbiro.sharedexpenses.api.ExpenseApi;
import com.gaborbiro.sharedexpenses.model.ExpenseItem;

import java.io.IOException;

import javax.inject.Inject;

import rx.Observable;

public class ExpensesService {

    @Inject ExpenseApi expenseApi;

    @Inject
    public ExpensesService() {
    }

    public Observable<ExpenseItem[]> getExpenses() {
        return Observable.defer(() -> {
            try {
                return Observable.just(expenseApi.fetchExpenses());
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        });
    }

    public Observable<Void> delete(final ExpenseItem expense) {
        return Observable.create(emitter -> {
            try {
                expenseApi.deleteExpense(expense.index);
            } catch (IOException e) {
                e.printStackTrace();
            }
            emitter.onCompleted();
        });
    }

    public Observable<String[]> getTenantNames() {
        return Observable.defer(() -> {
            try {
                return Observable.just(expenseApi.getTenantNames());
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        });
    }
}
