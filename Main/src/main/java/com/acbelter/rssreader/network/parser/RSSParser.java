package com.acbelter.rssreader.network.parser;

import android.util.Pair;
import android.util.Xml;
import com.acbelter.rssreader.core.RSSChannel;
import com.acbelter.rssreader.core.RSSItem;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

public class RSSParser extends BaseRSSParser {
    private static final String NAMESPACE = null;

    @Override
    public Pair<RSSChannel, ArrayList<RSSItem>> parse(String xml)
            throws XmlPullParserException, IOException {
        XmlPullParser parser = Xml.newPullParser();
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
        parser.setInput(new StringReader(xml));
        parser.nextTag();
        return readRSS(parser);
    }

    private Pair<RSSChannel, ArrayList<RSSItem>> readRSS(XmlPullParser parser)
            throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, NAMESPACE, "rss");
        String version = parser.getAttributeValue(null, "version");
        if (version.equals("2.0")) {
            return readRSS20(parser);
        } else if (version.equals("1.0")) {
            throw new XmlPullParserException("Unsupported version of RSS: " + version);
        } else {
            throw new XmlPullParserException("Unsupported version of RSS: " + version);
        }
    }

    private Pair<RSSChannel, ArrayList<RSSItem>> readRSS20(XmlPullParser parser)
            throws XmlPullParserException, IOException {
        Pair<RSSChannel, ArrayList<RSSItem>> result = null;

        parser.require(XmlPullParser.START_TAG, NAMESPACE, "rss");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }

            String name = parser.getName();
            if (name.equals("channel")) {
                result = readChannel(parser);
            } else {
                skip(parser);
            }
        }
        return result;
    }

    private Pair<RSSChannel, ArrayList<RSSItem>> readChannel(XmlPullParser parser)
            throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, NAMESPACE, "channel");

        String title = "";
        String description = "";
        String link = "";
        ArrayList<RSSItem> items = new ArrayList<RSSItem>();
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }

            String name = parser.getName();
            if (name.equals("title")) {
                title = readTitle(parser);
            } else if (name.equals("description")) {
                description = readDescription(parser);
            } else if (name.equals("link")) {
                link = readLink(parser);
            } else if (name.equals("item")) {
                items.add(readItem(parser));
            } else {
                skip(parser);
            }
        }

        RSSChannel channel = new RSSChannel();
        channel.setTitle(title);
        channel.setDescription(description);
        channel.setLink(link);
        return new Pair<RSSChannel, ArrayList<RSSItem>>(channel, items);
    }

    private String readTitle(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, NAMESPACE, "title");
        String title = readText(parser);
        parser.require(XmlPullParser.END_TAG, NAMESPACE, "title");
        return title;
    }

    private String readLink(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, NAMESPACE, "link");
        String link = readText(parser);
        parser.require(XmlPullParser.END_TAG, NAMESPACE, "link");
        return link;
    }

    private String readDescription(XmlPullParser parser)
            throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, NAMESPACE, "description");
        String description = readText(parser);
        parser.require(XmlPullParser.END_TAG, NAMESPACE, "description");
        return description;
    }

    private RSSItem readItem(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, NAMESPACE, "item");
        String title = "";
        String description = "";
        String link = "";
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }

            String name = parser.getName();
            if (name.equals("title")) {
                title = readTitle(parser);
            } else if (name.equals("description")) {
                description = readDescription(parser);
            } else if (name.equals("link")) {
                link = readLink(parser);
            } else {
                skip(parser);
            }
        }

        RSSItem item = new RSSItem();
        item.setTitle(title);
        item.setLink(link);
        item.setDescription(description);
        return item;
    }

    // Extracts text values
    private String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
        String result = "";
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        }
        return result;
    }

    private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }

        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }
}
