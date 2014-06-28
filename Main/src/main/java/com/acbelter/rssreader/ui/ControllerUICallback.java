package com.acbelter.rssreader.ui;

import android.database.Cursor;

public interface ControllerUICallback {
    void setChannelsFragmentCursor(Cursor c);
    void setChannelItemsFragmentCursor(Cursor c);
}
