package com.gaborbiro.sharedexpenses.view;

import android.app.ProgressDialog;
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
import com.google.api.services.sheets.v4.model.AppendValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import butterknife.ButterKnife;
import butterknife.InjectView;

import static org.apache.commons.lang3.StringEscapeUtils.escapeHtml4;

public class MainActivity extends AppCompatActivity implements MainView {

    @InjectView(R.id.table)
    WebView outputView;

    ProgressDialog progressDialog;

    private static final String SORT_DATE = "date";
    private static final String SORT_USER = "user";

    private static final String DEFAULT_SORT = SORT_DATE;

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

        Toolbar toolbar = (Toolbar) findViewById(com.gaborbiro.sharedexpenses.R.id.toolbar);
        setSupportActionBar(toolbar);

        updateTitle();

        FloatingActionButton fab = (FloatingActionButton) findViewById(com.gaborbiro.sharedexpenses.R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showNewExpenseItemDialog();
            }
        });

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.please_wait));

        getResultsFromApi();
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
                switch (UserPrefs.getSort(DEFAULT_SORT)) {
                    case SORT_DATE:
                        newSort = SORT_USER;
                        break;
                    case SORT_USER:
                        newSort = SORT_DATE;
                        break;
                    default:
                        newSort = DEFAULT_SORT;
                        break;
                }
                Toast.makeText(this, getString(R.string.sorting_by, newSort), Toast.LENGTH_SHORT).show();
                UserPrefs.setSort(newSort);
                getResultsFromApi();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void chooseUser() {
        new MaterialDialog.Builder(this).items(Constants.USERS).itemsCallbackSingleChoice(-1, new MaterialDialog.ListCallbackSingleChoice() {
            @Override
            public boolean onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
                UserPrefs.setUser(text.toString());
                updateTitle();
                getResultsFromApi();
                return true;
            }
        }).title(R.string.who_are_you).show();
    }

    public void setOutput(String text) {
        outputView.loadData(text, "", "");
    }

    public void getResultsFromApi() {
        if (googleApiPresenter.verifyCanDoWork()) {
            new FetchExpensesTask(this, googleApiPresenter).execute();
        }
    }

    private void showNewExpenseItemDialog() {
        final View layout = LayoutInflater.from(MainActivity.this).inflate(R.layout.new_expense_dialog, null);
        EditText buyerField = (EditText) layout.findViewById(R.id.buyer);
        final EditText descriptionField = (EditText) layout.findViewById(R.id.description);
        final EditText priceField = (EditText) layout.findViewById(R.id.price);
        final EditText commentField = (EditText) layout.findViewById(R.id.comment);
        final DatePicker datePicker = (DatePicker) layout.findViewById(R.id.date_picker);

        buyerField.setText(UserPrefs.getUser());
        buyerField.setEnabled(false);

        Calendar now = Calendar.getInstance();
        datePicker.init(now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH), null);

        new MaterialDialog.Builder(MainActivity.this).title(R.string.ad_new_expense).customView(layout, false).
                positiveText(R.string.submit).onPositive(new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                String buyer = UserPrefs.getUser();
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
                new InsertExpenseTask(MainActivity.this, googleApiPresenter).execute(entry);
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
        progressDialog.show();
    }

    @Override
    public void hideProgress() {
        progressDialog.hide();
    }

    public void updateTitle() {
        String title = getString(R.string.app_name);
        String user = UserPrefs.getUser();
        if (!TextUtils.isEmpty(user)) {
            title += " (" + UserPrefs.getUser() + ")";
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

    private static String formatOutput(ExpenseItem[] output) {
        ExpenseItem[] copy = new ExpenseItem[output.length];
        System.arraycopy(output, 0, copy, 0, output.length);

        Arrays.sort(copy, new Comparator<ExpenseItem>() {
            @Override
            public int compare(ExpenseItem o1, ExpenseItem o2) {
                int result = 0;
                if (Objects.equals(UserPrefs.getSort(DEFAULT_SORT), SORT_USER)) {
                    result = o2.buyer.compareTo(o1.buyer);

                    if (result == 0) {
                        if (o2.date != null && o1.date != null) {
                            result = o2.date.compareTo(o1.date);
                        }
                    }
                } else {
                    if (o2.date != null && o1.date != null) {
                        result = o2.date.compareTo(o1.date);
                    }
                }
                if (result == 0) {
                    result = o2.index - o1.index;
                }
                return result;
            }
        });
        output = copy;

        StringBuffer result = new StringBuffer();
        result.append("<html>");
        result.append("<head>" +
                "<script>" +
                "    function prompt(text)" +
                "    {" +
                "        alert(text);" +
                "    }" +
                "    </script>" +
                "</head>");
        result.append("<table>");
        result.append("<thead>");
        result.append("<tr>");
        if (Objects.equals(UserPrefs.getSort(DEFAULT_SORT), SORT_USER)) {
            result.append("<td><u>Date</u></td><td><u>Description</u></td><td><u>Price</u></td><td><u>Comment</u></td>");
        } else {
            result.append("<td><u>Buyer</u></td><td><u>Description</u></td><td><u>Price</u></td><td><u>Comment</u></td>");
        }
        result.append("</tr>");
        result.append("</thead>");
        result.append("<tbody>");

        Date currentDate = null;
        String currentUser = null;

        for (ExpenseItem expense : output) {
            if (TextUtils.isEmpty(expense.error)) {
                if (Objects.equals(UserPrefs.getSort(DEFAULT_SORT), SORT_USER)) {
                    if (expense.buyer != null && !expense.buyer.equals(currentUser)) {
                        result.append("<tr bgcolor=\"#eeeeee\"><td colspan=\"4\" align=\"center\">");
                        result.append(expense.buyer);
                        result.append("</td></tr>");
                        currentUser = expense.buyer;
                    }
                } else {
                    if (expense.date != null && !expense.date.equals(currentDate)) {
                        result.append("<tr bgcolor=\"#eeeeee\"><td colspan=\"4\" align=\"center\">");
                        result.append(ExpenseItem.DATE_FORMAT.format(expense.date));
                        result.append("</td></tr>");
                        currentDate = expense.date;
                    }
                }
                result.append("<tr>");
                result.append("<td>");
                if (Objects.equals(UserPrefs.getSort(DEFAULT_SORT), SORT_USER)) {
                    result.append(ExpenseItem.DATE_FORMAT.format(expense.date));
                } else {
                    result.append(clear(expense.buyer));
                }
                result.append("</td>");
                result.append("<td>");
                result.append(clear(expense.description));
                result.append("</td>");
                result.append("<td>");
                result.append(clear(expense.price));
                result.append("</td>");
                result.append("<td>");
                result.append(clear(expense.comment));
                result.append("</td>");
                result.append("</tr>");
            } else {
                result.append("<tr><td colspan=\"4\">");
                result.append("<div style=\"color:blue\" onclick=\"prompt('" + clear(expense.detailedError) + "')\"><u>" + clear(expense.error) + "</u></div>");
                result.append("</td></tr>");
            }
        }

        result.append("</tbody>");
        result.append("</table>");
        result.append("</html>");
        return result.toString();
    }

    private static String clear(String str) {
        if (str == null || str.length() == 0) {
            return "";
        }
        return escapeHtml4(str);
    }

    private class InsertExpenseTask extends BaseExpenseTask<ExpenseItem, AppendValuesResponse> {

        public InsertExpenseTask(MainView view, GoogleApiPresenter googleApiPresenter) {
            super(view, googleApiPresenter);
        }

        /**
         * Background task to call Google Sheets API.
         *
         * @param params no parameters needed for this task.
         */
        @Override
        protected AppendValuesResponse doInBackground(ExpenseItem... params) {
            try {
                return saveExpense(params[0]);
            } catch (Exception e) {
                mLastError = e;
                cancel(true);
            }
            return null;
        }

        private AppendValuesResponse saveExpense(ExpenseItem expense) throws IOException {
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
            valueRange.setRange(Constants.SPREADSHEET_RANGE);
            AppendValuesResponse response = this.service.spreadsheets().values().
                    append(Constants.SPREADSHEET_ID, Constants.SPREADSHEET_RANGE, valueRange).setValueInputOption("USER_ENTERED").
                    execute();
            return response;
        }

        @Override
        protected void onPostExecute(AppendValuesResponse response) {
            super.onPostExecute(response);
            getResultsFromApi();
            Toast.makeText(MainActivity.this, getString(R.string.updated, response.getUpdates().getUpdatedRange()), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * An asynchronous task that handles the Google Sheets API call.
     * Placing the API calls in their own task ensures the UI stays responsive.
     */
    private class FetchExpensesTask extends BaseExpenseTask<Void, ExpenseItem[]> {

        public FetchExpensesTask(MainView view, GoogleApiPresenter googleApiPresenter) {
            super(view, googleApiPresenter);
        }

        /**
         * Background task to call Google Sheets API.
         *
         * @param params no parameters needed for this task.
         */
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

        /**
         * Fetch a list of names and majors of students in a sample spreadsheet:
         * https://docs.google.com/spreadsheets/d/1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs74OgvE2upms/edit
         *
         * @return List of names and majors
         * @throws IOException
         */
        private ExpenseItem[] getDataFromApi() throws IOException {
            List<ExpenseItem> results = new ArrayList<>();
            ValueRange response = this.service.spreadsheets().values()
                    .get(Constants.SPREADSHEET_ID, Constants.SPREADSHEET_RANGE)
                    .execute();
            List<List<Object>> values = response.getValues();
            if (values != null && values.size() > 1) {
                try {
                    ExpenseItem.Builder builder = new ExpenseItem.Builder(values.get(0));

                    for (int i = 1; i < values.size(); i++) {
                        try {
                            List<Object> row = values.get(i);
                            if (row.size() > 3) {
                                results.add(builder.get(i - 1, row));
                            }
                        } catch (ParseException e) {
                            results.add(builder.get(i - 1, "Parse error. Tap for details", e.getMessage()));
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
                setOutput(formatOutput(output));
            }
        }
    }
}
