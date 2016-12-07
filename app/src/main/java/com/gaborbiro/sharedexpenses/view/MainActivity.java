package com.gaborbiro.sharedexpenses.view;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.gaborbiro.sharedexpenses.BuildConfig;
import com.gaborbiro.sharedexpenses.Constants;
import com.gaborbiro.sharedexpenses.R;
import com.gaborbiro.sharedexpenses.SpreadsheetException;
import com.gaborbiro.sharedexpenses.UserPrefs;
import com.gaborbiro.sharedexpenses.model.ExpenseItem;
import com.gaborbiro.sharedexpenses.model.Tenants;
import com.google.api.services.sheets.v4.model.AppendValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import butterknife.ButterKnife;
import butterknife.InjectView;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity implements MainView, EasyPermissions.PermissionCallbacks {

    @InjectView(R.id.table)
    WebView outputView;

    private ProgressDialog progressDialog;
    private int progressCount;

    private GoogleApiPresenter googleApiPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.gaborbiro.sharedexpenses.R.layout.activity_main);
        ButterKnife.inject(this);

        googleApiPresenter = new GoogleApiPresenter(this, this);

        outputView.getSettings().setJavaScriptEnabled(true);
        outputView.setWebViewClient(new WebViewClient());
        outputView.setWebChromeClient(new WebChromeClient());
        outputView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        outputView.setScrollbarFadingEnabled(true);

        Toolbar toolbar = (Toolbar) findViewById(com.gaborbiro.sharedexpenses.R.id.toolbar);
        setSupportActionBar(toolbar);

        updateTitle();

        FloatingActionButton fab = (FloatingActionButton) findViewById(com.gaborbiro.sharedexpenses.R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showCreateExpenseItemDialog();
            }
        });

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.please_wait));

        getDataFromApi();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_sort_by:
                String newSort;
                switch (UserPrefs.getSort(Constants.DEFAULT_SORT)) {
                    case Constants.SORT_DATE:
                        newSort = Constants.SORT_USER;
                        break;
                    case Constants.SORT_USER:
                        newSort = Constants.SORT_DATE;
                        break;
                    default:
                        newSort = Constants.DEFAULT_SORT;
                        break;
                }
                Toast.makeText(this, getString(R.string.sorting_by, newSort), Toast.LENGTH_SHORT).show();
                UserPrefs.setSort(newSort);
                getDataFromApi();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void chooseTenant() {
        new MaterialDialog.Builder(this).items(Tenants.getTenants()).itemsCallbackSingleChoice(-1, new MaterialDialog.ListCallbackSingleChoice() {
            @Override
            public boolean onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
                UserPrefs.setSelectedTenant(text.toString());
                updateTitle();
                return true;
            }
        }).title(R.string.who_are_you).show();
    }

    public void setOutput(String text) {
        outputView.loadData(text, "", "");
    }

    public void getDataFromApi() {
        if (googleApiPresenter.verifyCanDoWork()) {
            new FetchExpensesTask(this, googleApiPresenter).execute();
            new FetchTenantNamesTask(this, googleApiPresenter).execute();
        }
    }

    private void showCreateExpenseItemDialog() {
        final View layout = LayoutInflater.from(MainActivity.this).inflate(R.layout.new_expense_dialog, null);
        EditText buyerField = (EditText) layout.findViewById(R.id.buyer);
        final EditText descriptionField = (EditText) layout.findViewById(R.id.description);
        final EditText priceField = (EditText) layout.findViewById(R.id.price);
        final EditText commentField = (EditText) layout.findViewById(R.id.comment);
        final DatePicker datePicker = (DatePicker) layout.findViewById(R.id.date_picker);

        String selectedTenant = UserPrefs.getSelectedTenant();

        if (!TextUtils.isEmpty(selectedTenant)) {
            buyerField.setText(selectedTenant);
            buyerField.setEnabled(false);
        }

        Calendar now = Calendar.getInstance();
        datePicker.init(now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH), null);

        descriptionField.post(new Runnable() {
            @Override
            public void run() {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(descriptionField, InputMethodManager.SHOW_IMPLICIT);
            }
        });

        new MaterialDialog.Builder(MainActivity.this).title(R.string.ad_new_expense).customView(layout, false).
                positiveText(R.string.submit).onPositive(new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                String buyer = UserPrefs.getSelectedTenant();
                String description = descriptionField.getText().toString();

                if (TextUtils.isEmpty(description)) {
                    Toast.makeText(MainActivity.this, R.string.error_description, Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(priceField.getText().toString())) {
                    Toast.makeText(MainActivity.this, R.string.error_price, Toast.LENGTH_SHORT).show();
                    return;
                }
                String price = DecimalFormat.getCurrencyInstance(Locale.UK).format(Double.valueOf(priceField.getText().toString()));
                String comment = commentField.getText().toString();
                Calendar selectedDate = Calendar.getInstance();
                selectedDate.set(Calendar.YEAR, datePicker.getYear());
                selectedDate.set(Calendar.MONTH, datePicker.getMonth());
                selectedDate.set(Calendar.YEAR, datePicker.getYear());
                ExpenseItem entry = new ExpenseItem(buyer, description, price, selectedDate.getTime(), comment);
                new InsertSheetsTask(MainActivity.this, googleApiPresenter).execute(entry);
                dialog.dismiss();
            }
        }).negativeText(android.R.string.cancel).onNegative(new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                dialog.dismiss();
            }
        }).autoDismiss(false).show();
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
    public void rageQuit() {
        Toast.makeText(this, "This app needs that permission to function", Toast.LENGTH_SHORT).show();
        finish();
    }

    public void updateTitle() {
        String title = getString(R.string.app_name);
        String user = UserPrefs.getSelectedTenant();
        if (!TextUtils.isEmpty(user)) {
            title += " (" + UserPrefs.getSelectedTenant() + ")";
        }
        title += " " + BuildConfig.VERSION_NAME;
        getSupportActionBar().setTitle(title);
    }

    /**
     * Called when an activity launched here (specifically, AccountPicker
     * and authorization) exits, giving you the requestCode you started it with,
     * the resultCode it returned, and any additional data from it.
     *
     * @param requestCode code indicating which activity result is incoming.
     * @param resultCode  code indicating the result of the incoming
     *                    activity result.
     * @param data        Intent (containing result data) returned by incoming
     *                    activity result.
     */
    @Override
    protected void onActivityResult(
            int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        googleApiPresenter.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        googleApiPresenter.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
        googleApiPresenter.onPermissionsGranted(requestCode, perms);
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        googleApiPresenter.onPermissionsDenied(requestCode, perms);
    }


    private class InsertSheetsTask extends BaseSheetsTask<ExpenseItem, Integer> {

        public InsertSheetsTask(MainView view, GoogleApiPresenter googleApiPresenter) {
            super(view, googleApiPresenter);
        }

        @Override
        protected Integer doInBackground(ExpenseItem... params) {
            int modifiedRowCount = 0;
            for (ExpenseItem expense : params) {
                try {
                    ValueRange valueRange = new ValueRange();
                    List<List<Object>> row = new ArrayList<>(1);
                    List<Object> cells = new ArrayList<>(5);
                    cells.add(expense.buyer);
                    cells.add(expense.description);
                    cells.add(expense.price);
                    cells.add(ExpenseItem.DATE_FORMAT.format(expense.date));
                    cells.add(expense.comment);
                    row.add(cells);
                    valueRange.setValues(row);
                    valueRange.setRange(Constants.EXPENSES_TABLE_RANGE);
                    AppendValuesResponse response = this.service.spreadsheets().values().
                            append(Constants.SPREADSHEET_ID, Constants.EXPENSES_TABLE_RANGE, valueRange).setValueInputOption("USER_ENTERED").
                            execute();
                    modifiedRowCount += response.getUpdates().getUpdatedRows();
                } catch (Exception e) {
                    mLastError = e;
                    cancel(true);
                }
            }
            return modifiedRowCount;
        }

        @Override
        protected void onPostExecute(Integer response) {
            super.onPostExecute(response);
            getDataFromApi();
            Toast.makeText(MainActivity.this, getString(R.string.updated, response), Toast.LENGTH_SHORT).show();
        }
    }

    private class FetchExpensesTask extends BaseSheetsTask<Void, ExpenseItem[]> {

        public FetchExpensesTask(MainView view, GoogleApiPresenter googleApiPresenter) {
            super(view, googleApiPresenter);
        }

        @Override
        protected ExpenseItem[] doInBackground(Void... params) {
            try {
                return getDataFromApi();
            } catch (Exception e) {
                mLastError = e;
                cancel(true);
                return null;
            }
        }

        private ExpenseItem[] getDataFromApi() throws IOException {
            List<ExpenseItem> results = new ArrayList<>();
            ValueRange response = this.service.spreadsheets().values()
                    .get(Constants.SPREADSHEET_ID, Constants.EXPENSES_TABLE_RANGE)
                    .execute();
            List<List<Object>> values = response.getValues();
            if (values != null && values.size() > 1) {
                try {
                    ExpenseItem.Builder builder = new ExpenseItem.Builder(values.get(0));

                    for (int i = 1; i < values.size(); i++) {
                        List<Object> row = values.get(i);
                        if (row.size() > 3) {
                            results.add(builder.get(i - 1, row));
                        }
                    }
                } catch (SpreadsheetException e) {
                    e.printStackTrace();
                }
            }
            return results.toArray(new ExpenseItem[results.size()]);
        }

        @Override
        protected void onPostExecute(ExpenseItem[] output) {
            super.onPostExecute(output);
            if (output == null || output.length == 0) {
                setOutput("Empty");
            } else {
                setOutput(HtmlUtil.getHtmlTableFromExpense(output));
            }
        }
    }

    private class FetchTenantNamesTask extends BaseSheetsTask<Void, String[]> {

        public FetchTenantNamesTask(MainView view, GoogleApiPresenter googleApiPresenter) {
            super(view, googleApiPresenter);
        }

        @Override
        protected String[] doInBackground(Void... params) {
            String[] result = {};
            try {
                List<List<Object>> values = this.service.spreadsheets().values().get(Constants.SPREADSHEET_ID, Constants.TENANTS_TABLE_RANGE).execute().getValues();
                result = new String[values.size()];

                for (int i = 0; i < values.size(); i++) {
                    result[i] = values.get(i).get(0).toString();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return result;
        }

        @Override
        protected void onPostExecute(String[] response) {
            super.onPostExecute(response);
            Tenants.setTenants(response);

            String selectedTenant = UserPrefs.getSelectedTenant();
            if (TextUtils.isEmpty(selectedTenant) || !TextUtils.isEmpty(selectedTenant) && Arrays.binarySearch(Tenants.getTenants(), selectedTenant) < 0) {
                chooseTenant();
            }
        }
    }
}
