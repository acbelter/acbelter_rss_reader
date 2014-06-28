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
    private TextView mTitle;
    private TextView mDescription;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    public void setItem(RSSItem item) {
        mItem = item;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mItem != null) {
            mTitle.setText(mItem.getTitle());
            mDescription.setText(mItem.getDescription());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_item, container, false);
        mTitle = (TextView) view.findViewById(R.id.title);
        mDescription = (TextView) view.findViewById(R.id.description);
        return view;
    }
}
