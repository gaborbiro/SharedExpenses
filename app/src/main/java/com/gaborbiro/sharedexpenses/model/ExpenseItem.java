package com.gaborbiro.sharedexpenses.model;

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

    public static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yy");

    public final int index;
    public final String buyer;
    public final String description;
    public final String price;
    public final Date date;
    public final String dateString;
    public final String comment;

    public ExpenseItem(String buyer, String description, String price, Date date, String comment) {
        this(-1, buyer, description, price, date, comment);
    }

    public ExpenseItem(int index, String buyer, String description, String price, Date date, String comment) {
        this.index = index;
        this.buyer = buyer;
        this.description = description;
        this.price = price;
        this.date = date;
        this.dateString = null;
        this.comment = comment;
    }

    public ExpenseItem(int index, String buyer, String description, String price, String date, String comment) {
        this.index = index;
        this.buyer = buyer;
        this.description = description;
        this.price = price;
        this.date = null;
        this.dateString = date;
        this.comment = comment;
    }

    @Override
    public String toString() {
        return buyer + " " + description + " " + price + " " + (date != null ? DATE_FORMAT.format(date) : dateString) + " " + comment;
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
            if (date == null) {
                return new ExpenseItem(index, buyer, description, price, dateString, comment);
            } else {
                return new ExpenseItem(index, buyer, description, price, date, comment);
            }
        }
    }
}
