package com.gaborbiro.sharedexpenses.ui.view;

import android.util.Log;

import com.afollestad.materialdialogs.MaterialDialog;
import com.gaborbiro.sharedexpenses.UserPrefs;
import com.gaborbiro.sharedexpenses.service.ExpensesService;
import com.gaborbiro.sharedexpenses.ui.activity.ProgressScreen;
import com.gaborbiro.sharedexpenses.ui.activity.WebScreen;

import javax.inject.Inject;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public abstract class BaseMaterialDialog extends MaterialDialog {

    private static final String TAG = "BaseMaterialDialog";

    @Inject UserPrefs userPrefs;
    @Inject ExpensesService service;
    @Inject ProgressScreen progressScreen;
    @Inject WebScreen webScreen;

    BaseMaterialDialog(Builder builder) {
        super(builder);
        inject();
    }

    protected abstract void inject();

    <O> Observable<O> prepare(Observable<O> observable) {
        return observable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(progressScreen::showProgress)
                .doOnTerminate(progressScreen::hideProgress)
                .doOnError(this::log);
    }

    private void log(Throwable t) {
        Log.e(TAG, t.getMessage(), t);
    }
}
