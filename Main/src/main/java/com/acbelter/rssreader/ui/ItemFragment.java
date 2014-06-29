package com.acbelter.rssreader.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.acbelter.rssreader.R;
import com.acbelter.rssreader.core.RSSItem;

public class ItemFragment extends Fragment {
    private RSSItem mItem;

    public static ItemFragment newInstance(RSSItem item) {
        ItemFragment itemFragment = new ItemFragment();
        itemFragment.mItem = item;
        return itemFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_item, container, false);
        TextView title = (TextView) view.findViewById(R.id.title);
        TextView description = (TextView) view.findViewById(R.id.description);
        if (mItem != null) {
            title.setText(mItem.getTitle());
            description.setText(mItem.getDescription());
        }
        return view;
    }
}
