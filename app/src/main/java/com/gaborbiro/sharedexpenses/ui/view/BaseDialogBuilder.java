package com.gaborbiro.sharedexpenses.ui.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.afollestad.materialdialogs.MaterialDialog;
import com.gaborbiro.sharedexpenses.UserPrefs;
import com.gaborbiro.sharedexpenses.service.ExpensesService;
import com.gaborbiro.sharedexpenses.ui.activity.ProgressScreen;

import javax.inject.Inject;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public abstract class BaseDialogBuilder extends MaterialDialog.Builder {

    public static final String TAG = BaseDialogBuilder.class.getSimpleName();

    @Inject UserPrefs userPrefs;
    @Inject ExpensesService service;
    @Inject ProgressScreen progressScreen;

    public BaseDialogBuilder(@NonNull Context context) {
        super(context);
        inject();
    }

    protected abstract void inject();

    protected <O> Observable<O> prepare(Observable<O> observable) {
        return observable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(progressScreen::showProgress)
                .doAfterTerminate(progressScreen::hideProgress);
    }

    protected void log(Throwable t) {
        Log.e(TAG, t.getMessage(), t);
    }
}
