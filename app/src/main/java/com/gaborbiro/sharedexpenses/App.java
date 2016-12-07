package com.gaborbiro.sharedexpenses;

import android.app.Application;
import android.content.Context;

public class App extends Application {

    private static Context appContext;

    public App() {
        this.appContext = this;
    }

    public static Context getAppContext() {
        return appContext;
    }
}
