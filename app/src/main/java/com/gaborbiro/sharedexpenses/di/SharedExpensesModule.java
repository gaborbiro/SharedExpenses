package com.gaborbiro.sharedexpenses.di;

import android.app.Application;
import android.content.Context;

import com.gaborbiro.sharedexpenses.App;
import com.gaborbiro.sharedexpenses.api.ExpenseApi;
import com.gaborbiro.sharedexpenses.api.ExpenseApiImpl;
import com.gaborbiro.sharedexpenses.ui.activity.GoogleApiScreen;
import com.gaborbiro.sharedexpenses.ui.activity.ProgressScreen;
import com.gaborbiro.sharedexpenses.ui.activity.WebScreen;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.sheets.v4.SheetsScopes;

import java.util.Arrays;

import javax.inject.Provider;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class SharedExpensesModule {

    private static final String[] SCOPES = {SheetsScopes.SPREADSHEETS};

    @Provides
    Context provideContext(App application) {
        return application.getApplicationContext();
    }

    @Provides
    App provideApp(Application application) {
        return (App) application;
    }

    @Provides
    GoogleApiScreen provideGoogleApiScreen(App app) {
        return app.getGoogleApiScreen();
    }

    @Provides
    ProgressScreen provideProgressScreen(App app) {
        return app.getProgressScreen();
    }

    @Provides
    public WebScreen provideWebScreen(App app) {
        return app.getWebScreen();
    }

    @Provides
    @Singleton
    GoogleAccountCredential provideGoogleAccountCredential(Context context) {
        return GoogleAccountCredential.usingOAuth2(context, Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff());
    }

    @Provides
    @Singleton
    ExpenseApi provideExpenseApi(GoogleAccountCredential credential, Provider<GoogleApiClient> googleApiClientProvider) {
        return new ExpenseApiImpl(credential, googleApiClientProvider);
    }

    @Provides
    GoogleApiClient provideGoogleApiClient(App app) {
        return app.getGoogleApiClient();
    }
}
