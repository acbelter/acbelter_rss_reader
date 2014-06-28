package com.acbelter.rssreader.core;

import android.database.Cursor;
import com.acbelter.rssreader.storage.RSSContentProvider;

public class RSSItem extends BaseRSSElement {
    public RSSItem() {
        super();
    }

    public RSSItem(Cursor c) {
        mTitle = c.getString(c.getColumnIndex(RSSContentProvider.ITEM_TITLE));
        mDescription = c.getString(c.getColumnIndex(RSSContentProvider.ITEM_DESCRIPTION));
        mLink = c.getString(c.getColumnIndex(RSSContentProvider.ITEM_LINK));
    }

    @Override
    public String toString() {
        return "RSSItem{" +
                "mTitle='" + mTitle + '\'' +
                ", mLink='" + mLink + '\'' +
                '}';
    }
}
