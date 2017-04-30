package com.gaborbiro.sharedexpenses.ui;

import android.content.Context;
import android.text.TextUtils;

import com.gaborbiro.sharedexpenses.UserPrefs;
import com.gaborbiro.sharedexpenses.model.ExpenseItem;
import com.gaborbiro.sharedexpenses.service.ExpenseApiImpl;
import com.gaborbiro.sharedexpenses.util.AssetUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;

import javax.inject.Inject;
import javax.inject.Singleton;

import static org.apache.commons.lang3.StringEscapeUtils.escapeHtml4;

@Singleton
public class HtmlHelper {

    @Inject Context context;
    @Inject UserPrefs userPrefs;

    @Inject
    public HtmlHelper() {}

    public String getHtmlTableFromExpense(ExpenseItem[] expenses) throws IOException {
        final String sort = userPrefs.getSort();
        ExpenseItem[] copy = new ExpenseItem[expenses.length];
        System.arraycopy(expenses, 0, copy, 0, expenses.length);

        Arrays.sort(copy, new Comparator<ExpenseItem>() {
            @Override
            public int compare(ExpenseItem o1, ExpenseItem o2) {
                int result = 0;
                if (Objects.equals(sort, UserPrefs.SORT_USER)) {
                    result = o2.buyer.compareTo(o1.buyer);

                    if (result == 0) {
                        if (o2.date != null) {
                            if (o1.date != null) {
                                result = o2.date.compareTo(o1.date);
                            } else {
                                result = -1;
                            }
                        } else {
                            if (o1.date != null) {
                                result = 1;
                            } else {
                                if (!TextUtils.isEmpty(o2.dateString) && !TextUtils.isEmpty(o1.dateString)) {
                                    result = 0;
                                }
                            }
                        }
                    }
                } else {
                    if (o2.date != null && o1.date != null) {
                        result = o2.date.compareTo(o1.date);
                    }
                }
                if (result == 0) {
                    result = o2.index - o1.index;
                }
                return result;
            }
        });
        expenses = copy;
        String table = AssetUtils.readAssetAsText(context, "table.html");
        String row = AssetUtils.readAssetAsText(context, "row.html");
        String sectionHeader = AssetUtils.readAssetAsText(context, "section_header.html");
        String speechBubble = AssetUtils.readAssetAsText(context, "speech_bubble.html");
        StringBuffer result = new StringBuffer();
        String currentDate = null;
        String currentUser = null;

        for (ExpenseItem expense : expenses) {
            if (Objects.equals(sort, UserPrefs.SORT_USER)) {
                if (expense.buyer != null && !expense.buyer.equals(currentUser)) {
                    result.append(String.format(sectionHeader, expense.buyer));
                    currentUser = expense.buyer;
                }
            } else {
                if (expense.date != null) {
                    String formattedDate = ExpenseApiImpl.DATE_FORMAT.format(expense.date);
                    if (!formattedDate.equals(currentDate)) {
                        result.append(String.format(sectionHeader, formattedDate));
                        currentDate = formattedDate;
                    }
                } else {
                    if (!expense.dateString.equals(currentDate)) {
                        result.append(String.format(sectionHeader, expense.dateString));
                        currentDate = expense.dateString;
                    }
                }
            }
            String dateOrBuyer;
            if (Objects.equals(sort, UserPrefs.SORT_USER)) {
                if (expense.date != null) {
                    dateOrBuyer = ExpenseApiImpl.DATE_FORMAT.format(expense.date);
                } else {
                    dateOrBuyer = expense.dateString;
                }
            } else {
                dateOrBuyer = clearHtml(expense.buyer);
            }
            if (TextUtils.isEmpty(expense.comment)) {
                result.append(String.format(row, expense.index, dateOrBuyer, clearHtml(expense.description), clearHtml(expense.price), ""));
            } else {
                result.append(String.format(row, expense.index, dateOrBuyer, clearHtml(expense.description), clearHtml(expense.price), speechBubble));
                //clearHtml(expense.comment)
            }
        }
        return String.format(table, result.toString());
    }

    private static String clearHtml(String str) {
        if (str == null || str.length() == 0) {
            return "";
        }
        return escapeHtml4(str);
    }
}
