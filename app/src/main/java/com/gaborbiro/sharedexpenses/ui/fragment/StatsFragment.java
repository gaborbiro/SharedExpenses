package com.gaborbiro.sharedexpenses.ui.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TextView;

import com.gaborbiro.sharedexpenses.App;
import com.gaborbiro.sharedexpenses.R;
import com.gaborbiro.sharedexpenses.model.StatItem;
import com.gaborbiro.sharedexpenses.util.BottomSheetCallbackAdapter;

import butterknife.BindView;

public class StatsFragment extends BaseDialogFragment {

    private static final String EXTRA_STATS = "stats";

    @BindView(R.id.table) TableLayout table;

    private BottomSheetBehavior bottomSheetBehavior;
    private View contentView;
    private BottomSheetBehavior.BottomSheetCallback hideCallback = new BottomSheetCallbackAdapter() {

        @Override
        public void onStateChanged(@NonNull View bottomSheet, int newState) {
            switch (newState) {
                case (BottomSheetBehavior.STATE_HIDDEN):
                    // dismiss fragment when user swipes it down
                    dismiss();
                    break;
                case (BottomSheetBehavior.STATE_DRAGGING):
                    break;
                default:
                    break;
            }
        }
    };

    public static void show(AppCompatActivity activity, StatItem[] stats) {
        StatsFragment statsFragment = new StatsFragment();
        Bundle params = new Bundle();
        params.putParcelableArray(EXTRA_STATS, stats);
        statsFragment.setArguments(params);
        statsFragment.show(activity.getSupportFragmentManager(), statsFragment.getTag());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_stats, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Parcelable[] parcelables = getArguments().getParcelableArray(EXTRA_STATS);
        StatItem[] stats = new StatItem[parcelables.length];
        System.arraycopy(parcelables, 0, stats, 0, parcelables.length);
        setContent(stats);
    }

    @SuppressLint("UseSparseArrays")
    private void setContent(StatItem[] stats) {
        LayoutInflater inflater = LayoutInflater.from(getContext());

        for (StatItem statItem : stats) {
            View statRow = inflater.inflate(R.layout.table_row_stats, table, false);
            ((TextView) statRow.findViewById(R.id.name)).setText(statItem.name);
            ((TextView) statRow.findViewById(R.id.debt)).setText(statItem.debt);
            table.addView(statRow);
        }
    }

    @Override
    protected void inject() {
        App.component.inject(this);
    }

    @Override
    void onContentViewSet(View view) {
        this.contentView = view;
        bottomSheetBehavior = BottomSheetBehavior.from((View) contentView.getParent());
        bottomSheetBehavior.setBottomSheetCallback(hideCallback);
    }
}
