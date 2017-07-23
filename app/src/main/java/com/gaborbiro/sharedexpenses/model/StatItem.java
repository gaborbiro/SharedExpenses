package com.gaborbiro.sharedexpenses.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.gaborbiro.sharedexpenses.SpreadsheetException;
import com.gaborbiro.sharedexpenses.api.SheetRowReader;

import java.util.List;

public class StatItem extends BaseItem implements Parcelable {
    private static final String COLUMN_NAME = "Name";
    private static final String COLUMN_DEBT = "Debt";

    public final String name;
    public final String debt;

    public StatItem(int index, String name, String debt) {
        super(index);
        this.name = name;
        this.debt = debt;
    }

    public static SheetRowReader<StatItem> getReader(List<Object> header) throws SpreadsheetException {
        return new SheetRowReader<>(header, new String[]{
                COLUMN_NAME,
                COLUMN_DEBT},
                StatItem.class);
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(index);
        dest.writeString(this.name);
        dest.writeString(this.debt);
    }

    protected StatItem(Parcel in) {
        super(in.readInt());
        this.name = in.readString();
        this.debt = in.readString();
    }

    public static final Parcelable.Creator<StatItem> CREATOR = new Parcelable.Creator<StatItem>() {
        @Override
        public StatItem createFromParcel(Parcel source) {
            return new StatItem(source);
        }

        @Override
        public StatItem[] newArray(int size) {
            return new StatItem[size];
        }
    };
}
