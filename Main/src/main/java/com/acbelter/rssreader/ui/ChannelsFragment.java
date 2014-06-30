package com.acbelter.rssreader.ui;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.*;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.ListView;
import com.acbelter.rssreader.R;
import com.acbelter.rssreader.core.Constants;
import com.acbelter.rssreader.storage.RSSContentProvider;

import java.util.HashSet;
import java.util.Set;

public class ChannelsFragment extends ListFragment implements
        LoaderManager.LoaderCallbacks<Cursor> {
    private SimpleCursorAdapter mAdapter;
    private MainActivity mMainActivity;
    private Set<Long> mSelectedIds;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSelectedIds = new HashSet<Long>();
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
        String[] from = {RSSContentProvider.CHANNEL_TITLE, RSSContentProvider.CHANNEL_RSS_LINK};
        int[] to = {R.id.title, R.id.rss_link};
        mAdapter = new SimpleCursorAdapter(mMainActivity, R.layout.item_rss_channel, null,
                from, to, 0);
        setListAdapter(mAdapter);

        getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        getListView().setMultiChoiceModeListener(new MultiChoiceModeListener() {
            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id,
                                                  boolean checked) {
                if (checked) {
                    mSelectedIds.add(id);
                } else {
                    mSelectedIds.remove(id);
                }
            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                mode.getMenuInflater().inflate(R.menu.menu_context, menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                if (item.getItemId() == R.id.delete_item) {
                    mMainActivity.deleteChannels(mSelectedIds);
                    mode.finish();
                    return true;
                }
                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
            }
        });

        getLoaderManager().initLoader(Constants.CHANNELS_LOADER_ID, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {
        return inflater.inflate(R.layout.fragment_channels, container, false);
    }

    @Override
    public void onListItemClick(ListView listView, View view, int position, long id) {
        super.onListItemClick(listView, view, position, id);
        Cursor c = (Cursor) mAdapter.getItem(position);
        long channelId = c.getLong(c.getColumnIndex(RSSContentProvider.CHANNEL_ID));
        mMainActivity.showChannelItemsFragment(channelId);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (id == Constants.CHANNELS_LOADER_ID) {
            return new CursorLoader(mMainActivity, RSSContentProvider.URI_CHANNELS,
                    null, null, null, null);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (loader.getId() == Constants.CHANNELS_LOADER_ID) {
            mAdapter.swapCursor(data);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (loader.getId() == Constants.CHANNELS_LOADER_ID) {
            mAdapter.swapCursor(null);
        }
    }
}
