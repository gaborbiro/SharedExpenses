package com.gaborbiro.sharedexpenses.di;

import android.app.Application;

import com.gaborbiro.sharedexpenses.ui.activity.WebActivity;
import com.gaborbiro.sharedexpenses.ui.view.EditExpenseDialog;

import javax.inject.Singleton;

import dagger.BindsInstance;
import dagger.Component;

@Singleton
@Component(modules = SharedExpensesModule.class)
public interface SharedExpensesComponent {

    @Component.Builder
    interface Builder {
        @BindsInstance
        Builder application(Application application);

        SharedExpensesComponent build();
    }

    void inject(WebActivity baseActivity);

    void inject(EditExpenseDialog.EditExpenseDialogBuilder editExpenseDialogBuilder);
}
