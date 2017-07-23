package com.gaborbiro.sharedexpenses.ui.dialog;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.widget.ImageView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.gaborbiro.sharedexpenses.App;
import com.gaborbiro.sharedexpenses.R;
import com.gaborbiro.sharedexpenses.util.ImageUtils;

import java.io.IOException;

public class ReceiptDialog extends BaseServiceDialog {

    public static void show(@NonNull Context context, Uri receiptFileUri) {
        new ReceiptDialogBuilder(context, receiptFileUri).build().show();
    }


    private ReceiptDialog(ReceiptDialogBuilder builder) {
        super(builder);
    }

    void onDelete() {
        service.sendReceiptDeletedEvent();
        dismiss();
    }

    void onChange() {
        service.sendReceiptSelectEvent();
        dismiss();
    }

    @Override
    protected void inject() {
        App.component.inject(this);
    }

    private static class ReceiptDialogBuilder extends MaterialDialog.Builder {

        @SuppressLint("InflateParams")
        ReceiptDialogBuilder(Context context, Uri receiptFileUri) {
            super(context);

            ImageView layout = (ImageView) LayoutInflater.from(context).inflate(R.layout.receipt_dialog_content, null);
            customView(layout, false);

            title(context.getString(R.string.receipt_dialog_title));
            positiveText(R.string.change);
            onPositive((dialog, which) -> ((ReceiptDialog) dialog).onChange());
            neutralText(android.R.string.cancel);
            onNeutral((dialog, which) -> dialog.dismiss());
            negativeText(R.string.remove);
            onNegative((dialog, which) -> ((ReceiptDialog) dialog).onDelete());
            try {
                layout.setImageBitmap(ImageUtils.getImageFromUri(context, receiptFileUri));
            } catch (IOException e) {
                e.printStackTrace();
            }
            autoDismiss(false);
        }

        public ReceiptDialog build() {
            return new ReceiptDialog(this);
        }
    }
}
