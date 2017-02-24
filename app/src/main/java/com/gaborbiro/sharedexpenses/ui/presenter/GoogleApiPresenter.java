package com.gaborbiro.sharedexpenses.ui.presenter;

import com.gaborbiro.sharedexpenses.ui.screen.GoogleApiScreen;
import com.gaborbiro.sharedexpenses.util.ConnectivityUtil;

public class GoogleApiPresenter {

    private GoogleApiScreen screen;

    public GoogleApiPresenter(GoogleApiScreen screen) {
        this.screen = screen;
    }

    /**
     * Verifying that all the preconditions are satisfied to call the API.
     * The preconditions are: Google Play Services installed, an
     * account was selected and the device currently has online access. If any
     * of the preconditions are not satisfied, the app will prompt the user as
     * appropriate.
     */
    public boolean verifyApiAccess() {
        if (!screen.isGooglePlayServicesAvailable()) {
            screen.acquireGooglePlayServices();
            return false;
        } else if (screen.getSelectedAccountName() == null) {
            screen.chooseGoogleAccount();
            return false;
        } else if (!ConnectivityUtil.isDeviceOnline()) {
            screen.error("No network connection available.");
            return false;
        } else {
            return true;
        }
    }
}
