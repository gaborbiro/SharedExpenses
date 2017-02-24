package com.gaborbiro.sharedexpenses.ui.presenter;

import android.database.Observable;

public class BasePresenter {

    protected <O> Observable<O> prepare(Observable<O> observable) {
        return observable
                .subscribeOn(Schedulers.io())
                .observeOn(presenterTools.schedulerProvider().mainThread());
    }
}
