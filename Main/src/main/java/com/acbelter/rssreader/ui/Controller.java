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

    public ArrayList<String> getChannelsRSSLinks() {
        Cursor c = mContentResolver
                .query(RSSContentProvider.URI_CHANNELS, null, null, null, null);
        if (c == null || c.isClosed()) {
            return new ArrayList<String>(0);
        }

        ArrayList<String> result = new ArrayList<String>(c.getCount());
        while (c.moveToNext()) {
            result.add(c.getString(c.getColumnIndex(RSSContentProvider.CHANNEL_RSS_LINK)));
        }
        return result;
    }

    public void clearData() {
        mContentResolver.delete(RSSContentProvider.URI_CHANNELS, null, null);
        mContentResolver.delete(RSSContentProvider.URI_ITEMS, null, null);
    }

    public boolean isChannelExists(String rssLink) {
        String selection = RSSContentProvider.CHANNEL_RSS_LINK + "=?";
        String[] selectionArgs = new String[]{rssLink};
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
        Uri newUri = mContentResolver.insert(RSSContentProvider.URI_CHANNELS,
                makeChannelValues(channel));
        return Long.parseLong(newUri.getLastPathSegment());
    }

    public void insertChannelItems(long channelId, ArrayList<RSSItem> items) {
        for (int i = 0; i < items.size(); i++) {
            mContentResolver.insert(RSSContentProvider.URI_ITEMS,
                    makeItemValues(channelId, items.get(i)));
        }
    }

    private static ContentValues makeChannelValues(RSSChannel channel) {
        ContentValues cv = new ContentValues();
        cv.put(RSSContentProvider.CHANNEL_RSS_LINK, channel.getRssLink());
        cv.put(RSSContentProvider.CHANNEL_TITLE, channel.getTitle());
        cv.put(RSSContentProvider.CHANNEL_DESCRIPTION, channel.getDescription());
        cv.put(RSSContentProvider.CHANNEL_LINK, channel.getLink());
        return cv;
    }

    private static ContentValues makeItemValues(long channelId, RSSItem item) {
        ContentValues cv = new ContentValues();
        cv.put(RSSContentProvider.ITEM_CHANNEL_ID, channelId);
        cv.put(RSSContentProvider.ITEM_TITLE, item.getTitle());
        cv.put(RSSContentProvider.ITEM_DESCRIPTION, item.getDescription());
        cv.put(RSSContentProvider.ITEM_LINK, item.getLink());
        return cv;
    }

    public void deleteChannelWithItems(long channelId) {
        mContentResolver.delete(RSSContentProvider.URI_CHANNELS, RSSContentProvider.CHANNEL_ID +
                "=?", new String[]{Long.toString(channelId)});
        mContentResolver.delete(RSSContentProvider.URI_ITEMS, RSSContentProvider.ITEM_CHANNEL_ID +
                "=?", new String[]{Long.toString(channelId)});
    }

    private void updateChannelItems(String rssLink, ArrayList<RSSItem> items) {
        String selection = RSSContentProvider.CHANNEL_RSS_LINK + "=?";
        String[] selectionArgs = new String[]{rssLink};
        Cursor c = mContentResolver
                .query(RSSContentProvider.URI_CHANNELS, null, selection, selectionArgs, null);
        if (c != null && !c.isClosed() && c.getCount() > 0) {
            // If there are several channels with same RSS links
            long channelId;
            while (c.moveToNext()) {
                channelId = c.getLong(c.getColumnIndex(RSSContentProvider.CHANNEL_ID));
                mContentResolver.delete(RSSContentProvider.URI_ITEMS,
                        RSSContentProvider.ITEM_CHANNEL_ID + "=?",
                        new String[]{Long.toString(channelId)});
                insertChannelItems(channelId, items);
            }
        }
    }

    public void updateChannel(RSSChannel channel, ArrayList<RSSItem> items) {
        ContentValues channelValues = makeChannelValues(channel);
        mContentResolver.update(RSSContentProvider.URI_CHANNELS, channelValues,
                RSSContentProvider.CHANNEL_RSS_LINK + "=?", new String[]{channel.getRssLink()});
        updateChannelItems(channel.getRssLink(), items);
    }
}
