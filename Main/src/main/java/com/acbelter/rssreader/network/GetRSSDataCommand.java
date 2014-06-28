package com.acbelter.rssreader.network;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.ResultReceiver;
import android.util.Pair;
import com.acbelter.nslib.command.BaseNetworkServiceCommand;
import com.acbelter.rssreader.R;
import com.acbelter.rssreader.core.Constants;
import com.acbelter.rssreader.core.RSSChannel;
import com.acbelter.rssreader.core.RSSItem;
import com.acbelter.rssreader.network.parser.RSSParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class GetRSSDataCommand extends BaseNetworkServiceCommand {
    private String mLink;

    public GetRSSDataCommand(String link) {
        mLink = link;
    }

    private GetRSSDataCommand(Parcel in) {
        mLink = in.readString();
    }

    @Override
    protected void doExecute(Context context, Intent requestIntent, ResultReceiver callback) {
        InputStream is = context.getResources().openRawResource(R.raw.test);
        RSSParser parser = new RSSParser();
        Bundle data = new Bundle();
        Pair<RSSChannel, ArrayList<RSSItem>> pair;
        try {
            pair = parser.parse(is);
            if (pair != null) {
                data.putParcelable(Constants.KEY_RSS_CHANNEL, pair.first);
                data.putParcelableArrayList(Constants.KEY_RSS_ITEMS, pair.second);
                notifySuccess(data);
            } else {
                data.putInt(Constants.KEY_EXCEPTION_CODE, Constants.CODE_PARSE_EXCEPTION);
                notifyFailure(data);
            }
        } catch (IOException e) {
            data.putInt(Constants.KEY_EXCEPTION_CODE, Constants.CODE_IO_EXCEPTION);
            notifyFailure(data);
        } catch (XmlPullParserException e) {
            data.putInt(Constants.KEY_EXCEPTION_CODE, Constants.CODE_PARSE_EXCEPTION);
            notifyFailure(data);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }



//        HttpsURLConnection conn = null;
//        Bundle data = new Bundle();
//        try {
//            URL url = new URL(mLink);
//            conn = (HttpsURLConnection) url.openConnection();
//            conn.setReadTimeout(10000 /* milliseconds */);
//            conn.setConnectTimeout(15000 /* milliseconds */);
//            if (conn.getResponseCode() == HttpsURLConnection.HTTP_BAD_REQUEST) {
//                data.putInt(Constants.KEY_EXCEPTION_CODE, Constants.CODE_ACCESS_EXCEPTION);
//                notifyFailure(data);
//                return;
//            }
//
//            RSSParser parser = new RSSParser();
//            Pair<RSSChannel, ArrayList<RSSItem>> pair = parser.parse(conn.getInputStream());
//            if (pair != null) {
//                data.putParcelable(Constants.KEY_RSS_CHANNEL, pair.first);
//                data.putParcelableArrayList(Constants.KEY_RSS_ITEMS, pair.second);
//                notifySuccess(data);
//            } else {
//                data.putInt(Constants.KEY_EXCEPTION_CODE, Constants.CODE_PARSE_EXCEPTION);
//                notifyFailure(data);
//            }
//        } catch (MalformedURLException e) {
//            data.putInt(Constants.KEY_EXCEPTION_CODE, Constants.CODE_URL_EXCEPTION);
//            notifyFailure(data);
//        } catch (IOException e) {
//            data.putInt(Constants.KEY_EXCEPTION_CODE, Constants.CODE_IO_EXCEPTION);
//            notifyFailure(data);
//        } catch (XmlPullParserException e) {
//            data.putInt(Constants.KEY_EXCEPTION_CODE, Constants.CODE_PARSE_EXCEPTION);
//            notifyFailure(data);
//        } finally {
//            if (conn != null) {
//                conn.disconnect();
//            }
//        }
    }

    public static final Parcelable.Creator<GetRSSDataCommand> CREATOR =
            new Parcelable.Creator<GetRSSDataCommand>() {
                @Override
                public GetRSSDataCommand createFromParcel(Parcel in) {
                    return new GetRSSDataCommand(in);
                }

                @Override
                public GetRSSDataCommand[] newArray(int size) {
                    return new GetRSSDataCommand[size];
                }
            };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(mLink);
    }
}
