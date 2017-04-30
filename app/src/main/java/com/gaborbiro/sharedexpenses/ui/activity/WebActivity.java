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
import com.gaborbiro.sharedexpenses.App;
import com.gaborbiro.sharedexpenses.BuildConfig;
import com.gaborbiro.sharedexpenses.R;
import com.gaborbiro.sharedexpenses.model.ExpenseItem;
import com.gaborbiro.sharedexpenses.tasks.FetchExpensesTask;
import com.gaborbiro.sharedexpenses.tasks.FetchTenantNamesTask;
import com.gaborbiro.sharedexpenses.ui.HtmlHelper;
import com.gaborbiro.sharedexpenses.ui.view.EditExpenseDialog;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;

import butterknife.BindView;

public class WebActivity extends GoogleApiActivity implements WebScreen {

    @Inject Provider<FetchExpensesTask> fetchExpensesTaskProvider;
    @Inject Provider<FetchTenantNamesTask> fetchTenantNamesTaskProvider;
    @Inject HtmlHelper htmlHelper;

    @BindView(R.id.webview) WebView webView;

    private Map<Integer, ExpenseItem> expenses;

    public class WebAppInterface {

        @JavascriptInterface
        public void update(final int index) {
            EditExpenseDialog.show(WebActivity.this, expenses.get(index));
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        app.setWebScreen(this);
        setContentView(com.gaborbiro.sharedexpenses.R.layout.activity_web);

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
                EditExpenseDialog.show(WebActivity.this);
            }
        });
        update();
    }

    @Override
    protected void inject() {
        App.component.inject(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        app.setWebScreen(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        app.setWebScreen(null);
    }

    @Override
    public void onPermissionsGranted() {
        update();
    }

    public void update() {
        if (googleApiPresenter.verifyApiAccess()) {
            fetchExpensesTaskProvider.get().execute();
            fetchTenantNamesTaskProvider.get().execute();
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
                userPrefs.toggleSort();
                toast(getString(R.string.sorting_by, userPrefs.getSort()));
                update();
                break;
            case R.id.action_refresh:
                update();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void chooseTenant() {
        new MaterialDialog.Builder(this).items(appPrefs.getTenants()).itemsCallbackSingleChoice(-1, new MaterialDialog.ListCallbackSingleChoice() {
            @Override
            public boolean onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
                userPrefs.setSelectedTenant(text.toString());
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
            String html = htmlHelper.getHtmlTableFromExpense(expenses);
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
        String user = userPrefs.getSelectedTenant();
        if (!TextUtils.isEmpty(user)) {
            title += " (" + userPrefs.getSelectedTenant() + ")";
        }
        title += " " + BuildConfig.VERSION_NAME;
        getSupportActionBar().setTitle(title);
    }
}
