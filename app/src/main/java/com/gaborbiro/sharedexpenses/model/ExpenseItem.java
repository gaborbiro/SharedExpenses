package com.gaborbiro.sharedexpenses.model;

import java.util.Date;

import static com.gaborbiro.sharedexpenses.api.ExpenseApiImpl.DATE_FORMAT;

public class ExpenseItem {
    public final int index;
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
        this.index = index;
        this.buyer = buyer;
        this.description = description;
        this.price = price;
        this.date = date;
        this.dateString = null;
        this.comment = comment;
        this.receipt = receipt;
    }

    public ExpenseItem(int index, String buyer, String description, String price, String date, String comment, String receipt) {
        this.index = index;
        this.buyer = buyer;
        this.description = description;
        this.price = price;
        this.date = null;
        this.dateString = date;
        this.comment = comment;
        this.receipt = receipt;
    }

    @Override
    public String toString() {
        return buyer + " " + description + " " + price + " " + (date != null ? DATE_FORMAT.format(date) : dateString) + " " + comment;
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
}
