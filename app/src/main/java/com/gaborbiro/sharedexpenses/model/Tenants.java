package com.gaborbiro.sharedexpenses.model;

import com.gaborbiro.sharedexpenses.util.PrefsUtil;

public class Tenants {

    private static final String PREF_TENANTS = "com.gaborbiro.sharedexpenses.util.PREF_TENANTS";

    public static String[] getTenants() {
        return PrefsUtil.get(PREF_TENANTS, (String[]) null);
    }

    public static void setTenants(String[] tenants) {
        PrefsUtil.put(PREF_TENANTS, tenants);
    }
}
