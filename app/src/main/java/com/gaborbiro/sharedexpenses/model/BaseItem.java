package com.gaborbiro.sharedexpenses.model;

public class BaseItem {
    public final int index;

    public BaseItem(int index) {
        this.index = index;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ExpenseItem that = (ExpenseItem) o;

        return index == that.index;
    }

    @Override
    public int hashCode() {
        return index;
    }
}
