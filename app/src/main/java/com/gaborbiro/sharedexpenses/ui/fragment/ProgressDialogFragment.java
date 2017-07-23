package com.gaborbiro.sharedexpenses.ui.fragment;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;

import com.gaborbiro.sharedexpenses.R;

public class ProgressDialogFragment extends DialogFragment {

    public static void show(AppCompatActivity activity, String tag) {
        new ProgressDialogFragment().show(activity.getSupportFragmentManager(), tag);
    }

    public static void hide(AppCompatActivity activity, String tag) {
        activity.getSupportFragmentManager().beginTransaction().
                remove(activity.getSupportFragmentManager().findFragmentByTag(tag)).commitAllowingStateLoss();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCancelable(false);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        ProgressDialog dialog = new ProgressDialog(getActivity(), getTheme());
        dialog.setMessage(getString(R.string.please_wait));
        dialog.setIndeterminate(true);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        return dialog;
    }
}
