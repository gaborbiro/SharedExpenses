package com.gaborbiro.sharedexpenses.model;

import com.gaborbiro.sharedexpenses.SpreadsheetException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static com.gaborbiro.sharedexpenses.service.ExpenseApi.DATE_FORMAT;

public class ExpenseItem {
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
}
