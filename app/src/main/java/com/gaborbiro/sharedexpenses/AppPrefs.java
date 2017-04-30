package com.gaborbiro.sharedexpenses;

import com.gaborbiro.sharedexpenses.util.PrefsUtil;

public class AppPrefs {

    private static final String PREF_TENANTS = "com.gaborbiro.sharedexpenses.PREF_TENANTS";

    public static String[] getTenants() {
        return PrefsUtil.get(PREF_TENANTS, (String[]) null);
    }

    public static void setTenants(String[] tenants) {
        PrefsUtil.put(PREF_TENANTS, tenants);
    }
}
