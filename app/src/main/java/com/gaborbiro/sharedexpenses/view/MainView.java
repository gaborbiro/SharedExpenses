package com.gaborbiro.sharedexpenses.view;

import android.content.Intent;

public interface MainView {

    void setOutput(String text);

    void getDataFromApi();

    void startActivityForResult(Intent intent, int requestCode);

    void showProgress();

    void hideProgress();

    void rageQuit();
}
