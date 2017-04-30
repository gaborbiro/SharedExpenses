package com.gaborbiro.sharedexpenses.ui.presenter;

import com.gaborbiro.sharedexpenses.ui.activity.GoogleApiScreen;
import com.gaborbiro.sharedexpenses.ui.activity.ProgressScreen;
import com.gaborbiro.sharedexpenses.util.ConnectivityUtil;

public class GoogleApiPresenter {

    private GoogleApiScreen googleApiScreen;
    private ProgressScreen progressScreen;

    public GoogleApiPresenter(GoogleApiScreen googleApiScreen, ProgressScreen progressScreen) {
        this.googleApiScreen = googleApiScreen;
        this.progressScreen = progressScreen;
    }

    /**
     * Verifying that all the preconditions are satisfied to call the API.
     * The preconditions are: Google Play Services installed, an
     * account was selected and the device currently has online access. If any
     * of the preconditions are not satisfied, the app will prompt the user as
     * appropriate.
     */
    public boolean verifyApiAccess() {
        if (!googleApiScreen.isGooglePlayServicesAvailable()) {
            googleApiScreen.acquireGooglePlayServices();
            return false;
        } else if (googleApiScreen.getSelectedAccountName() == null) {
            googleApiScreen.chooseGoogleAccount();
            return false;
        } else if (!ConnectivityUtil.isDeviceOnline()) {
            progressScreen.error("No network connection available.");
            return false;
        } else {
            return true;
        }
    }
}
