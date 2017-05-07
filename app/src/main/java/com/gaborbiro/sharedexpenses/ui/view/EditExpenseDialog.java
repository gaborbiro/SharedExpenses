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
import com.gaborbiro.sharedexpenses.App;
import com.gaborbiro.sharedexpenses.R;
import com.gaborbiro.sharedexpenses.model.ExpenseItem;
import com.gaborbiro.sharedexpenses.ui.activity.ProgressScreen;
import com.gaborbiro.sharedexpenses.ui.activity.WebScreen;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import rx.functions.Actions;

public class EditExpenseDialog extends MaterialDialog {

    private static final NumberFormat LOCAL_CURRENCY = DecimalFormat.getCurrencyInstance();

    public static void show(@NonNull Context context) {
        new EditExpenseDialogBuilder(context, null).build().show();
    }

    public static void show(@NonNull Context context, ExpenseItem expenseItem) {
        new EditExpenseDialogBuilder(context, expenseItem).build().show();
    }

    protected EditExpenseDialog(EditExpenseDialogBuilder builder) {
        super(builder);
    }

    public static class EditExpenseDialogBuilder extends BaseDialogBuilder {

        @Inject ProgressScreen progressScreen;
        @Inject WebScreen webScreen;

        private View layout;
        private EditText descriptionField;
        private EditText priceField;
        private TextView currencyField;
        private EditText commentField;
        private DatePicker datePicker;

        private final ExpenseItem expenseItem;

        public EditExpenseDialogBuilder(Context context, final ExpenseItem expenseItem) {
            super(context);
            this.expenseItem = expenseItem;

            layout = LayoutInflater.from(context).inflate(R.layout.expense_details_dialog, null);
            descriptionField = (EditText) layout.findViewById(R.id.description);
            priceField = (EditText) layout.findViewById(R.id.price);
            currencyField = (TextView) layout.findViewById(R.id.currency);
            commentField = (EditText) layout.findViewById(R.id.comment);
            datePicker = (DatePicker) layout.findViewById(R.id.date_picker);

            String selectedTenant = userPrefs.getSelectedTenant();

            if (!TextUtils.isEmpty(selectedTenant)) {
                title(context.getString(R.string.add_new_expense_as, selectedTenant));
            } else {
                title(context.getString(R.string.add_new_expense));
            }

            descriptionField.post(() -> {
                InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(descriptionField, InputMethodManager.SHOW_IMPLICIT);
            });
            customView(layout, false);

            positiveText(R.string.submit);
            onPositive((dialog, which) -> onSubmitDialog(dialog));
            neutralText(android.R.string.cancel);
            onNeutral((dialog, which) -> dialog.dismiss());
            if (expenseItem != null) {
                negativeText(R.string.delete);
                onNegative((dialog, which) -> prepare(service.delete(expenseItem))
                        .doOnTerminate(() -> {
                            progressScreen.toast(R.string.deleted, 1);
                            webScreen.update();
                            dialog.dismiss();
                        })
                        .subscribe(aVoid -> Actions.empty(), throwable -> log(throwable)));
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

        @Override
        protected void inject() {
            App.component.inject(this);
        }

        private void onSubmitDialog(@NonNull MaterialDialog dialog) {
            String buyer = userPrefs.getSelectedTenant();
            String description = descriptionField.getText().toString();

            if (TextUtils.isEmpty(description)) {
                progressScreen.toast(R.string.error_description);
                return;
            }

            if (TextUtils.isEmpty(priceField.getText().toString())) {
                progressScreen.toast(R.string.error_price);
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
                prepare(service.insert(entry))
                        .doOnTerminate(() -> {
                            progressScreen.toast(R.string.inserted, 1);
                            webScreen.update();
                            dialog.dismiss();
                        })
                        .subscribe(aVoid -> Actions.empty(), throwable -> log(throwable));
            } else {
                String[] currencyPrice = splitCurrency(expenseItem.price);
                currencyPrice[0] = currency;
                currencyPrice[1] = price;
                ExpenseItem entry = new ExpenseItem(expenseItem.index, expenseItem.buyer, description, concat(currencyPrice), selectedDate.getTime(), comment);

                if (!entry.equals(expenseItem)) {
                    prepare(service.update(entry))
                            .doOnTerminate(() -> {
                                progressScreen.toast(R.string.updated, 1);
                                webScreen.update();
                                dialog.dismiss();
                            })
                            .subscribe(aVoid -> Actions.empty(), throwable -> log(throwable));
                } else {
                    progressScreen.toast(R.string.nothing_changed);
                }
            }
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
