package com.acbelter.rssreader.network;

import android.content.Context;
import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.ResultReceiver;
import com.acbelter.nslib.command.BaseNetworkServiceCommand;

import java.util.ArrayList;

public class UpdateRSSDataCommand extends BaseNetworkServiceCommand {
    private ArrayList<String> mChannelsLinks;

    public UpdateRSSDataCommand(ArrayList<String> channelsLinks) {
        mChannelsLinks = channelsLinks;
    }

    private UpdateRSSDataCommand(Parcel in) {
        mChannelsLinks = new ArrayList<String>();
        in.readStringList(mChannelsLinks);
    }

    @Override
    protected void doExecute(Context context, Intent requestIntent, ResultReceiver callback) {

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
    }
}
