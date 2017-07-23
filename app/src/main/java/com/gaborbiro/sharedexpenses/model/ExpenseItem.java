package com.gaborbiro.sharedexpenses.model;

import com.gaborbiro.sharedexpenses.Constants;
import com.gaborbiro.sharedexpenses.SpreadsheetException;
import com.gaborbiro.sharedexpenses.api.SheetRowReader;

import java.util.Date;
import java.util.List;

public class ExpenseItem extends BaseItem {
    private static final String COLUMN_BUYER = "Buyer";
    private static final String COLUMN_DESCRIPTION = "Description";
    private static final String COLUMN_PRICE = "Price";
    private static final String COLUMN_DATE = "Date";
    private static final String COLUMN_COMMENT = "Comment";
    private static final String COLUMN_RECEIPT = "Receipt";

    public final String buyer;
    public final String description;
    public final String price;
    public final Date date;
    public final String dateString;
    public final String comment;
    public final String receipt;

    public ExpenseItem(String buyer, String description, String price, Date date, String comment, String receipt) {
        this(-1, buyer, description, price, date, comment, receipt);
    }

    public ExpenseItem(int index, String buyer, String description, String price, Date date, String comment, String receipt) {
        super(index);
        this.buyer = buyer;
        this.description = description;
        this.price = price;
        this.date = date;
        this.dateString = Constants.DATE_FORMAT.format(date);
        this.comment = comment;
        this.receipt = receipt;
    }

    public ExpenseItem(int index, String buyer, String description, String price, String dateString, String comment, String receipt) {
        super(index);
        this.buyer = buyer;
        this.description = description;
        this.price = price;
        Date finalDate;
        try {
            finalDate = Constants.DATE_FORMAT.parse(dateString);
        } catch (Throwable e) {
            finalDate = null;
            // we just use the dateString as is
        }
        this.date = finalDate;
        this.dateString = dateString;
        this.comment = comment;
        this.receipt = receipt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ExpenseItem that = (ExpenseItem) o;

        if (buyer != null ? !buyer.equals(that.buyer) : that.buyer != null) return false;
        if (description != null ? !description.equals(that.description) : that.description != null)
            return false;
        if (price != null ? !price.equals(that.price) : that.price != null) return false;
        if (date != null ? !date.equals(that.date) : that.date != null) return false;
        if (dateString != null ? !dateString.equals(that.dateString) : that.dateString != null)
            return false;
        if (comment != null ? !comment.equals(that.comment) : that.comment != null) return false;
        return receipt != null ? receipt.equals(that.receipt) : that.receipt == null;
    }

    @Override
    public int hashCode() {
        int result = buyer != null ? buyer.hashCode() : 0;
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (price != null ? price.hashCode() : 0);
        result = 31 * result + (date != null ? date.hashCode() : 0);
        result = 31 * result + (dateString != null ? dateString.hashCode() : 0);
        result = 31 * result + (comment != null ? comment.hashCode() : 0);
        result = 31 * result + (receipt != null ? receipt.hashCode() : 0);
        return result;
    }

    public static SheetRowReader<ExpenseItem> getReader(List<Object> header) throws SpreadsheetException {
        return new SheetRowReader<>(header, new String[]{
                COLUMN_BUYER,
                COLUMN_DESCRIPTION,
                COLUMN_PRICE,
                COLUMN_DATE,
                COLUMN_COMMENT,
                COLUMN_RECEIPT},
                ExpenseItem.class);
    }
}
