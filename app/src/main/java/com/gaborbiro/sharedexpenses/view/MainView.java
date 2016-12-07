package com.gaborbiro.sharedexpenses.view;

import android.content.Intent;

public interface MainView {

    void setOutput(String text);

    void getResultsFromApi();

    void chooseUser();

    void startActivityForResult(Intent intent, int requestCode);

    void showProgress();

    void hideProgress();
}
