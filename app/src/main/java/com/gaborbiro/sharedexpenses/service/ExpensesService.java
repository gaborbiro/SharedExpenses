package com.gaborbiro.sharedexpenses.service;

import com.gaborbiro.sharedexpenses.api.ExpenseApi;
import com.gaborbiro.sharedexpenses.model.ExpenseItem;

import java.io.IOException;

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
            } catch (IOException e) {
                emitter.onError(e);
            } finally {
                emitter.onCompleted();
            }
        }, Emitter.BackpressureMode.NONE);
    }

    public Observable<String[]> getTenantNames() {
        return Observable.create(emitter -> {
            try {
                emitter.onNext(expenseApi.getTenantNames());
            } catch (IOException e) {
                emitter.onError(e);
            } finally {
                emitter.onCompleted();
            }
        }, Emitter.BackpressureMode.NONE);
    }

    public Observable<Void> delete(final ExpenseItem expense) {
        return Observable.create(emitter -> {
            try {
                expenseApi.deleteExpense(expense.index);
            } catch (IOException e) {
                emitter.onError(e);
            } finally {
                emitter.onCompleted();
            }
        }, Emitter.BackpressureMode.NONE);
    }

    public Observable<Void> insert(final ExpenseItem expense) {
        return Observable.create(emitter -> {
            try {
                expenseApi.insertExpense(expense);
            } catch (IOException e) {
                emitter.onError(e);
            } finally {
                emitter.onCompleted();
            }
        }, Emitter.BackpressureMode.NONE);
    }

    public Observable<Void> update(final ExpenseItem expense) {
        return Observable.create(emitter -> {
            try {
                expenseApi.updateExpense(expense);
            } catch (IOException e) {
                emitter.onError(e);
            } finally {
                emitter.onCompleted();
            }
        }, Emitter.BackpressureMode.NONE);
    }
}
