package com.gaborbiro.sharedexpenses.model;

import android.text.TextUtils;

import com.gaborbiro.sharedexpenses.SpreadsheetException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class ExpenseItem {
    private static final String COLUMN_BUYER = "Buyer";
    private static final String COLUMN_DESCRIPTION = "Description";
    private static final String COLUMN_PRICE = "Price";
    private static final String COLUMN_DATE = "Date";
    private static final String COLUMN_COMMENT = "Comment";

    public static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy");

    public final int index;
    public final String buyer;
    public final String description;
    public final String price;
    public final Date date;
    public final String comment;
    public String error;
    public String detailedError;

    private ExpenseItem(int index, String error, String detailedError) {
        this(index, null, null, null, null, null, error, detailedError);
    }

    public ExpenseItem(String buyer, String description, String price, Date date, String comment) {
        this(-1, buyer, description, price, date, comment, null, null);
    }

    public ExpenseItem(int index, String buyer, String description, String price, Date date, String comment, String error, String detailedError) {
        this.index = index;
        this.buyer = buyer;
        this.description = description;
        this.price = price;
        this.date = date;
        this.comment = comment;
        this.error = error;
        this.detailedError = detailedError;
    }

    @Override
    public String toString() {
        if (TextUtils.isEmpty(error)) {
            return buyer + " " + description + " " + price + " " + DATE_FORMAT.format(date) + " " + comment;
        } else {
            return error;
        }
    }

    public static class Builder {
        private static int indexBuyer = -1;
        private static int indexDescription = -1;
        private static int indexPrice = -1;
        private static int indexDate = -1;
        private static int indexComment = -1;

        public Builder(List<Object> header) throws SpreadsheetException {
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
                    default:
                        throw new SpreadsheetException("Unknown header column '" + title.toString() + "'");
                }
            }
            if (indexBuyer < 0) {
                throw new SpreadsheetException("Column 'Buyer' is missing. It should be in the first row of the spreadsheet (page 2016-2017).");
            }
            if (indexDescription < 0) {
                throw new SpreadsheetException("Column 'Description' is missing. It should be in the first row of the spreadsheet (page 2016-2017).");
            }
            if (indexPrice < 0) {
                throw new SpreadsheetException("Column 'Price' is missing. It should be in the first row of the spreadsheet (page 2016-2017).");
            }
            if (indexDate < 0) {
                throw new SpreadsheetException("Column 'Date' is missing. It should be in the first row of the spreadsheet (page 2016-2017).");
            }
            if (indexComment < 0) {
                throw new SpreadsheetException("Column 'Comment' is missing. It should be in the first row of the spreadsheet (page 2016-2017).");
            }
        }

        public ExpenseItem get(int index, List<Object> row) throws ParseException {
            String buyer = row.get(indexBuyer).toString();
            String description = row.get(indexDescription).toString();
            String price = row.get(indexPrice).toString();
            Date date = DATE_FORMAT.parse(row.get(indexDate).toString());
            String comment = null;

            if (row.size() > 4) {
                comment = row.get(indexComment).toString();
            }
            return new ExpenseItem(index, buyer, description, price, date, comment, null, null);
        }

        public ExpenseItem get(int index, String error, String detailedError) {
            return new ExpenseItem(index, error, detailedError);
        }
    }
}
