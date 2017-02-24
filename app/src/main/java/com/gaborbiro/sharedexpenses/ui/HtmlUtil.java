package com.gaborbiro.sharedexpenses.ui;

import android.text.TextUtils;

import com.gaborbiro.sharedexpenses.Constants;
import com.gaborbiro.sharedexpenses.UserPrefs;
import com.gaborbiro.sharedexpenses.model.ExpenseItem;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;

import static org.apache.commons.lang3.StringEscapeUtils.escapeHtml4;

public class HtmlUtil {

    public static String getHtmlTableFromExpense(ExpenseItem[] expenses) {
        ExpenseItem[] copy = new ExpenseItem[expenses.length];
        System.arraycopy(expenses, 0, copy, 0, expenses.length);

        Arrays.sort(copy, new Comparator<ExpenseItem>() {
            @Override
            public int compare(ExpenseItem o1, ExpenseItem o2) {
                int result = 0;
                if (Objects.equals(UserPrefs.getSort(Constants.DEFAULT_SORT), Constants.SORT_USER)) {
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

        StringBuffer result = new StringBuffer();
        result.append("<html>");
        result.append("<head>" +
                "<script>" +
                "    function prompt(text)" +
                "    {" +
                "        alert(text);" +
                "    }" +
                "    </script>" +
                "</head>");
        result.append("<table>");
        result.append("<thead>");
        result.append("<tr>");
        if (Objects.equals(UserPrefs.getSort(Constants.DEFAULT_SORT), Constants.SORT_USER)) {
            result.append("<td><u>Date</u></td><td><u>Description</u></td><td><u>Price</u></td><td><u>Comment</u></td>");
        } else {
            result.append("<td><u>Buyer</u></td><td><u>Description</u></td><td><u>Price</u></td><td><u>Comment</u></td>");
        }
        result.append("</tr>");
        result.append("</thead>");
        result.append("<tbody>");

        String currentDate = null;
        String currentUser = null;

        for (ExpenseItem expense : expenses) {
            if (Objects.equals(UserPrefs.getSort(Constants.DEFAULT_SORT), Constants.SORT_USER)) {
                if (expense.buyer != null && !expense.buyer.equals(currentUser)) {
                    result.append("<tr bgcolor=\"#eeeeee\"><td colspan=\"4\" align=\"center\"><font color=\"#666\">");
                    result.append(expense.buyer);
                    result.append("</font></td></tr>");
                    currentUser = expense.buyer;
                }
            } else {
                if (expense.date != null) {
                    String formattedDate = ExpenseItem.DATE_FORMAT.format(expense.date);
                    if (!formattedDate.equals(currentDate)) {
                        result.append("<tr bgcolor=\"#eeeeee\"><td colspan=\"4\" align=\"center\"><font color=\"#666\">");
                        result.append(formattedDate);
                        result.append("</font></td></tr>");
                        currentDate = formattedDate;
                    }
                } else {
                    if (!expense.dateString.equals(currentDate)) {
                        result.append("<tr bgcolor=\"#eeeeee\"><td colspan=\"4\" align=\"center\"><font color=\"#666\">");
                        result.append(expense.dateString);
                        result.append("</font></td></tr>");
                        currentDate = expense.dateString;
                    }
                }
            }
            result.append("<tr>");
            result.append("<td>");
            if (Objects.equals(UserPrefs.getSort(Constants.DEFAULT_SORT), Constants.SORT_USER)) {
                if (expense.date != null) {
                    result.append(ExpenseItem.DATE_FORMAT.format(expense.date));
                } else {
                    result.append(expense.dateString);
                }
            } else {
                result.append(clearHtml(expense.buyer));
            }
            result.append("</td>");
            result.append("<td>");
            result.append(clearHtml(expense.description));
            result.append("</td>");
            result.append("<td>");
            result.append(clearHtml(expense.price));
            result.append("</td>");
            result.append("<td>");
            result.append(clearHtml(expense.comment));
            result.append("</td>");
            result.append("</tr>");
        }
        result.append("</tbody>");
        result.append("</table>");
        result.append("</html>");
        return result.toString();
    }

    private static String clearHtml(String str) {
        if (str == null || str.length() == 0) {
            return "";
        }
        return escapeHtml4(str);
    }
}
