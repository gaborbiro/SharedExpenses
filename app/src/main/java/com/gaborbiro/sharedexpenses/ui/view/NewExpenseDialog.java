package com.gaborbiro.sharedexpenses.ui.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.DatePicker;
import android.widget.EditText;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.gaborbiro.sharedexpenses.R;
import com.gaborbiro.sharedexpenses.UserPrefs;
import com.gaborbiro.sharedexpenses.model.ExpenseItem;
import com.gaborbiro.sharedexpenses.tasks.InsertSheetsTask;
import com.gaborbiro.sharedexpenses.ui.screen.MainScreen;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Locale;

public class NewExpenseDialog extends MaterialDialog {

    protected NewExpenseDialog(Builder builder) {
        super(builder);
    }

    //             ____        _ _     _
    //            |  _ \      (_) |   | |
    //            | |_) |_   _ _| | __| | ___ _ __
    //            |  _ <| | | | | |/ _` |/ _ \ '__|
    //            | |_) | |_| | | | (_| |  __/ |
    //            |____/ \__,_|_|_|\__,_|\___|_|
    //
    public static class Builder extends MaterialDialog.Builder {

        private MainScreen screen;
        private GoogleAccountCredential credential;

        private View layout;
        private EditText descriptionField;
        private EditText priceField;
        private EditText commentField;
        private DatePicker datePicker;

        public Builder(@NonNull Context context, final MainScreen screen, final GoogleAccountCredential credential) {
            super(context);
            this.screen = screen;
            this.credential = credential;

            layout = LayoutInflater.from(context).inflate(R.layout.new_expense_dialog, null);
            descriptionField = (EditText) layout.findViewById(R.id.description);
            priceField = (EditText) layout.findViewById(R.id.price);
            commentField = (EditText) layout.findViewById(R.id.comment);
            datePicker = (DatePicker) layout.findViewById(R.id.date_picker);

            String selectedTenant = UserPrefs.getSelectedTenant();

            if (!TextUtils.isEmpty(selectedTenant)) {
                title(context.getString(R.string.add_new_expense_as, selectedTenant));
            } else {
                title(context.getString(R.string.add_new_expense));
            }

            Calendar now = Calendar.getInstance();
            datePicker.init(now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH), null);

            descriptionField.post(new Runnable() {
                @Override
                public void run() {
                    InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(descriptionField, InputMethodManager.SHOW_IMPLICIT);
                }
            });
            customView(layout, false);

            positiveText(R.string.submit);
            onPositive(new MaterialDialog.SingleButtonCallback() {
                @Override
                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                    onSubmitDialog(dialog);
                }
            });
            negativeText(android.R.string.cancel);
            onNegative(new MaterialDialog.SingleButtonCallback() {
                @Override
                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                    dialog.dismiss();
                }
            });
            autoDismiss(false);
        }

        private void onSubmitDialog(@NonNull MaterialDialog dialog) {
            String buyer = UserPrefs.getSelectedTenant();
            String description = descriptionField.getText().toString();

            if (TextUtils.isEmpty(description)) {
                screen.toast(R.string.error_description);
                return;
            }

            if (TextUtils.isEmpty(priceField.getText().toString())) {
                screen.toast(R.string.error_price);
                return;
            }
            String price = DecimalFormat.getCurrencyInstance(Locale.UK).format(Double.valueOf(priceField.getText().toString()));
            String comment = commentField.getText().toString();
            Calendar selectedDate = Calendar.getInstance();
            selectedDate.set(Calendar.DATE, datePicker.getDayOfMonth());
            selectedDate.set(Calendar.MONTH, datePicker.getMonth());
            selectedDate.set(Calendar.YEAR, datePicker.getYear());
            ExpenseItem entry = new ExpenseItem(buyer, description, price, selectedDate.getTime(), comment);
            new InsertSheetsTask(screen, credential).execute(entry);
            dialog.dismiss();
        }
    }
}
