package com.gaborbiro.sharedexpenses;

import com.gaborbiro.sharedexpenses.util.PrefsUtil;

public class UserPrefs {
    private static final String PREF_ACCOUNT_NAME = "com.gaborbiro.sharedexpenses.util.PREF_ACCOUNT_NAME";
    private static final String PREF_USER = "com.gaborbiro.sharedexpenses.util.PREF_USER";
    private static final String PREF_SORT = "com.gaborbiro.sharedexpenses.util.PREF_SORT";

    public static String getAccountName() {
        return PrefsUtil.get(PREF_ACCOUNT_NAME, (String) null);
    }

    public static void setAccountName(String name) {
        PrefsUtil.put(PREF_ACCOUNT_NAME, name);
    }

    public static String getUser() {
        return PrefsUtil.get(PREF_USER, (String) null);
    }

    public static void setUser(String user) {
        PrefsUtil.put(PREF_USER, user);
    }

    public static String getSort(String default_) {
        return PrefsUtil.get(PREF_SORT, default_);
    }

    public static void setSort(String sort) {
        PrefsUtil.put(PREF_SORT, sort);
    }
}
