package com.gaborbiro.sharedexpenses.util;

import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.view.View;

public class BottomSheetCallbackAdapter extends BottomSheetBehavior.BottomSheetCallback {

    @Override
    public void onStateChanged(@NonNull View bottomSheet, int newState) {
        // override method if required
    }

    @Override
    public void onSlide(@NonNull View bottomSheet, float slideOffset) {
        // override method if required
    }
}
