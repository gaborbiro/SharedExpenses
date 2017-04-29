package com.gaborbiro.sharedexpenses.service;

import android.Manifest;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import com.gaborbiro.sharedexpenses.App;
import com.gaborbiro.sharedexpenses.R;
import com.gaborbiro.sharedexpenses.UserPrefs;
import com.gaborbiro.sharedexpenses.tasks.DummyScreen;
import com.gaborbiro.sharedexpenses.tasks.FetchExpensesTask;
import com.gaborbiro.sharedexpenses.ui.activity.GoogleApiActivity;
import com.gaborbiro.sharedexpenses.ui.activity.MainActivity;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.util.ExponentialBackOff;

import java.util.Arrays;
import java.util.concurrent.ExecutionException;

import pub.devrel.easypermissions.EasyPermissions;

public class FetchService extends Service {

    GoogleAccountCredential credential;
    ExpenseApiImpl service;

    @Override
    public void onCreate() {
        super.onCreate();
        credential = GoogleAccountCredential.usingOAuth2(App.getAppContext(), Arrays.asList(GoogleApiActivity.SCOPES))
                .setBackOff(new ExponentialBackOff());
        service = new ExpenseApiImpl(credential);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (EasyPermissions.hasPermissions(App.getAppContext(), Manifest.permission.GET_ACCOUNTS)) {
            String accountName = UserPrefs.getAccountName();
            if (accountName != null) {
                credential.setSelectedAccountName(accountName);
            }
        }
        if (credential.getSelectedAccountName() == null) {
            return super.onStartCommand(intent, flags, startId);
        }
        FetchExpensesTask task = new FetchExpensesTask(new DummyScreen(), credential);
        task.execute();
        try {
            if (task.get().length > 0) {
                NotificationCompat.Builder mBuilder =
                        new NotificationCompat.Builder(this)
                                .setSmallIcon(R.mipmap.ic_launcher)
                                .setContentTitle("My notification")
                                .setContentText("Hello World!");
                Intent resultIntent = new Intent(this, MainActivity.class);
                TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
                stackBuilder.addNextIntent(resultIntent);
                PendingIntent resultPendingIntent =
                        stackBuilder.getPendingIntent(
                                0,
                                PendingIntent.FLAG_UPDATE_CURRENT
                        );
                mBuilder.setContentIntent(resultPendingIntent);
                NotificationManager mNotificationManager =
                        (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                mNotificationManager.notify(0, mBuilder.build());
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
