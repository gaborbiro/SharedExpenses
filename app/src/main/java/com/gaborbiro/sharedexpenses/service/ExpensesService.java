package com.gaborbiro.sharedexpenses.service;

import com.gaborbiro.sharedexpenses.api.ExpenseApi;
import com.gaborbiro.sharedexpenses.model.ExpenseItem;

import javax.inject.Inject;

import rx.Emitter;
import rx.Observable;

public class ExpensesService {

    @Inject ExpenseApi expenseApi;

    @Inject
    public ExpensesService() {
    }

    public Observable<ExpenseItem[]> getExpenses() {
        return Observable.create(emitter -> {
            try {
                emitter.onNext(expenseApi.fetchExpenses());
                emitter.onCompleted();
            } catch (Exception e) {
                emitter.onError(e);
            }
        }, Emitter.BackpressureMode.NONE);
    }

    public Observable<String[]> getTenantNames() {
        return Observable.create(emitter -> {
            try {
                emitter.onNext(expenseApi.getTenantNames());
                emitter.onCompleted();
            } catch (Exception e) {
                emitter.onError(e);
            }
        }, Emitter.BackpressureMode.NONE);
    }

    public Observable<Void> delete(final ExpenseItem expense) {
        return Observable.create(emitter -> {
            try {
                expenseApi.deleteExpense(expense);
                emitter.onCompleted();
            } catch (Exception e) {
                emitter.onError(e);
            }
        }, Emitter.BackpressureMode.NONE);
    }

    public Observable<Void> insert(final ExpenseItem expense) {
        return Observable.create(emitter -> {
            try {
                expenseApi.insertExpense(expense);
                emitter.onCompleted();
            } catch (Exception e) {
                emitter.onError(e);
            }
        }, Emitter.BackpressureMode.NONE);
    }

    public Observable<Void> update(final ExpenseItem expense, final ExpenseItem original) {
        return Observable.create(emitter -> {
            try {
                expenseApi.updateExpense(expense, original);
                emitter.onCompleted();
            } catch (Exception e) {
                emitter.onError(e);
            }
        }, Emitter.BackpressureMode.NONE);
    }
}
