package com.gaborbiro.sharedexpenses.ui;

import android.content.Context;
import android.content.res.Resources;
import android.text.TextUtils;

import com.gaborbiro.sharedexpenses.Constants;
import com.gaborbiro.sharedexpenses.R;
import com.gaborbiro.sharedexpenses.UserPrefs;
import com.gaborbiro.sharedexpenses.model.ExpenseItem;
import com.gaborbiro.sharedexpenses.util.AssetUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

import javax.inject.Inject;
import javax.inject.Singleton;

import static org.apache.commons.lang3.StringEscapeUtils.escapeHtml4;

@Singleton
public class HtmlHelper {

    @Inject Context context;
    @Inject UserPrefs userPrefs;

    @Inject
    public HtmlHelper() {
    }

    public String getHtmlTableFromExpense(ExpenseItem[] expenses) throws IOException {
        final String sort = userPrefs.getSort();
        ExpenseItem[] copy = new ExpenseItem[expenses.length];
        System.arraycopy(expenses, 0, copy, 0, expenses.length);
        expenses = copy;
        Arrays.sort(expenses, (o2, o1) -> {
            int result = 0;
            if (Objects.equals(sort, UserPrefs.SORT_USER)) {
                result = o1.buyer.compareTo(o2.buyer);
            }
            if (result == 0) {
                if (o1.date != null) {
                    if (o2.date != null) {
                        result = o1.date.compareTo(o2.date);
                    } else {
                        result = 1;
                    }
                } else {
                    if (o2.date != null) {
                        result = -1;
                    } else {
                        return 0;
                    }
                }
            }
            if (result == 0) {
                result = o1.index - o2.index;
            }
            return result;
        });
        String table = AssetUtils.readAssetAsText(context, "table.html");
        String row = AssetUtils.readAssetAsText(context, "row.html");
        String sectionHeader = AssetUtils.readAssetAsText(context, "section_header.html");
        String speechBubble = AssetUtils.readAssetAsText(context, "speech_bubble.html");
        StringBuilder result = new StringBuilder();
        String currentDate = null;
        String currentUser = null;

        for (ExpenseItem expense : expenses) {
            if (Objects.equals(sort, UserPrefs.SORT_USER)) {
                if (expense.buyer != null && !expense.buyer.equals(currentUser)) {
                    result.append(String.format(sectionHeader, expense.buyer));
                    currentUser = expense.buyer;
                }
            } else {
                String date = TextUtils.isEmpty(expense.dateString) ? "Missing date" : expense.dateString;
                if (!date.equals(currentDate)) {
                    result.append(String.format(sectionHeader, date));
                    currentDate = date;
                }
            }
            String dateOrBuyer;
            if (Objects.equals(sort, UserPrefs.SORT_USER)) {
                if (expense.date != null) {
                    dateOrBuyer = Constants.DATE_FORMAT.format(expense.date);
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
            }
        }
        Resources r = context.getResources();
        int bottomButtonHeightPx = (int) (r.getDimensionPixelSize(R.dimen.bottom_button_height) / r.getDisplayMetrics().density);
        return String.format(table, bottomButtonHeightPx, result.toString());
    }

    private static String clearHtml(String str) {
        if (str == null || str.length() == 0) {
            return "";
        }
        return escapeHtml4(str);
    }
}
