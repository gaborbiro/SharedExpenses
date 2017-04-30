package com.gaborbiro.sharedexpenses.ui.activity;

import com.gaborbiro.sharedexpenses.model.ExpenseItem;

public interface MainScreen {

    void chooseTenant();

    void setExpenses(ExpenseItem[] expenses);

    void update();
}
