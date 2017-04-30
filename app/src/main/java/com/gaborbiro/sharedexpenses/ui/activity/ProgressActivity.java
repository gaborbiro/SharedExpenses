package com.gaborbiro.sharedexpenses.ui.activity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.widget.Toast;

import com.gaborbiro.sharedexpenses.R;

public abstract class ProgressActivity extends BaseActivity implements ProgressScreen {

    private ProgressDialog progressDialog;
    private int progressCount;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.please_wait));
    }

    @Override
    public void showProgress() {

        progressCount++;
        progressDialog.show();
    }

    @Override
    public void hideProgress() {
        if (--progressCount <= 0) {
            progressDialog.hide();
            progressCount = 0;
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
}
