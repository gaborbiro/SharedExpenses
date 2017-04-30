package com.gaborbiro.sharedexpenses;

import com.gaborbiro.sharedexpenses.util.PrefsHelper;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class AppPrefs {

    private static final String PREF_TENANTS = "com.gaborbiro.sharedexpenses.PREF_TENANTS";
    private final PrefsHelper prefsHelper;

    @Inject
    public AppPrefs(PrefsHelper prefsHelper) {
        this.prefsHelper = prefsHelper;
    }

    public String[] getTenants() {
        return prefsHelper.get(PREF_TENANTS, (String[]) null);
    }

    public void setTenants(String[] tenants) {
        prefsHelper.put(PREF_TENANTS, tenants);
    }
}
