package com.gaborbiro.sharedexpenses.ui.screen;

import com.gaborbiro.sharedexpenses.model.ExpenseItem;

public interface MainScreen extends GoogleApiScreen {

    void chooseTenant();

    void setExpenses(ExpenseItem[] expenses);
}
