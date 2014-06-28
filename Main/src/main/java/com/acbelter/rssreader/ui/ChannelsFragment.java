package com.acbelter.rssreader.ui;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import com.acbelter.rssreader.R;
import com.acbelter.rssreader.storage.RSSContentProvider;

public class ChannelsFragment extends ListFragment {
    private SimpleCursorAdapter mAdapter;
    private MainActivity mMainActivity;

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
        String[] from = {RSSContentProvider.CHANNEL_TITLE, RSSContentProvider.CHANNEL_LINK};
        int[] to = {R.id.title, R.id.link};
        if (mAdapter == null) {
            mAdapter = new SimpleCursorAdapter(mMainActivity, R.layout.item_rss_channel, null,
                    from, to, 0);
        }
        setListAdapter(mAdapter);
    }

    public SimpleCursorAdapter getAdapter() {
        return mAdapter;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {
        return inflater.inflate(R.layout.fragment_channels, container, false);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        Cursor c = (Cursor) mAdapter.getItem(position);
        int channelId = c.getInt(c.getColumnIndex(RSSContentProvider.CHANNEL_ID));
        mMainActivity.showChannelItems(channelId);
    }
}
