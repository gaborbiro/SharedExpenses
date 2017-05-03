package com.gaborbiro.sharedexpenses.ui.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.gaborbiro.sharedexpenses.App;
import com.gaborbiro.sharedexpenses.AppPrefs;
import com.gaborbiro.sharedexpenses.UserPrefs;
import com.gaborbiro.sharedexpenses.service.ExpensesService;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.Unbinder;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

public abstract class BaseActivity extends AppCompatActivity {

    public static final String TAG = BaseActivity.class.getSimpleName();

    private Unbinder unbinder;

    @Inject protected App app;
    @Inject ExpensesService service;
    @Inject protected UserPrefs userPrefs;
    @Inject protected AppPrefs appPrefs;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        inject();
    }

    @Override
    public void onContentChanged() {
        super.onContentChanged();
        unbinder = ButterKnife.bind(this);
    }

    @Override
    protected void onDestroy() {
        if (unbinder != null) {
            unbinder.unbind();
        }
        super.onDestroy();
    }

    protected abstract void inject();

    protected <O> Observable<O> prepare(Observable<O> observable) {
        return observable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .takeUntil(PublishSubject.create())
                .doOnError(this::log);
    }

    private void log(Throwable t) {
        Log.e(TAG, t.getMessage(), t);
    }
}
