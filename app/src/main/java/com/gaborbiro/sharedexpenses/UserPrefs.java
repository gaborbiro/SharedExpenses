package com.gaborbiro.sharedexpenses;

import com.gaborbiro.sharedexpenses.util.PrefsHelper;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class UserPrefs {
    private static final String PREF_ACCOUNT_NAME = "com.gaborbiro.sharedexpenses.PREF_ACCOUNT_NAME";
    private static final String PREF_SELECTED_TENANT = "com.gaborbiro.sharedexpenses.PREF_SELECTED_TENANT";
    private static final String PREF_SORT = "com.gaborbiro.sharedexpenses.PREF_SORT";
    private final PrefsHelper prefsHelper;

    @Inject
    public UserPrefs(PrefsHelper prefsHelper) {
        this.prefsHelper = prefsHelper;
    }

    public String getAccountName() {
        return prefsHelper.get(PREF_ACCOUNT_NAME, (String) null);
    }

    public void setAccountName(String name) {
        prefsHelper.put(PREF_ACCOUNT_NAME, name);
    }

    public String getSelectedTenant() {
        return prefsHelper.get(PREF_SELECTED_TENANT, (String) null);
    }

    public void setSelectedTenant(String tenant) {
        prefsHelper.put(PREF_SELECTED_TENANT, tenant);
    }

    public String getSort(String default_) {
        return prefsHelper.get(PREF_SORT, default_);
    }

    public void setSort(String sort) {
        prefsHelper.put(PREF_SORT, sort);
    }
}
