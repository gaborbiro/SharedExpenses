package com.gaborbiro.sharedexpenses.ui.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.widget.Toast;

import com.gaborbiro.sharedexpenses.ui.fragment.ProgressDialogFragment;

import io.reactivex.Observable;
import io.reactivex.Single;

public abstract class ProgressActivity extends BaseActivity implements ProgressScreen {

    private static final String TAG = "ProgressDialogFragment";

    private int progressCount;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        app.setProgressScreen(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        app.setProgressScreen(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        app.setProgressScreen(null);
    }

    @Override
    public void showProgress() {
        progressCount++;
        if (progressCount == 1) {
            runOnUiThread(() -> ProgressDialogFragment.show(this, TAG));
        }
    }

    @Override
    public void hideProgress() {
        if (--progressCount == 0) {
            runOnUiThread(() -> ProgressDialogFragment.hide(this, TAG));
        }
    }

    @Override
    public void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void toast(@StringRes int message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void toast(@StringRes int message, Object... formatArgs) {
        toast(getString(message, formatArgs));
    }

    @Override
    protected <O> Single<O> prepare(Single<O> observable) {
        return super.prepare(observable)
                .doOnSubscribe(disposable -> this.showProgress())
                .doAfterTerminate(this::hideProgress);
    }
}
