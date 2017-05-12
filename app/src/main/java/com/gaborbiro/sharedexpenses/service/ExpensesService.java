package com.gaborbiro.sharedexpenses.service;

import android.net.Uri;

import com.gaborbiro.sharedexpenses.api.ExpenseApi;
import com.gaborbiro.sharedexpenses.model.ExpenseItem;

import javax.inject.Inject;
import javax.inject.Singleton;

import rx.Emitter;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

@Singleton
public class ExpensesService {

    @Inject ExpenseApi expenseApi;
    private PublishSubject<Uri> receiptFileBroadcast;

    @Inject
    public ExpensesService() {
        receiptFileBroadcast = PublishSubject.create();
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

    public void onReceiptFileSelected(Uri receiptFileUri) {
        receiptFileBroadcast.onNext(receiptFileUri);
    }

    public Observable<Uri> getReceiptFileSelectedBroadcast() {
        return receiptFileBroadcast.asObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }
}
