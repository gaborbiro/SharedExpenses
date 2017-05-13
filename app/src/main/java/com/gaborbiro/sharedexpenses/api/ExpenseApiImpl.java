package com.gaborbiro.sharedexpenses.api;

import android.content.IntentSender;
import android.graphics.Bitmap;

import com.gaborbiro.sharedexpenses.Constants;
import com.gaborbiro.sharedexpenses.SpreadsheetException;
import com.gaborbiro.sharedexpenses.model.ExpenseItem;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.sheets.v4.model.ClearValuesRequest;
import com.google.api.services.sheets.v4.model.ValueRange;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Provider;

import rx.Emitter;

public class ExpenseApiImpl implements ExpenseApi {
    private static final String COLUMN_BUYER = "Buyer";
    private static final String COLUMN_DESCRIPTION = "Description";
    private static final String COLUMN_PRICE = "Price";
    private static final String COLUMN_DATE = "Date";
    private static final String COLUMN_COMMENT = "Comment";
    private static final String COLUMN_RECEIPT = "Receipt";

    public static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yy");

    private com.google.api.services.sheets.v4.Sheets sheetsApi = null;
    private Provider<GoogleApiClient> googleApiClientProvider;

    public ExpenseApiImpl(GoogleAccountCredential credential, Provider<GoogleApiClient> googleApiClientProvider) {
        this.sheetsApi = getSheetsApi(credential);
        this.googleApiClientProvider = googleApiClientProvider;
    }

    private com.google.api.services.sheets.v4.Sheets getSheetsApi(GoogleAccountCredential credential) {
        HttpTransport transport = AndroidHttp.newCompatibleTransport();
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
        return new com.google.api.services.sheets.v4.Sheets.Builder(
                transport, jsonFactory, credential)
                .setApplicationName(Constants.GOOGLE_APP_NAME)
                .build();
    }

    public ExpenseItem[] fetchExpenses() throws IOException, SpreadsheetException {
        List<ExpenseItem> results = new ArrayList<>();
        ValueRange response = this.sheetsApi.spreadsheets().values()
                .get(Constants.SPREADSHEET_ID, Constants.EXPENSES_TABLE_RANGE)
                .execute();
        List<List<Object>> values = response.getValues();
        if (values != null && values.size() > 1) {
            ExpenseRowReader reader = new ExpenseRowReader(values.get(0));

            for (int i = 1; i < values.size(); i++) {
                List<Object> row = values.get(i);
                if (row.size() > 3) {
                    results.add(reader.get(i + 1, row));
                }
            }
        }
        return results.toArray(new ExpenseItem[results.size()]);
    }

    public String[] getTenantNames() throws IOException {
        List<List<Object>> values = sheetsApi.spreadsheets().values().get(Constants.SPREADSHEET_ID,
                Constants.TENANTS_TABLE_RANGE).execute().getValues();
        String[] result = new String[values.size()];

        for (int i = 0; i < values.size(); i++) {
            result[i] = values.get(i).get(0).toString();
        }
        return result;
    }

    @Override
    public void uploadFile(Bitmap bmp, Emitter<IntentSender> callback) {
        DriveApi.DriveContentsResult result = Drive.DriveApi.newDriveContents(googleApiClientProvider.get()).await();
        // If the operation was not successful, we cannot do anything and must fail.
        if (!result.getStatus().isSuccess()) {
            callback.onError(new Exception("Failed to create new contents."));
            return;
        }
        // Otherwise, we can write our data to the new contents.
        // Get an output stream for the contents.
        OutputStream outputStream = result.getDriveContents().getOutputStream();
        // Write the bitmap data from it.
        ByteArrayOutputStream bitmapStream = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, bitmapStream);
        try {
            outputStream.write(bitmapStream.toByteArray());
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        MetadataChangeSet metadataChangeSet = new MetadataChangeSet.Builder()
                .setMimeType("image/jpeg").setTitle("Android Photo.png").build();
        // Create an intent for the file chooser, and start it.
        callback.onNext(Drive.DriveApi
                .newCreateFileActivityBuilder()
                .setInitialMetadata(metadataChangeSet)
                .setInitialDriveContents(result.getDriveContents())
                .build(googleApiClientProvider.get()));
    }

    public static class ExpenseRowReader {
        private static int indexBuyer = -1;
        private static int indexDescription = -1;
        private static int indexPrice = -1;
        private static int indexDate = -1;
        private static int indexComment = -1;
        private static int indexReceipt = -1;

        ExpenseRowReader(List<Object> header) throws SpreadsheetException {
            int index = 0;
            for (Object title : header) {
                switch (title.toString()) {
                    case COLUMN_BUYER:
                        indexBuyer = index++;
                        break;
                    case COLUMN_DESCRIPTION:
                        indexDescription = index++;
                        break;
                    case COLUMN_PRICE:
                        indexPrice = index++;
                        break;
                    case COLUMN_DATE:
                        indexDate = index++;
                        break;
                    case COLUMN_COMMENT:
                        indexComment = index++;
                        break;
                    case COLUMN_RECEIPT:
                        indexReceipt = index++;
                        break;
                    default:
                        throw new SpreadsheetException("Unknown header column '" + title.toString() + "'");
                }
            }
            if (indexBuyer < 0) {
                throw new SpreadsheetException("Column '" + COLUMN_BUYER + "' is missing. It should be in the first row of the spreadsheet (page 2016-2017).");
            }
            if (indexDescription < 0) {
                throw new SpreadsheetException("Column '" + COLUMN_DESCRIPTION + "' is missing. It should be in the first row of the spreadsheet (page 2016-2017).");
            }
            if (indexPrice < 0) {
                throw new SpreadsheetException("Column '" + COLUMN_PRICE + "' is missing. It should be in the first row of the spreadsheet (page 2016-2017).");
            }
            if (indexDate < 0) {
                throw new SpreadsheetException("Column '" + COLUMN_DATE + "' is missing. It should be in the first row of the spreadsheet (page 2016-2017).");
            }
            if (indexComment < 0) {
                throw new SpreadsheetException("Column '" + COLUMN_COMMENT + "' is missing. It should be in the first row of the spreadsheet (page 2016-2017).");
            }
            if (indexReceipt < 0) {
                throw new SpreadsheetException("Column '" + COLUMN_RECEIPT + "' is missing. It should be in the first row of the spreadsheet (page 2016-2017).");
            }
        }

        public ExpenseItem get(int index, List<Object> row) {
            String buyer = row.get(indexBuyer).toString();
            String description = row.get(indexDescription).toString();
            String price = row.get(indexPrice).toString();
            Date date = null;
            String dateString = null;
            try {
                date = DATE_FORMAT.parse(row.get(indexDate).toString());
            } catch (ParseException e) {
                dateString = row.get(indexDate).toString();
            }
            String comment = null;

            if (row.size() > 4) {
                comment = row.get(indexComment).toString();
            }

            String receipt = null;
            if (row.size() > 5) {
                receipt = row.get(indexReceipt).toString();
            }
            if (date == null) {
                return new ExpenseItem(index, buyer, description, price, dateString, comment, receipt);
            } else {
                return new ExpenseItem(index, buyer, description, price, date, comment, receipt);
            }
        }

    }

    public void insertExpense(ExpenseItem expense) throws IOException {
        ValueRange valueRange = new ValueRange();
        List<List<Object>> row = new ArrayList<>(1);
        List<Object> cells = new ArrayList<>(5);
        cells.add(expense.buyer);
        cells.add(expense.description);
        cells.add(expense.price);
        cells.add(DATE_FORMAT.format(expense.date));
        cells.add(expense.comment);
        row.add(cells);
        valueRange.setValues(row);
        valueRange.setRange(Constants.EXPENSES_TABLE_RANGE);
        sheetsApi.spreadsheets().values().
                append(Constants.SPREADSHEET_ID, Constants.EXPENSES_TABLE_RANGE, valueRange).
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
        cells.add(DATE_FORMAT.format(expense.date));
        cells.add(expense.comment);
        row.add(cells);
        valueRange.setValues(row);
        valueRange.setRange(String.format(Constants.EXPENSES_ROW_RANGE, expense.index, expense.index));

        ExpenseItem[] currentContent = fetchExpenses();

        if (currentContent.length <= original.index - 2 || !currentContent[original.index - 2].equals(original)) {
            throw new Exception("Expense item has changed in the meanwhile");
        }

        sheetsApi.spreadsheets().values().
                update(Constants.SPREADSHEET_ID, String.format(Constants.EXPENSES_ROW_RANGE, expense.index, expense.index), valueRange).
                setValueInputOption("USER_ENTERED").execute();
    }

    @Override
    public void deleteExpense(ExpenseItem expense) throws Exception {
        ExpenseItem[] currentContent = fetchExpenses();

        if (currentContent.length <= expense.index - 2 || !currentContent[expense.index - 2].equals(expense)) {
            throw new Exception("Expense item has changed in the meanwhile");
        }

        ClearValuesRequest request = new ClearValuesRequest();
        sheetsApi.spreadsheets().values().clear(Constants.SPREADSHEET_ID, String.format(Constants.EXPENSES_ROW_RANGE, expense.index, expense.index), request).execute();
    }
}
