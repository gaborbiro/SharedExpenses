package com.gaborbiro.sharedexpenses.service;

import com.gaborbiro.sharedexpenses.api.ExpenseApi;
import com.gaborbiro.sharedexpenses.model.ExpenseItem;

import java.io.IOException;

import javax.inject.Inject;

import rx.Observable;
import rx.functions.Func0;

public class ExpensesService {

    @Inject ExpenseApi expenseApi;

    @Inject
    public ExpensesService() {
    }

    public Observable<ExpenseItem[]> getExpenses() {
        return Observable.defer(new Func0<Observable<ExpenseItem[]>>() {
            @Override
            public Observable<ExpenseItem[]> call() {
                try {
                    return Observable.just(expenseApi.fetchExpenses());
                } catch (IOException e) {
                    return null;
                }
            }
        });
    }
}
