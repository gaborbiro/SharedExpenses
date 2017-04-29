package com.gaborbiro.sharedexpenses.ui.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.gaborbiro.sharedexpenses.R;
import com.gaborbiro.sharedexpenses.UserPrefs;
import com.gaborbiro.sharedexpenses.model.ExpenseItem;
import com.gaborbiro.sharedexpenses.tasks.DeleteExpensesTask;
import com.gaborbiro.sharedexpenses.tasks.InsertExpensesTask;
import com.gaborbiro.sharedexpenses.tasks.UpdateExpensesTask;
import com.gaborbiro.sharedexpenses.ui.screen.MainScreen;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EditExpenseDialog extends MaterialDialog {

    private static final NumberFormat LOCAL_CURRENCY = DecimalFormat.getCurrencyInstance();

    public static void show(@NonNull Context context, final MainScreen screen, final GoogleAccountCredential credential) {
        new EditExpenseDialog.Builder(context, screen, credential).build().show();
    }

    public static void show(@NonNull Context context, final MainScreen screen, final GoogleAccountCredential credential, ExpenseItem expenseItem) {
        new EditExpenseDialog.Builder(context, screen, credential, expenseItem).build().show();
    }

    protected EditExpenseDialog(Builder builder) {
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

        private ExpenseItem expenseItem;

        private MainScreen screen;
        private GoogleAccountCredential credential;

        private View layout;
        private EditText descriptionField;
        private EditText priceField;
        private TextView currencyField;
        private EditText commentField;
        private DatePicker datePicker;

        public Builder(@NonNull Context context, final MainScreen screen, final GoogleAccountCredential credential) {
            this(context, screen, credential, null);
        }

        public Builder(@NonNull Context context, final MainScreen screen, final GoogleAccountCredential credential, final ExpenseItem expenseItem) {
            super(context);
            this.expenseItem = expenseItem;
            this.screen = screen;
            this.credential = credential;

            layout = LayoutInflater.from(context).inflate(R.layout.expense_details_dialog, null);
            descriptionField = (EditText) layout.findViewById(R.id.description);
            priceField = (EditText) layout.findViewById(R.id.price);
            currencyField = (TextView) layout.findViewById(R.id.currency);
            commentField = (EditText) layout.findViewById(R.id.comment);
            datePicker = (DatePicker) layout.findViewById(R.id.date_picker);

            String selectedTenant = UserPrefs.getSelectedTenant();

            if (!TextUtils.isEmpty(selectedTenant)) {
                title(context.getString(R.string.add_new_expense_as, selectedTenant));
            } else {
                title(context.getString(R.string.add_new_expense));
            }

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
            neutralText(android.R.string.cancel);
            onNeutral(new MaterialDialog.SingleButtonCallback() {
                @Override
                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                    dialog.dismiss();
                }
            });
            if (expenseItem != null) {
                negativeText(R.string.delete);
                onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        new DeleteExpensesTask(screen, credential).execute(expenseItem);
                        dialog.dismiss();
                    }
                });

            }
            autoDismiss(false);

            Calendar now = Calendar.getInstance();

            if (expenseItem != null) {
                now.setTime(expenseItem.date);
                descriptionField.setText(expenseItem.description);
                commentField.setText(expenseItem.comment);
                String[] currencyPrice = splitCurrency(expenseItem.price);
                currencyField.setText(currencyPrice[0] != null ? currencyPrice[0] : currencyPrice[2]);
                priceField.setText(currencyPrice[1]);
            } else {
                currencyField.setText(LOCAL_CURRENCY.getCurrency().getSymbol());
            }
            datePicker.init(now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH), null);
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
            String currency = currencyField.getText().toString();
            String price = priceField.getText().toString();
            String comment = commentField.getText().toString();
            comment = TextUtils.isEmpty(comment) ? null : comment;
            Calendar selectedDate = Calendar.getInstance();
            if (expenseItem != null) {
                selectedDate.setTime(expenseItem.date);
            }
            selectedDate.set(Calendar.YEAR, datePicker.getYear());
            selectedDate.set(Calendar.MONTH, datePicker.getMonth());
            selectedDate.set(Calendar.DATE, datePicker.getDayOfMonth());

            if (expenseItem == null) {
                ExpenseItem entry = new ExpenseItem(buyer, description, currency + price, selectedDate.getTime(), comment);
                new InsertExpensesTask(screen, credential).execute(entry);
            } else {
                String[] currencyPrice = splitCurrency(expenseItem.price);
                currencyPrice[0] = currency;
                currencyPrice[1] = price;
                ExpenseItem entry = new ExpenseItem(expenseItem.index, expenseItem.buyer, description, concat(currencyPrice), selectedDate.getTime(), comment);

                if (!entry.equals(expenseItem)) {
                    new UpdateExpensesTask(screen, credential).execute(entry);
                } else {
                    screen.toast(R.string.nothing_changed);
                }
            }
            dialog.dismiss();
        }
    }

    private static String[] splitCurrency(String price) {
        Pattern p = Pattern.compile("[\\d]+");
        Matcher m = p.matcher(price);

        if (m.find()) {
            int startIndex = m.start();
            int endIndex = -1;

            if (startIndex == 0) {
                // postfix currency
                m = p.matcher(new StringBuffer(price).reverse().toString());
                endIndex = m.start();
            } else {
                // prefix currency
                endIndex = price.length();
            }
            return new String[]{price.substring(0, startIndex), price.substring(startIndex, endIndex), price.substring(endIndex)};
        } else {
            return new String[]{null, price, null};
        }
    }

    private static String concat(String[] texts) {
        StringBuffer buffer = new StringBuffer();

        for (String text : texts) {
            if (!TextUtils.isEmpty(text)) {
                buffer.append(text);
            }
        }
        return buffer.toString();
    }
}
