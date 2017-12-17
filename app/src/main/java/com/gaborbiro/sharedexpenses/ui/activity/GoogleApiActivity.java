package com.gaborbiro.sharedexpenses.ui.activity;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.gaborbiro.sharedexpenses.ui.presenter.GoogleApiPresenter;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.Single;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public abstract class GoogleApiActivity extends ProgressActivity implements GoogleApiScreen, EasyPermissions.PermissionCallbacks {

    public static final int REQUEST_ACCOUNT_PICKER = 1000;
    public static final int REQUEST_AUTHORIZATION = 1001;
    public static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    public static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;
    public static final int REQUEST_CODE_RESOLUTION = 1004;

    @Inject GoogleAccountCredential credential;
    @Inject GoogleApiPresenter googleApiPresenter;

    private GoogleApiClient googleApiClient;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        googleApiPresenter.setGoogleApiScreen(this);
        googleApiPresenter.setProgressScreen(this);
        app.setGoogleApiScreen(this);

        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Drive.API)
                .addScope(Drive.SCOPE_APPFOLDER)
                .addScope(Drive.SCOPE_FILE)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(@Nullable Bundle bundle) {
                        toast("GoogleApiClient connected");
                    }

                    @Override
                    public void onConnectionSuspended(int i) {
                        toast("GoogleApiClient connection suspended");
                    }
                })
                .addOnConnectionFailedListener(result -> {
                    if (!result.hasResolution()) {
                        // show the localized error dialog.
                        GoogleApiAvailability.getInstance().getErrorDialog(GoogleApiActivity.this, result.getErrorCode(), 0).show();
                        return;
                    }
                    // The failure has a resolution. Resolve it.
                    // Called typically when the app is not yet authorized, and an authorization dialog is displayed to the user.
                    try {
                        result.startResolutionForResult(GoogleApiActivity.this, REQUEST_CODE_RESOLUTION);
                    } catch (IntentSender.SendIntentException e) {
                        error(e.getMessage());
                    }
                })
                .build();
        this.googleApiClient.connect();
    }

    @Override
    protected void onStart() {
        super.onStart();
        app.setGoogleApiScreen(this);
        app.setGoogleApiClient(googleApiClient);
    }

    @Override
    protected void onStop() {
        super.onStop();
        app.setGoogleApiScreen(null);
        app.setGoogleApiClient(null);
    }

    @Override
    public void requestAuthorization(Intent intent) {
        startActivityForResult(intent, REQUEST_AUTHORIZATION);
    }

    /**
     * Check that Google Play services APK is installed and up to date.
     *
     * @return true if Google Play Services is available and up to
     * date on this device; false otherwise.
     */
    @Override
    public boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        final int connectionStatusCode = apiAvailability.isGooglePlayServicesAvailable(this);
        return connectionStatusCode == ConnectionResult.SUCCESS;
    }

    /**
     * Attempt to resolve a missing, out-of-date, invalid or disabled Google
     * Play Services installation via a user dialog, if possible.
     */
    @Override
    public void acquireGooglePlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        final int connectionStatusCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
        }
    }

    @Override
    public String getSelectedAccountName() {
        return credential.getSelectedAccountName();
    }

    private void setSelectedAccountName(String selectedAccountName) {
        credential.setSelectedAccountName(selectedAccountName);
    }

    public abstract void onPermissionsGranted();

    /**
     * Display an error dialog showing that Google Play Services is missing
     * or out of date.
     *
     * @param connectionStatusCode code describing the presence (or lack of)
     *                             Google Play Services on this device.
     */
    @Override
    public void showGooglePlayServicesAvailabilityErrorDialog(final int connectionStatusCode) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        Dialog dialog = apiAvailability.getErrorDialog(
                this,
                connectionStatusCode,
                REQUEST_GOOGLE_PLAY_SERVICES);
        dialog.show();
    }

    /**
     * Attempts to set the account used with the API credentials. If an account
     * name was previously saved it will use that one; otherwise an account
     * picker dialog will be shown to the user. Note that the setting the
     * account to use with the credentials object requires the app to have the
     * GET_ACCOUNTS permission, which is requested here if it is not already
     * present. The AfterPermissionGranted annotation indicates that this
     * function will be rerun automatically whenever the GET_ACCOUNTS permission
     * is granted.
     */
    @Override
    @AfterPermissionGranted(REQUEST_PERMISSION_GET_ACCOUNTS)
    public void chooseGoogleAccount() {
        if (EasyPermissions.hasPermissions(this, Manifest.permission.GET_ACCOUNTS)) {
            String accountName = userPrefs.getAccountName();
            if (accountName != null) {
                setSelectedAccountName(accountName);
                onPermissionsGranted();
            } else {
                // Start a dialog from which the user can choose an account
                startActivityForResult(
                        credential.newChooseAccountIntent(),
                        REQUEST_ACCOUNT_PICKER);
            }
        } else {
            // Request the GET_ACCOUNTS permission via a user dialog
            EasyPermissions.requestPermissions(
                    this,
                    "This app needs to access your Google account (via Contacts).",
                    REQUEST_PERMISSION_GET_ACCOUNTS,
                    Manifest.permission.GET_ACCOUNTS);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode != Activity.RESULT_OK) {
                    error(
                            "This app requires Google Play Services. Please install " +
                                    "Google Play Services on your device and relaunch this app.");
                } else {
                    onPermissionsGranted();
                }
                break;
            case REQUEST_ACCOUNT_PICKER:
                if (resultCode == Activity.RESULT_OK && data != null &&
                        data.getExtras() != null) {
                    String accountName =
                            data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        userPrefs.setAccountName(accountName);
                        setSelectedAccountName(accountName);
                        onPermissionsGranted();
                    }
                }
                break;
            case REQUEST_AUTHORIZATION:
                if (resultCode == Activity.RESULT_OK) {
                    onPermissionsGranted();
                }
                break;
        }
    }

    @Override
    protected <O> Single<O> prepare(Single<O> observable) {
        return super.prepare(observable).doOnError(throwable -> {
            if (throwable instanceof UserRecoverableAuthIOException) {
                requestAuthorization(((UserRecoverableAuthIOException) throwable).getIntent());
            }
        });
    }

    // EasyPermissions.PermissionCallbacks implementation

    /**
     * Respond to requests for permissions at runtime for API 23 and above.
     *
     * @param requestCode  The request code passed in
     *                     requestPermissions(android.app.Activity, String, int, String[])
     * @param permissions  The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *                     which is either PERMISSION_GRANTED or PERMISSION_DENIED. Never null.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    /**
     * Callback for when a permission is granted using the EasyPermissions
     * library.
     *
     * @param requestCode The request code associated with the requested
     *                    permission
     * @param perms       The requested permission list. Never null.
     */
    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
        onPermissionsGranted();
    }

    /**
     * Callback for when a permission is denied using the EasyPermissions
     * library.
     *
     * @param requestCode The request code associated with the requested
     *                    permission
     * @param perms       The requested permission list. Never null.
     */
    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        toast("This app needs that permission to function");
        finish();
    }

    // end EasyPermissions.PermissionCallbacks implementation
}
