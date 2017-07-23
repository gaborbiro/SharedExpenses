package com.gaborbiro.sharedexpenses.ui.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.widget.Toast;

import com.gaborbiro.sharedexpenses.ui.fragment.ProgressDialogFragment;

import rx.Observable;

public abstract class ProgressActivity extends BaseActivity implements ProgressScreen {

    private static final String TAG = "progress";

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
//            runOnUiThread(() -> ProgressDialogFragment.newInstance().show(getFragmentManager(), TAG));
        }
    }

    @Override
    public void hideProgress() {
        if (--progressCount == 0) {
//            runOnUiThread(() -> getFragmentManager()
//                    .beginTransaction()
//                    .remove(getFragmentManager().findFragmentByTag(TAG))
//                    .commit());
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
    protected <O> Observable<O> prepare(Observable<O> observable) {
        return super.prepare(observable)
                .doOnSubscribe(this::showProgress)
                .doAfterTerminate(this::hideProgress);
    }
}
