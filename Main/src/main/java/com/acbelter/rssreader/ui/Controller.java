package com.acbelter.rssreader.ui;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import com.acbelter.rssreader.core.RSSChannel;
import com.acbelter.rssreader.core.RSSItem;
import com.acbelter.rssreader.storage.RSSContentProvider;

import java.util.ArrayList;

public final class Controller {
    private ContentResolver mContentResolver;

    public Controller(MainActivity mainActivity) {
        mContentResolver = mainActivity.getContentResolver();
    }

    public void clearData() {
        mContentResolver.delete(RSSContentProvider.URI_CHANNELS, null, null);
        mContentResolver.delete(RSSContentProvider.URI_ITEMS, null, null);
    }

    public boolean isChannelExists(String link) {
        String selection = RSSContentProvider.CHANNEL_LINK + "=?";
        String[] selectionArgs = new String[]{link};
        Cursor c = mContentResolver
                .query(RSSContentProvider.URI_CHANNELS, null, selection, selectionArgs, null);
        if (c != null && !c.isClosed() && c.getCount() > 0) {
            return true;
        }
        return false;
    }

    /**
     * Insert new channel to ContentProvider.
     * @param channel
     * @return id of inserted channel.
     */
    public long insertChannel(RSSChannel channel) {
        ContentValues cv = new ContentValues();
        cv.put(RSSContentProvider.CHANNEL_TITLE, channel.getTitle());
        cv.put(RSSContentProvider.CHANNEL_DESCRIPTION, channel.getDescription());
        cv.put(RSSContentProvider.CHANNEL_LINK, channel.getLink());

        Uri newUri = mContentResolver.insert(RSSContentProvider.URI_CHANNELS, cv);
        return Long.parseLong(newUri.getLastPathSegment());
    }

    public void insertChannelItems(long channelId, ArrayList<RSSItem> items) {
        ContentValues cv = new ContentValues();
        RSSItem item;
        for (int i = 0; i < items.size(); i++) {
            item = items.get(i);
            cv.put(RSSContentProvider.ITEM_CHANNEL_ID, channelId);
            cv.put(RSSContentProvider.ITEM_TITLE, item.getTitle());
            cv.put(RSSContentProvider.ITEM_DESCRIPTION, item.getDescription());
            cv.put(RSSContentProvider.ITEM_LINK, item.getLink());
            mContentResolver.insert(RSSContentProvider.URI_ITEMS, cv);
        }
    }

    public void deleteChannel(long channelId) {
        mContentResolver.delete(RSSContentProvider.URI_CHANNELS, RSSContentProvider.CHANNEL_ID +
                "=?", new String[]{Long.toString(channelId)});
        mContentResolver.delete(RSSContentProvider.URI_ITEMS, RSSContentProvider.ITEM_CHANNEL_ID +
                "=?", new String[]{Long.toString(channelId)});
    }
}
