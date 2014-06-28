package com.acbelter.rssreader.ui;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import com.acbelter.rssreader.core.Constants;
import com.acbelter.rssreader.core.RSSChannel;
import com.acbelter.rssreader.core.RSSItem;
import com.acbelter.rssreader.storage.RSSContentProvider;

import java.util.ArrayList;

public final class Controller implements LoaderManager.LoaderCallbacks<Cursor> {
    private MainActivity mMainActivity;
    private ControllerUICallback mUICallback;
    private LoaderManager mLoaderManager;
    private ContentResolver mContentResolver;

    private static final int CHANNELS_LOADER_ID = 1;
    private static final int CHANNEL_ITEMS_LOADER_ID = 2;

    public Controller(MainActivity mainActivity) {
        mLoaderManager = mainActivity.getSupportLoaderManager();
        mContentResolver = mainActivity.getContentResolver();
        mMainActivity = mainActivity;
        mUICallback = mainActivity;
    }

    public void loadChannels() {
        mLoaderManager.initLoader(CHANNELS_LOADER_ID, null, this);
    }

    public void loadChannelItems(long channelId) {
        Bundle args = new Bundle();
        args.putLong(Constants.KEY_CHANNEL_ID, channelId);
        mLoaderManager.initLoader(CHANNEL_ITEMS_LOADER_ID, args, this);
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
        if (c != null && c.getCount() > 0) {
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
        // TODO
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case CHANNELS_LOADER_ID: {
                return new CursorLoader(mMainActivity, RSSContentProvider.URI_CHANNELS,
                        null, null, null, null);
            }
            case CHANNEL_ITEMS_LOADER_ID: {
                if (!args.containsKey(Constants.KEY_CHANNEL_ID)) {
                    throw new IllegalArgumentException("Channel items loader must contains " +
                            "channel identifier in args.");
                }

                long channelId = args.getLong(Constants.KEY_CHANNEL_ID);
                String selection = RSSContentProvider.ITEM_CHANNEL_ID + "=?";
                String[] selectionArgs = new String[]{Long.toString(channelId)};
                return new CursorLoader(mMainActivity, RSSContentProvider.URI_ITEMS,
                        null, selection, selectionArgs, null);
            }
            default: {
                throw new IllegalArgumentException("Unsupported loader id: " + id);
            }
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        switch (loader.getId()) {
            case CHANNELS_LOADER_ID: {
                mUICallback.setChannelsFragmentCursor(data);
                break;
            }
            case CHANNEL_ITEMS_LOADER_ID: {
                mUICallback.setChannelItemsFragmentCursor(data);
                break;
            }
            default: {
                throw new IllegalArgumentException("Unsupported loader id: " + loader.getId());
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        switch (loader.getId()) {
            case CHANNELS_LOADER_ID: {
                mUICallback.setChannelsFragmentCursor(null);
                break;
            }
            case CHANNEL_ITEMS_LOADER_ID: {
                mUICallback.setChannelItemsFragmentCursor(null);
                break;
            }
            default: {
                throw new IllegalArgumentException("Unsupported loader id: " + loader.getId());
            }
        }
    }
}
