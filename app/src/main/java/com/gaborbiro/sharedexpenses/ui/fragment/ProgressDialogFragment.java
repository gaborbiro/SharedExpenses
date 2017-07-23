package com.gaborbiro.sharedexpenses.ui.fragment;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.os.Bundle;

import com.gaborbiro.sharedexpenses.R;

public class ProgressDialogFragment extends DialogFragment {

    public static ProgressDialogFragment newInstance() {
        return new ProgressDialogFragment();
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
