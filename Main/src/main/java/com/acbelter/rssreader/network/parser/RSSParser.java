package com.acbelter.rssreader.network.parser;

import android.util.Pair;
import com.acbelter.rssreader.core.RSSChannel;
import com.acbelter.rssreader.core.RSSItem;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;

public interface RSSParser {
    Pair<RSSChannel, ArrayList<RSSItem>> parse(String xml)
        throws XmlPullParserException, IOException;
}
