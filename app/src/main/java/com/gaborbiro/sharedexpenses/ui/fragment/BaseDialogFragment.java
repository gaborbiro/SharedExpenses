package com.gaborbiro.sharedexpenses.ui.fragment;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialog;
import android.support.v7.app.AppCompatDialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.gaborbiro.sharedexpenses.App;
import com.gaborbiro.sharedexpenses.AppPrefs;
import com.gaborbiro.sharedexpenses.UserPrefs;
import com.gaborbiro.sharedexpenses.service.ExpensesService;
import com.gaborbiro.sharedexpenses.ui.activity.ProgressScreen;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.Unbinder;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public abstract class BaseDialogFragment extends AppCompatDialogFragment {

    private Unbinder unbinder;

    @Inject protected App app;
    @Inject ExpensesService service;
    @Inject protected UserPrefs userPrefs;
    @Inject protected AppPrefs appPrefs;
    @Inject ProgressScreen progressScreen;

    protected abstract void inject();

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        unbinder = ButterKnife.bind(this, view);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        inject();
    }

    @Override
    public @NonNull
    Dialog onCreateDialog(Bundle savedInstanceState) {
        return new BottomSheetDialog(getContext(), getTheme()) {

            @Override
            public void setContentView(View view) {
                super.setContentView(view);
                onContentViewSet(view);
            }
        };
    }

    <O> Observable<O> prepare(Observable<O> observable) {
        return observable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(() -> progressScreen.showProgress())
                .doAfterTerminate(() -> progressScreen.hideProgress())
                .doOnError(throwable -> {
                    progressScreen.hideProgress();
                    log(throwable);
                });
    }

    private void log(Throwable t) {
        Log.e(getClass().getSimpleName(), t.getMessage(), t);
    }

    @Override
    public void onDestroyView() {
        if (unbinder != null) {
            unbinder.unbind();
        }
        super.onDestroyView();
    }

    abstract void onContentViewSet(View view);
}
