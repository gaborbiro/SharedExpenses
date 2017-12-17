package com.gaborbiro.sharedexpenses.api;

import com.gaborbiro.sharedexpenses.Constants;
import com.gaborbiro.sharedexpenses.SpreadsheetException;
import com.gaborbiro.sharedexpenses.model.ExpenseItem;
import com.gaborbiro.sharedexpenses.model.StatItem;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.services.sheets.v4.model.ClearValuesRequest;
import com.google.api.services.sheets.v4.model.ValueRange;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public class ExpenseApiImpl extends ApiBase implements ExpenseApi {

    public ExpenseApiImpl(GoogleAccountCredential credential) {
        super(credential);
    }

    public ExpenseItem[] fetchExpenses() throws IOException, SpreadsheetException {
        List<ExpenseItem> results = new ArrayList<>();
        ValueRange response = getSheetsApi().spreadsheets().values()
                .get(Constants.EXPENSE_SPREADSHEET_ID, Constants.EXPENSES_TABLE_RANGE)
                .execute();
        List<List<Object>> values = response.getValues();
        if (values != null && values.size() > 1) {
            SheetRowReader<ExpenseItem> reader = ExpenseItem.getReader(values.get(0));

            for (int i = 1; i < values.size(); i++) {
                List<Object> row = values.get(i);
                if (row.size() > 0) {
                    try {
                        results.add(reader.get(i + 1, row));
                    } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return results.toArray(new ExpenseItem[results.size()]);
    }

    public String[] getTenantNames() throws IOException {
        List<List<Object>> values = getSheetsApi().spreadsheets().values().get(Constants.EXPENSE_SPREADSHEET_ID,
                Constants.TENANTS_TABLE_RANGE).execute().getValues();
        String[] result = new String[values.size()];

        for (int i = 0; i < values.size(); i++) {
            result[i] = values.get(i).get(0).toString();
        }
        return result;
    }

    @Override
    public StatItem[] fetchStats() throws IOException, SpreadsheetException {
        List<StatItem> results = new ArrayList<>();
        ValueRange response = getSheetsApi().spreadsheets().values()
                .get(Constants.EXPENSE_SPREADSHEET_ID, Constants.STATS_TABLE_RANGE)
                .execute();
        List<List<Object>> values = response.getValues();
        if (values != null && values.size() > 1) {
            SheetRowReader<StatItem> reader = StatItem.getReader(values.get(0));

            for (int i = 1; i < values.size(); i++) {
                List<Object> row = values.get(i);
                if (row.size() > 0) {
                    try {
                        results.add(reader.get(i + 1, row));
                    } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return results.toArray(new StatItem[results.size()]);
    }

    public void insertExpense(ExpenseItem expense) throws IOException {
        ValueRange valueRange = new ValueRange();
        List<List<Object>> row = new ArrayList<>(1);
        List<Object> cells = new ArrayList<>(5);
        cells.add(expense.buyer);
        cells.add(expense.description);
        cells.add(expense.price);
        cells.add(expense.dateString);
        cells.add(expense.comment);
        row.add(cells);
        valueRange.setValues(row);
        valueRange.setRange(Constants.EXPENSES_TABLE_RANGE);
        getSheetsApi().spreadsheets().values().
                append(Constants.EXPENSE_SPREADSHEET_ID, Constants.EXPENSES_TABLE_RANGE, valueRange).
                setValueInputOption("USER_ENTERED").execute();
    }

    @Override
    public void updateExpense(ExpenseItem expense, ExpenseItem original) throws Exception {
        ValueRange valueRange = new ValueRange();
        List<List<Object>> row = new ArrayList<>(1);
        List<Object> cells = new ArrayList<>(5);
        cells.add(expense.buyer);
        cells.add(expense.description);
        cells.add(expense.price);
        cells.add(expense.dateString);
        cells.add(expense.comment);
        row.add(cells);
        valueRange.setValues(row);
        valueRange.setRange(String.format(Constants.EXPENSES_ROW_RANGE, expense.index, expense.index));

        ExpenseItem[] currentContent = fetchExpenses();

        if (currentContent.length <= original.index - 2 || !currentContent[original.index - 2].equals(original)) {
            throw new Exception("Expense item has changed in the meanwhile");
        }

        getSheetsApi().spreadsheets().values().
                update(Constants.EXPENSE_SPREADSHEET_ID, String.format(Constants.EXPENSES_ROW_RANGE, expense.index, expense.index), valueRange).
                setValueInputOption("USER_ENTERED").execute();
    }

    @Override
    public void deleteExpense(ExpenseItem expense) throws Exception {
        ExpenseItem[] currentContent = fetchExpenses();

        if (currentContent.length <= expense.index - 2 || !currentContent[expense.index - 2].equals(expense)) {
            throw new Exception("Expense item has changed in the meanwhile");
        }

        ClearValuesRequest request = new ClearValuesRequest();
        getSheetsApi().spreadsheets().values().clear(Constants.EXPENSE_SPREADSHEET_ID, String.format(Constants.EXPENSES_ROW_RANGE, expense.index, expense.index), request).execute();
    }
}
