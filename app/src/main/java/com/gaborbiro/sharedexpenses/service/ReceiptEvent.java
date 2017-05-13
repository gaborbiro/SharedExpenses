package com.gaborbiro.sharedexpenses.service;

import android.net.Uri;
import android.support.annotation.IntDef;

import com.gaborbiro.sharedexpenses.model.ExpenseItem;

import lombok.Builder;

@Builder
public class ReceiptEvent {

    public enum Type {
        DELETED,
        SELECT,
        SELECTED,
        UPDATE;
    }

    public Type type;
    public Uri receiptUri;
    public ExpenseItem expenseItem;
}
