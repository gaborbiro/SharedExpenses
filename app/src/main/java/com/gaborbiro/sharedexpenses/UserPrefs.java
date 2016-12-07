package com.gaborbiro.sharedexpenses;

import com.gaborbiro.sharedexpenses.util.PrefsUtil;

public class UserPrefs {
    private static final String PREF_ACCOUNT_NAME = "com.gaborbiro.sharedexpenses.util.PREF_ACCOUNT_NAME";
    private static final String PREF_SELECTED_TENANT = "com.gaborbiro.sharedexpenses.util.PREF_SELECTED_TENANT";
    private static final String PREF_SORT = "com.gaborbiro.sharedexpenses.util.PREF_SORT";

    public static String getAccountName() {
        return PrefsUtil.get(PREF_ACCOUNT_NAME, (String) null);
    }

    public static void setAccountName(String name) {
        PrefsUtil.put(PREF_ACCOUNT_NAME, name);
    }

    public static String getSelectedTenant() {
        return PrefsUtil.get(PREF_SELECTED_TENANT, (String) null);
    }

    public static void setSelectedTenant(String tenant) {
        PrefsUtil.put(PREF_SELECTED_TENANT, tenant);
    }

    public static String getSort(String default_) {
        return PrefsUtil.get(PREF_SORT, default_);
    }

    public static void setSort(String sort) {
        PrefsUtil.put(PREF_SORT, sort);
    }
}
