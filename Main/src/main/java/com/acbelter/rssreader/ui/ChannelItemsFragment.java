package com.acbelter.rssreader.ui;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import com.acbelter.rssreader.R;
import com.acbelter.rssreader.core.Constants;
import com.acbelter.rssreader.core.RSSItem;
import com.acbelter.rssreader.storage.RSSContentProvider;

public class ChannelItemsFragment extends ListFragment implements
        LoaderManager.LoaderCallbacks<Cursor> {
    private long mChannelId;
    private SimpleCursorAdapter mAdapter;
    private MainActivity mMainActivity;

    public static ChannelItemsFragment newInstance(long channelId) {
        ChannelItemsFragment channelItemsFragment = new ChannelItemsFragment();
        channelItemsFragment.mChannelId = channelId;
        return channelItemsFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mMainActivity = (MainActivity) activity;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        String[] from = {RSSContentProvider.ITEM_TITLE, RSSContentProvider.ITEM_DESCRIPTION};
        int[] to = {R.id.title, R.id.description};
        mAdapter = new SimpleCursorAdapter(getActivity(), R.layout.item_rss_item, null,
                from, to, 0);
        setListAdapter(mAdapter);

        Bundle args = new Bundle();
        args.putLong(Constants.KEY_CHANNEL_ID, mChannelId);
        getLoaderManager().initLoader(Constants.CHANNEL_ITEMS_LOADER_ID, args, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {
        return inflater.inflate(R.layout.fragment_channel_items, container, false);
    }

    @Override
    public void onListItemClick(ListView listView, View view, int position, long id) {
        super.onListItemClick(listView, view, position, id);
        Cursor c = (Cursor) mAdapter.getItem(position);
        RSSItem item = new RSSItem(c);
        mMainActivity.showItemFragment(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (id == Constants.CHANNEL_ITEMS_LOADER_ID) {
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
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (loader.getId() == Constants.CHANNEL_ITEMS_LOADER_ID) {
            mAdapter.swapCursor(data);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (loader.getId() == Constants.CHANNEL_ITEMS_LOADER_ID) {
            mAdapter.swapCursor(null);
        }
    }
}
