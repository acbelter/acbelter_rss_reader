package com.acbelter.rssreader.core;

public class Constants {
    public static final int CODE_URL_EXCEPTION = -1;
    public static final int CODE_IO_EXCEPTION = -2;
    public static final int CODE_ACCESS_EXCEPTION = -3;
    public static final int CODE_PARSE_EXCEPTION = -4;
    public static final int CODE_UNKNOWN_EXCEPTION = -5;

    public static final String KEY_EXCEPTION_CODE =
            "com.acbelter.rssreader.KEY_EXCEPTION_CODE";
    public static final String KEY_RSS_CHANNEL =
            "com.acbelter.rssreader.KEY_RSS_CHANNEL";
    public static final String KEY_RSS_ITEMS =
            "com.acbelter.rssreader.KEY_RSS_ITEMS";
    public static final String KEY_CHANNEL_ID =
            "com.acbelter.rssreader.KEY_CHANNEL_ID";
    public static final String KEY_GET_REQUEST_ID =
            "com.acbelter.rssreader.KEY_GET_REQUEST_ID";
    public static final String KEY_UPDATE_REQUEST_ID =
            "com.acbelter.rssreader.KEY_UPDATE_REQUEST_ID";

    public static final String PREF_FIRST_RUN = "first_run";

    public static final int CHANNELS_LOADER_ID = 1;
    public static final int CHANNEL_ITEMS_LOADER_ID = 2;
}
