package com.gaborbiro.sharedexpenses;

import com.gaborbiro.sharedexpenses.util.PrefsHelper;

import javax.inject.Inject;
import javax.inject.Singleton;

import static com.gaborbiro.sharedexpenses.UserPrefs.Strings.ACCOUNT_NAME;
import static com.gaborbiro.sharedexpenses.UserPrefs.Strings.SELECTED_TENANT;
import static com.gaborbiro.sharedexpenses.UserPrefs.Strings.SORT;

@Singleton
public class UserPrefs {

    private final PrefsHelper prefsHelper;

    public static final String SORT_DATE = "date";
    public static final String SORT_USER = "user";

    @Inject
    public UserPrefs(PrefsHelper prefsHelper) {
        this.prefsHelper = prefsHelper;
    }

    public String getAccountName() {
        return get(ACCOUNT_NAME);
    }

    public void setAccountName(String name) {
        put(ACCOUNT_NAME, name);
    }

    public String getSelectedTenant() {
        return get(SELECTED_TENANT);
    }

    public void setSelectedTenant(String tenant) {
        put(SELECTED_TENANT, tenant);
    }

    public String getSort() {
        return get(SORT);
    }

    public void setSort(String sort) {
        put(SORT, sort);
    }

    public void toggleSort() {
        String sort = getSort();
        switch (sort) {
            case SORT_DATE:
                setSort(UserPrefs.SORT_USER);
                break;
            case UserPrefs.SORT_USER:
                setSort(UserPrefs.SORT_DATE);
                break;
            default:
                break;
        }
    }

    private String get(Strings key) {
        return prefsHelper.get(key.name(), key.defaultValue);
    }

    private void put(Strings key, String value) {
        prefsHelper.put(key.name(), value);
    }

    enum Strings {
        ACCOUNT_NAME(null),
        SELECTED_TENANT(null),
        SORT(SORT_DATE);

        private String defaultValue;

        Strings(String defaultValue) {
            this.defaultValue = defaultValue;
        }
    }
}
