package com.gaborbiro.sharedexpenses.ui.activity;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.gaborbiro.sharedexpenses.App;
import com.gaborbiro.sharedexpenses.BuildConfig;
import com.gaborbiro.sharedexpenses.R;
import com.gaborbiro.sharedexpenses.model.ExpenseItem;
import com.gaborbiro.sharedexpenses.ui.HtmlHelper;
import com.gaborbiro.sharedexpenses.ui.view.EditExpenseDialog;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import butterknife.BindView;

public class WebActivity extends GoogleApiActivity implements WebScreen {

    @Inject HtmlHelper htmlHelper;

    @BindView(R.id.webview) WebView webView;

    private Map<Integer, ExpenseItem> expenses;
    private Snackbar snackbar;

    public class WebAppInterface {

        @JavascriptInterface
        public void update(final int index) {
            EditExpenseDialog.show(WebActivity.this, expenses.get(index));
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
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
        fab.setOnClickListener(view -> EditExpenseDialog.show(WebActivity.this));
        updateWithTenantNames();
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
        updateWithTenantNames();
    }

    public void updateWithTenantNames() {
        if (googleApiPresenter.verifyApiAccess()) {
            prepare(service.getExpenses())
                    .doOnTerminate(this::fetchTenantNames)
                    .subscribe(this::setExpenses);
        }
    }

    public void update() {
        if (googleApiPresenter.verifyApiAccess()) {
            if (snackbar != null) {
                snackbar.dismiss();
            }
            prepare(service.getExpenses())
                    .subscribe(this::setExpenses);
        }
    }

    private void fetchTenantNames() {
        prepare(service.getTenantNames())
                .doOnNext(tenants -> appPrefs.setTenants(tenants))
                .subscribe(tenants -> updateTenant());
    }

    public void updateTenant() {
        String selectedTenant = userPrefs.getSelectedTenant();
        if (TextUtils.isEmpty(selectedTenant) || !TextUtils.isEmpty(selectedTenant) && Arrays.binarySearch(appPrefs.getTenants(), selectedTenant) < 0) {
            chooseTenant();
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
        new MaterialDialog.Builder(this).items(appPrefs.getTenants()).itemsCallbackSingleChoice(-1, (dialog, itemView, which, text) -> {
            userPrefs.setSelectedTenant(text.toString());
            updateTitle();
            return true;
        }).title(R.string.who_are_you).show();
    }

    @Override
    public void error(String text) {
        snackbar = Snackbar.make(findViewById(R.id.coordinator_layout), text, Snackbar.LENGTH_INDEFINITE)
                .setAction("REFRESH", v -> update());
        snackbar.setActionTextColor(Color.RED);
        View sbView = snackbar.getView();
        TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(Color.YELLOW);
        snackbar.addCallback(new Snackbar.Callback(){
            @Override
            public void onDismissed(Snackbar transientBottomBar, int event) {
                snackbar = null;
            }
        });
        snackbar.show();
    }

    @SuppressLint("UseSparseArrays")
    private void setExpenses(ExpenseItem[] expenses) {
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
