package com.acbelter.rssreader.network.parser;

import android.util.Pair;
import com.acbelter.rssreader.core.RSSChannel;
import com.acbelter.rssreader.core.RSSItem;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public abstract class BaseRSSParser {
    public abstract Pair<RSSChannel, ArrayList<RSSItem>> parse(InputStream in)
            throws XmlPullParserException, IOException;
}