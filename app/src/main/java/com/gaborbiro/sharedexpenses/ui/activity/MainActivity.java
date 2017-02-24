package com.gaborbiro.sharedexpenses.ui.activity;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.afollestad.materialdialogs.MaterialDialog;
import com.gaborbiro.sharedexpenses.BuildConfig;
import com.gaborbiro.sharedexpenses.Constants;
import com.gaborbiro.sharedexpenses.R;
import com.gaborbiro.sharedexpenses.UserPrefs;
import com.gaborbiro.sharedexpenses.model.Tenants;
import com.gaborbiro.sharedexpenses.tasks.FetchExpensesTask;
import com.gaborbiro.sharedexpenses.tasks.FetchTenantNamesTask;
import com.gaborbiro.sharedexpenses.ui.presenter.MainPresenter;
import com.gaborbiro.sharedexpenses.ui.screen.MainScreen;
import com.gaborbiro.sharedexpenses.ui.view.NewExpenseDialog;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class MainActivity extends GoogleApiActivity implements MainScreen {

    @InjectView(R.id.table)
    WebView outputView;

    private MainPresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.gaborbiro.sharedexpenses.R.layout.activity_main);
        ButterKnife.inject(this);

        presenter = new MainPresenter(this);

        outputView.getSettings().setJavaScriptEnabled(true);
        outputView.setWebViewClient(new WebViewClient());
        outputView.setWebChromeClient(new WebChromeClient());
        outputView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        outputView.setScrollbarFadingEnabled(true);

        Toolbar toolbar = (Toolbar) findViewById(com.gaborbiro.sharedexpenses.R.id.toolbar);
        setSupportActionBar(toolbar);

        updateTitle();

        FloatingActionButton fab = (FloatingActionButton) findViewById(com.gaborbiro.sharedexpenses.R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new NewExpenseDialog.Builder(MainActivity.this, MainActivity.this, credential).build().show();
            }
        });

//        Intent intent = new Intent(this, FetchService.class);
//        PendingIntent pintent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
//        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
//        int interval = 5 * 1000;
//        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 1000, interval, pintent);

        getDataFromApi();
    }

    @Override
    public void onPermissionsGranted() {
        getDataFromApi();
    }

    public void getDataFromApi() {
        if (googleApiPresenter.verifyApiAccess()) {
            new FetchExpensesTask(this, credential).execute();
            new FetchTenantNamesTask(this, credential).execute();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_sort_by:
                String newSort;
                switch (UserPrefs.getSort(Constants.DEFAULT_SORT)) {
                    case Constants.SORT_DATE:
                        newSort = Constants.SORT_USER;
                        break;
                    case Constants.SORT_USER:
                        newSort = Constants.SORT_DATE;
                        break;
                    default:
                        newSort = Constants.DEFAULT_SORT;
                        break;
                }
                toast(getString(R.string.sorting_by, newSort));
                UserPrefs.setSort(newSort);
                getDataFromApi();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void chooseTenant() {
        new MaterialDialog.Builder(this).items(Tenants.getTenants()).itemsCallbackSingleChoice(-1, new MaterialDialog.ListCallbackSingleChoice() {
            @Override
            public boolean onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
                UserPrefs.setSelectedTenant(text.toString());
                updateTitle();
                return true;
            }
        }).title(R.string.who_are_you).show();
    }

    @Override
    public void error(String text) {
        outputView.loadData(text, "", "");
    }

    @Override
    public void data(String data) {
        outputView.loadData(data, "", "");
    }

    public void updateTitle() {
        String title = getString(R.string.app_name);
        String user = UserPrefs.getSelectedTenant();
        if (!TextUtils.isEmpty(user)) {
            title += " (" + UserPrefs.getSelectedTenant() + ")";
        }
        title += " " + BuildConfig.VERSION_NAME;
        getSupportActionBar().setTitle(title);
    }
}
