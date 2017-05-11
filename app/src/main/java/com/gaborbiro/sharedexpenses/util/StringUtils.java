package com.gaborbiro.sharedexpenses.util;

import android.text.TextUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils {

    public static String[] splitCurrency(String price) {
        price = price.trim();

        if (price.startsWith("+")) {
            price = price.substring(1);
        }

        boolean negative = false;
        if (price.startsWith("-")) {
            negative = true;
            price = price.substring(1);
        }

        Pattern p = Pattern.compile("[\\d]+");
        Matcher m = p.matcher(price);

        if (m.find()) {
            int startIndex = m.start();
            int endIndex = -1;

            if (startIndex == 0) {
                // postfix currency
                m = p.matcher(new StringBuffer(price).reverse().toString());
                endIndex = m.start();
            } else {
                // prefix currency
                endIndex = price.length();
            }
            return new String[]{price.substring(0, startIndex), (negative ? "-" : "") + price.substring(startIndex, endIndex), price.substring(endIndex)};
        } else {
            return new String[]{null, (negative ? "-" : "") + price, null};
        }
    }

    public static String concat(String[] texts) {
        StringBuffer buffer = new StringBuffer();

        for (String text : texts) {
            if (!TextUtils.isEmpty(text)) {
                buffer.append(text);
            }
        }
        return buffer.toString();
    }
}
