package com.gaborbiro.sharedexpenses;

import android.app.Application;

import com.gaborbiro.sharedexpenses.di.DaggerSharedExpensesComponent;
import com.gaborbiro.sharedexpenses.di.SharedExpensesComponent;
import com.gaborbiro.sharedexpenses.ui.activity.GoogleApiScreen;
import com.gaborbiro.sharedexpenses.ui.activity.WebScreen;
import com.gaborbiro.sharedexpenses.ui.activity.ProgressScreen;

public class App extends Application {

    private GoogleApiScreen googleApiScreen;
    private ProgressScreen progressScreen;
    private WebScreen webScreen;

    public static SharedExpensesComponent component;

    @Override
    public void onCreate() {
        super.onCreate();
        component = DaggerSharedExpensesComponent.builder().application(this).build();
    }

    public GoogleApiScreen getGoogleApiScreen() {
        return googleApiScreen;
    }

    public void setGoogleApiScreen(GoogleApiScreen googleApiScreen) {
        this.googleApiScreen = googleApiScreen;
    }

    public ProgressScreen getProgressScreen() {
        return progressScreen;
    }

    public void setProgressScreen(ProgressScreen progressScreen) {
        this.progressScreen = progressScreen;
    }

    public WebScreen getWebScreen() {
        return webScreen;
    }

    public void setWebScreen(WebScreen webScreen) {
        this.webScreen = webScreen;
    }
}
