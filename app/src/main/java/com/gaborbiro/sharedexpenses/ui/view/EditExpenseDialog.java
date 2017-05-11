package com.gaborbiro.sharedexpenses.ui.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.gaborbiro.sharedexpenses.App;
import com.gaborbiro.sharedexpenses.R;
import com.gaborbiro.sharedexpenses.model.ExpenseItem;
import com.gaborbiro.sharedexpenses.util.StringUtils;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Calendar;

import rx.Observable;
import rx.functions.Actions;

public class EditExpenseDialog extends BaseMaterialDialog {

    private static final NumberFormat LOCAL_CURRENCY = DecimalFormat.getCurrencyInstance();

    private ExpenseItem expenseItem;

    public static void show(@NonNull Context context) {
        new EditExpenseDialogBuilder(context, null).build().show();
    }

    public static void show(@NonNull Context context, @Nullable ExpenseItem expenseItem) {
        new EditExpenseDialogBuilder(context, expenseItem).build().show();
    }

    private EditExpenseDialog(EditExpenseDialogBuilder builder) {
        super(builder);
        this.expenseItem = builder.expenseItem;
    }

    @Override
    protected void inject() {
        App.component.inject(this);
    }

    private static class EditExpenseDialogBuilder extends MaterialDialog.Builder {

        private View layout;
        private EditText descriptionField;
        private EditText priceField;
        private TextView currencyField;
        private EditText commentField;
        private DatePicker datePicker;

        private final ExpenseItem expenseItem;

        @SuppressLint("InflateParams")
        EditExpenseDialogBuilder(Context context, final ExpenseItem expenseItem) {
            super(context);
            this.expenseItem = expenseItem;

            layout = LayoutInflater.from(context).inflate(R.layout.expense_details_dialog, null);
            descriptionField = (EditText) layout.findViewById(R.id.description);
            priceField = (EditText) layout.findViewById(R.id.price);
            currencyField = (TextView) layout.findViewById(R.id.currency);
            commentField = (EditText) layout.findViewById(R.id.comment);
            datePicker = (DatePicker) layout.findViewById(R.id.date_picker);

            title(context.getString(R.string.expense_details));

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
                onNegative((dialog, which) -> onDelete((EditExpenseDialog) dialog));
            }
            autoDismiss(false);

            Calendar now = Calendar.getInstance();

            if (expenseItem != null) {
                now.setTime(expenseItem.date);
                descriptionField.setText(expenseItem.description);
                commentField.setText(expenseItem.comment);
                String[] currencyPrice = StringUtils.splitCurrency(expenseItem.price);
                currencyField.setText(currencyPrice[0] != null ? currencyPrice[0] : currencyPrice[2]);
                priceField.setText(currencyPrice[1]);
            } else {
                currencyField.setText(LOCAL_CURRENCY.getCurrency().getSymbol());
            }
            datePicker.init(now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH), null);
        }

        public MaterialDialog build() {
            return new EditExpenseDialog(this);
        }

        private void onDelete(@NonNull EditExpenseDialog dialog) {
            dialog.doDelete(expenseItem);
        }

        private void onSubmitDialog(@NonNull MaterialDialog dialog) {
            String description = descriptionField.getText().toString();
            String currency = currencyField.getText().toString();
            String price = priceField.getText().toString();
            String comment = commentField.getText().toString();
            int year = datePicker.getYear();
            int month = datePicker.getMonth();
            int dayOfMonth = datePicker.getDayOfMonth();

            ((EditExpenseDialog) dialog).onSubmitDialog(description, currency, price, comment, year, month, dayOfMonth);
        }
    }

    private void onSubmitDialog(String description, String currency, String price, String comment, int year, int month, int dayOfMonth) {
        String buyer = userPrefs.getSelectedTenant();

        if (TextUtils.isEmpty(description)) {
            progressScreen.toast(R.string.error_description);
            return;
        }

        if (TextUtils.isEmpty(price)) {
            progressScreen.toast(R.string.error_price);
            return;
        }
        comment = TextUtils.isEmpty(comment) ? null : comment;
        Calendar selectedDate = Calendar.getInstance();
        if (expenseItem != null) {
            selectedDate.setTime(expenseItem.date);
        }
        selectedDate.set(Calendar.YEAR, year);
        selectedDate.set(Calendar.MONTH, month);
        selectedDate.set(Calendar.DATE, dayOfMonth);

        if (expenseItem == null) {
            ExpenseItem entry = new ExpenseItem(buyer, description, currency + price, selectedDate.getTime(), comment);
            doCreate(entry);
        } else {
            String[] currencyPrice = StringUtils.splitCurrency(expenseItem.price);
            currencyPrice[0] = currency;
            currencyPrice[1] = price;
            ExpenseItem entry = new ExpenseItem(expenseItem.index, expenseItem.buyer, description, StringUtils.concat(currencyPrice), selectedDate.getTime(), comment);

            if (!entry.equals(expenseItem)) {
                doUpdate(entry);
            } else {
                progressScreen.toast(R.string.nothing_changed);
            }
        }
    }

    private void doDelete(ExpenseItem expenseItem) {
        execute(
                prepare(service.delete(expenseItem))
                        .doOnTerminate(() -> progressScreen.toast(R.string.deleted, 1))
        );
    }

    private void doCreate(ExpenseItem expenseItem) {
        execute(
                prepare(service.insert(expenseItem))
                        .doOnTerminate(() -> progressScreen.toast(R.string.inserted, 1))
        );
    }

    private void doUpdate(ExpenseItem expenseItem) {
        execute(
                prepare(service.update(expenseItem))
                        .doOnTerminate(() -> progressScreen.toast(R.string.updated, 1))
        );
    }

    @Override
    <O> Observable<O> prepare(Observable<O> observable) {
        return super.prepare(observable).doOnTerminate(this::dismiss);
    }

    private <T> void execute(Observable<T> o) {
        o.subscribe(Actions.empty(), this::log);
    }
}
