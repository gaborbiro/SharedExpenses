package com.gaborbiro.sharedexpenses.ui.dialog;

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

import io.reactivex.Completable;
import io.reactivex.Observable;

public class EditExpenseDialog extends BaseServiceDialog {

    private static final NumberFormat LOCAL_CURRENCY = DecimalFormat.getCurrencyInstance();

    private ExpenseItem expenseItem;
    private String uploadedReceiptFile;

    private EditText descriptionField;
    private EditText priceField;
    private TextView currencyField;
    private EditText commentField;
    private DatePicker datePicker;

    public static void show(@NonNull Context context) {
        show(context, null);
    }

    public static void show(@NonNull Context context, @Nullable ExpenseItem expenseItem) {
        EditExpenseDialog dialog = new EditExpenseDialogBuilder(context, expenseItem != null).build();
        dialog.init(expenseItem);
        dialog.show();
    }

    private EditExpenseDialog(EditExpenseDialogBuilder builder) {
        super(builder);

        View layout = getCustomView();
        descriptionField = (EditText) layout.findViewById(R.id.description);
        priceField = (EditText) layout.findViewById(R.id.price);
        currencyField = (TextView) layout.findViewById(R.id.currency);
        commentField = (EditText) layout.findViewById(R.id.comment);
        datePicker = (DatePicker) layout.findViewById(R.id.date_picker);

        descriptionField.post(() -> {
            InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(descriptionField, InputMethodManager.SHOW_IMPLICIT);
        });
    }

    private void init(ExpenseItem expenseItem) {
        this.expenseItem = expenseItem;
        Calendar now = Calendar.getInstance();

        if (expenseItem != null) {
            uploadedReceiptFile = TextUtils.isEmpty(expenseItem.receipt) ? null : expenseItem.receipt;
            now.setTime(expenseItem.date);
            descriptionField.setText(expenseItem.description);
            commentField.setText(expenseItem.comment);
            String[] currencyPrice = StringUtils.splitCurrency(expenseItem.price);
            currencyField.setText(currencyPrice[0] != null ? currencyPrice[0] : currencyPrice[2]);
            priceField.setText(currencyPrice[1]);
        } else {
            uploadedReceiptFile = null;
            currencyField.setText(LOCAL_CURRENCY.getCurrency().getSymbol());
        }
        datePicker.init(now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH), null);
    }

    @Override
    protected void inject() {
        App.component.inject(this);
    }

    private void onSubmit() {
        doSubmit();
    }

    private void doSubmit() {
        String description = descriptionField.getText().toString();
        String currency = currencyField.getText().toString();
        String price = priceField.getText().toString();
        String comment = commentField.getText().toString();
        int year = datePicker.getYear();
        int month = datePicker.getMonth();
        int dayOfMonth = datePicker.getDayOfMonth();

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
            ExpenseItem entry = new ExpenseItem(buyer, description, currency + price, selectedDate.getTime(), comment, uploadedReceiptFile);
            doCreate(entry);
        } else {
            String[] currencyPrice = StringUtils.splitCurrency(expenseItem.price);
            currencyPrice[0] = currency;
            currencyPrice[1] = price;
            ExpenseItem entry = new ExpenseItem(expenseItem.index, expenseItem.buyer, description,
                    StringUtils.concat(currencyPrice), selectedDate.getTime(), comment, uploadedReceiptFile);

            if (!entry.equals(expenseItem)) {
                doUpdate(entry, expenseItem);
            } else {
                progressScreen.toast(R.string.nothing_changed);
            }
        }
    }

    private void doDelete() {
        execute(
                prepare(service.delete(expenseItem))
                        .doOnSubscribe(disposable -> dismiss())
                        .doOnComplete(() -> {
                            progressScreen.toast(R.string.deleted, 1);
                            webScreen.update();
                        })
        );
    }

    private void doCreate(ExpenseItem expenseItem) {
        execute(
                prepare(service.insert(expenseItem))
                        .doOnComplete(() -> {
                            dismiss();
                            progressScreen.toast(R.string.inserted, 1);
                            webScreen.update();
                        })
        );
    }

    private void doUpdate(ExpenseItem expenseItem, ExpenseItem original) {
        execute(prepare(service.update(expenseItem, original))
                .doOnSubscribe(disposable -> this.dismiss())
                .doOnComplete(() -> {
                    progressScreen.toast(R.string.updated, 1);
                    webScreen.update();
                })
        );
    }

    private <T> void execute(Observable<T> o) {
        o.subscribe(t -> dismiss(),
                throwable -> progressScreen.error(throwable.getMessage()));
    }

    private void execute(Completable o) {
        o.subscribe(this::dismiss,
                throwable -> progressScreen.error(throwable.getMessage()));
    }

    private static class EditExpenseDialogBuilder extends MaterialDialog.Builder {

        @SuppressLint("InflateParams")
        EditExpenseDialogBuilder(Context context, boolean deleteEnabled) {
            super(context);

            View layout = LayoutInflater.from(context).inflate(R.layout.expense_details_dialog, null);
            customView(layout, false);

            title(context.getString(R.string.expense_details));
            positiveText(R.string.submit);
            onPositive((dialog, which) -> ((EditExpenseDialog) dialog).onSubmit());
            neutralText(android.R.string.cancel);
            onNeutral((dialog, which) -> dialog.dismiss());

            if (deleteEnabled) {
                negativeText(R.string.remove);
                onNegative((dialog, which) -> ((EditExpenseDialog) dialog).doDelete());
            }
            autoDismiss(false);
        }

        public EditExpenseDialog build() {
            return new EditExpenseDialog(this);
        }
    }
}
