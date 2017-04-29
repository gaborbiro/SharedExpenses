package com.gaborbiro.sharedexpenses.ui.activity;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.gaborbiro.sharedexpenses.BuildConfig;
import com.gaborbiro.sharedexpenses.Constants;
import com.gaborbiro.sharedexpenses.R;
import com.gaborbiro.sharedexpenses.UserPrefs;
import com.gaborbiro.sharedexpenses.model.ExpenseItem;
import com.gaborbiro.sharedexpenses.model.Tenants;
import com.gaborbiro.sharedexpenses.tasks.FetchExpensesTask;
import com.gaborbiro.sharedexpenses.tasks.FetchTenantNamesTask;
import com.gaborbiro.sharedexpenses.ui.HtmlUtil;
import com.gaborbiro.sharedexpenses.ui.presenter.MainPresenter;
import com.gaborbiro.sharedexpenses.ui.screen.MainScreen;
import com.gaborbiro.sharedexpenses.ui.view.EditExpenseDialog;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class MainActivity extends GoogleApiActivity implements MainScreen {

    @InjectView(R.id.webview) WebView webView;

    private MainPresenter presenter;
    private Map<Integer, ExpenseItem> expenses;

    public class WebAppInterface {

        @JavascriptInterface
        public void update(final int index) {
            webView.post(new Runnable() {
                @Override
                public void run() {
                    EditExpenseDialog.show(MainActivity.this, MainActivity.this, credential, expenses.get(index));
                }
            });
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.gaborbiro.sharedexpenses.R.layout.activity_main);
        ButterKnife.inject(this);

        presenter = new MainPresenter(this);

        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebChromeClient(new WebChromeClient());
        webView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        webView.setScrollbarFadingEnabled(true);
        webView.addJavascriptInterface(new WebAppInterface(), "android");

        Toolbar toolbar = (Toolbar) findViewById(com.gaborbiro.sharedexpenses.R.id.toolbar);
        setSupportActionBar(toolbar);

        updateTitle();

        FloatingActionButton fab = (FloatingActionButton) findViewById(com.gaborbiro.sharedexpenses.R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditExpenseDialog.show(MainActivity.this, MainActivity.this, credential);
            }
        });
        update();
    }

    @Override
    public void onPermissionsGranted() {
        update();
    }

    public void update() {
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
                update();
                break;
            case R.id.action_refresh:
                update();
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
        webView.loadDataWithBaseURL("file:///android_asset/", text, "text/html", "UTF-8", null);
    }

    @Override
    public void setExpenses(ExpenseItem[] expenses) {
//        webView.loadUrl("file:///android_asset/test.html");
        try {
            String html = HtmlUtil.getHtmlTableFromExpense(this, expenses);
            webView.loadDataWithBaseURL("file:///android_asset/", html, "text/html", "UTF-8", null);
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.expenses = new HashMap<>();

        for (ExpenseItem expense : expenses) {
            this.expenses.put(expense.index, expense);
        }
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
