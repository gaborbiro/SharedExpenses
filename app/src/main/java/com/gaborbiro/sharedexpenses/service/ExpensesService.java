package com.gaborbiro.sharedexpenses.service;

import android.content.IntentSender;
import android.graphics.Bitmap;
import android.net.Uri;

import com.gaborbiro.sharedexpenses.api.ExpenseApi;
import com.gaborbiro.sharedexpenses.model.ExpenseItem;

import javax.inject.Inject;
import javax.inject.Singleton;

import rx.Emitter;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

@Singleton
public class ExpensesService {

    @Inject ExpenseApi expenseApi;
    private PublishSubject<ReceiptEvent> receiptEventBroadcast;

    @Inject
    public ExpensesService() {
        receiptEventBroadcast = PublishSubject.create();
    }

    public void sendReceiptSelectEvent() {
        receiptEventBroadcast.onNext(ReceiptEvent.builder().type(ReceiptEvent.Type.SELECT).build());
    }

    public void sendReceiptDeletedEvent() {
        receiptEventBroadcast.onNext(ReceiptEvent.builder().type(ReceiptEvent.Type.DELETED).build());
    }

    public void sendReceiptSelectedEvent(Uri receiptFileUri) {
        receiptEventBroadcast.onNext(ReceiptEvent.builder().type(ReceiptEvent.Type.SELECTED).receiptUri(receiptFileUri).build());
    }

    public Observable<ReceiptEvent> getReceiptEventBroadcast() {
        return receiptEventBroadcast.asObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
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

    public Observable<IntentSender> uploadReceipt(Bitmap bmp) {
        return Observable.create(
                uriEmitter -> Observable.create(
                        (Action1<Emitter<IntentSender>>) stringEmitter -> expenseApi.uploadFile(bmp, stringEmitter),
                        Emitter.BackpressureMode.NONE
                )
                .subscribe(
                        intentSender -> uriEmitter.onNext(intentSender),
                        throwable -> uriEmitter.onError(new Exception("bad uri"))),
                Emitter.BackpressureMode.NONE);
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
