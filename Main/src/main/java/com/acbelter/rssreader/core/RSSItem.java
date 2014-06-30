package com.acbelter.rssreader.core;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.acbelter.rssreader.storage.RSSContentProvider;

public class RSSItem implements Parcelable {
    protected String mTitle;
    protected String mDescription;
    protected String mLink;

    public RSSItem() {
        mTitle = "";
        mDescription = "";
        mLink = "";
    }

    public RSSItem(Cursor c) {
        mTitle = c.getString(c.getColumnIndex(RSSContentProvider.ITEM_TITLE));
        mDescription = c.getString(c.getColumnIndex(RSSContentProvider.ITEM_DESCRIPTION));
        mLink = c.getString(c.getColumnIndex(RSSContentProvider.ITEM_LINK));
    }

    private RSSItem(Parcel in) {
        mTitle = in.readString();
        mDescription = in.readString();
        mLink = in.readString();
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public String getDescription() {
        return mDescription;
    }

    public void setDescription(String description) {
        mDescription = description;
    }

    public String getLink() {
        return mLink;
    }

    public void setLink(String link) {
        mLink = link;
    }

    public static final Parcelable.Creator<RSSItem> CREATOR =
            new Parcelable.Creator<RSSItem>() {
                @Override
                public RSSItem createFromParcel(Parcel in) {
                    return new RSSItem(in);
                }

                @Override
                public RSSItem[] newArray(int size) {
                    return new RSSItem[size];
                }
            };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(mTitle);
        out.writeString(mDescription);
        out.writeString(mLink);
    }

    @Override
    public String toString() {
        return "RSSItem{" +
                "mTitle='" + mTitle + '\'' +
                ", mLink='" + mLink + '\'' +
                '}';
    }
}
