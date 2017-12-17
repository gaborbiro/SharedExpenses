package com.gaborbiro.sharedexpenses.service;

import com.gaborbiro.sharedexpenses.api.ExpenseApi;
import com.gaborbiro.sharedexpenses.model.ExpenseItem;
import com.gaborbiro.sharedexpenses.model.StatItem;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.Completable;
import io.reactivex.Single;

@Singleton
public class ExpensesService {

    @Inject ExpenseApi expenseApi;

    @Inject
    public ExpensesService() {
    }

    public Single<ExpenseItem[]> getExpenses() {
        return Single.create(emitter -> {
                    try {
                        emitter.onSuccess(expenseApi.fetchExpenses());
                    } catch (Exception e) {
                        emitter.onError(e);
                    }
                }
        );
    }

    public Single<String[]> getTenantNames() {
        return Single.create(emitter -> {
            try {
                emitter.onSuccess(expenseApi.getTenantNames());
            } catch (Exception e) {
                emitter.onError(e);
            }
        });
    }

    public Completable delete(final ExpenseItem expense) {
        return Completable.create(emitter -> {
            try {
                expenseApi.deleteExpense(expense);
                emitter.onComplete();
            } catch (Exception e) {
                emitter.onError(e);
            }
        });
    }

    public Completable insert(final ExpenseItem expense) {
        return Completable.create(emitter -> {
            try {
                expenseApi.insertExpense(expense);
                emitter.onComplete();
            } catch (Exception e) {
                emitter.onError(e);
            }
        });
    }

    public Completable update(final ExpenseItem expense, final ExpenseItem original) {
        return Completable.create(emitter -> {
            try {
                expenseApi.updateExpense(expense, original);
                emitter.onComplete();
            } catch (Exception e) {
                emitter.onError(e);
            }
        });
    }

    public Single<StatItem[]> getStats() {
        return Single.create(emitter -> {
            try {
                emitter.onSuccess(expenseApi.fetchStats());
            } catch (Exception e) {
                emitter.onError(e);
            }
        });
    }
}
