package com.acbelter.rssreader.network.command;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.ResultReceiver;
import android.util.Pair;
import com.acbelter.nslib.command.BaseNetworkServiceCommand;
import com.acbelter.rssreader.core.Constants;
import com.acbelter.rssreader.core.RSSChannel;
import com.acbelter.rssreader.core.RSSItem;
import com.acbelter.rssreader.network.parser.RSSParser;
import com.acbelter.rssreader.network.parser.Utils;
import org.xmlpull.v1.XmlPullParserException;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class UpdateRSSDataCommand extends BaseNetworkServiceCommand {
    private ArrayList<String> mChannelsLinks;
    private int mUpdateCounter;

    public UpdateRSSDataCommand(ArrayList<String> channelsLinks) {
        mChannelsLinks = channelsLinks;
    }

    private UpdateRSSDataCommand(Parcel in) {
        mChannelsLinks = new ArrayList<String>();
        in.readStringList(mChannelsLinks);
        mUpdateCounter = in.readInt();
    }

    @Override
    protected void doExecute(Context context, Intent requestIntent, ResultReceiver callback) {
        HttpURLConnection conn = null;
        Bundle data = new Bundle();

        for (int i = 0; i < mChannelsLinks.size(); i++) {
            data.clear();
            try {
                URL url = new URL(mChannelsLinks.get(i));
                conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000);
                conn.setConnectTimeout(15000);

                if (conn.getResponseCode() == HttpsURLConnection.HTTP_BAD_REQUEST) {
                    data.putInt(Constants.KEY_EXCEPTION_CODE, Constants.CODE_ACCESS_EXCEPTION);
                    notifyFailure(data);
                    return;
                }

                RSSParser parser = new RSSParser();
                String xml = Utils.readXmlToString(conn.getInputStream());
                Pair<RSSChannel, ArrayList<RSSItem>> pair = parser.parse(xml);
                if (pair != null) {
                    data.putParcelable(Constants.KEY_RSS_CHANNEL, pair.first);
                    data.putParcelableArrayList(Constants.KEY_RSS_ITEMS, pair.second);
                    int progress = (int)(100 * ((float)(i + 1) / mChannelsLinks.size()));
                    if (progress > 100) {
                        progress = 100;
                    }
                    sendProgress(progress);

                    // Last channel has been updated
                    if (i == mChannelsLinks.size() - 1) {
                        data.putBoolean(Constants.KEY_CHANNELS_UPDATED, true);
                    }
                    notifySuccess(data);
                } else {
                    data.putInt(Constants.KEY_EXCEPTION_CODE, Constants.CODE_PARSE_EXCEPTION);
                    notifyFailure(data);
                }
            } catch (MalformedURLException e) {
                data.putInt(Constants.KEY_EXCEPTION_CODE, Constants.CODE_URL_EXCEPTION);
                notifyFailure(data);
            } catch (IOException e) {
                data.putInt(Constants.KEY_EXCEPTION_CODE, Constants.CODE_IO_EXCEPTION);
                notifyFailure(data);
            } catch (XmlPullParserException e) {
                e.printStackTrace();
                data.putInt(Constants.KEY_EXCEPTION_CODE, Constants.CODE_PARSE_EXCEPTION);
                notifyFailure(data);
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }
        }
    }

    public static final Parcelable.Creator<UpdateRSSDataCommand> CREATOR =
            new Parcelable.Creator<UpdateRSSDataCommand>() {
                @Override
                public UpdateRSSDataCommand createFromParcel(Parcel in) {
                    return new UpdateRSSDataCommand(in);
                }

                @Override
                public UpdateRSSDataCommand[] newArray(int size) {
                    return new UpdateRSSDataCommand[size];
                }
            };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeStringList(mChannelsLinks);
        out.writeInt(mUpdateCounter);
    }
}
