package com.gaborbiro.sharedexpenses.ui.activity;

import com.gaborbiro.sharedexpenses.model.ExpenseItem;

public interface WebScreen {

    void chooseTenant();

    void setExpenses(ExpenseItem[] expenses);

    void update();
}
