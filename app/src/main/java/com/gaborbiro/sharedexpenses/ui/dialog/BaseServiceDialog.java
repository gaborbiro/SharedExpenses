package com.gaborbiro.sharedexpenses.ui.dialog;

import android.graphics.Color;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.gaborbiro.sharedexpenses.UserPrefs;
import com.gaborbiro.sharedexpenses.service.ExpensesService;
import com.gaborbiro.sharedexpenses.ui.activity.ProgressScreen;
import com.gaborbiro.sharedexpenses.ui.activity.WebScreen;

import javax.inject.Inject;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public abstract class BaseServiceDialog extends MaterialDialog {

    @Inject UserPrefs userPrefs;
    @Inject ExpensesService service;
    @Inject ProgressScreen progressScreen;
    @Inject WebScreen webScreen;

    BaseServiceDialog(Builder builder) {
        super(builder);
        inject();
    }

    protected abstract void inject();

    <O> Observable<O> prepare(Observable<O> observable) {
        return observable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(result -> progressScreen.showProgress())
                .doAfterTerminate(progressScreen::hideProgress)
                .doOnError(this::log);
    }

    Completable prepare(Completable completable) {
        return completable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(result -> progressScreen.showProgress())
                .doAfterTerminate(progressScreen::hideProgress)
                .doOnError(this::log);
    }

    private void log(Throwable t) {
        Log.e(getClass().getSimpleName(), t.getMessage(), t);
    }

    void showError(String message) {
        Snackbar snackbar = Snackbar.make(getView(), message, Snackbar.LENGTH_LONG);
        snackbar.setActionTextColor(Color.RED);
        View sbView = snackbar.getView();
        TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(Color.YELLOW);
        snackbar.show();
    }
}
