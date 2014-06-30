package com.acbelter.rssreader.network;

import android.content.Intent;
import com.acbelter.nslib.BaseNetworkServiceHelper;
import com.acbelter.nslib.NetworkServiceCallbackListener;
import com.acbelter.nslib.NetworkServiceHelper;
import com.acbelter.nslib.command.BaseNetworkServiceCommand;
import com.acbelter.rssreader.network.command.GetRSSDataCommand;
import com.acbelter.rssreader.network.command.UpdateRSSDataCommand;

import java.util.ArrayList;

public class SimpleNetworkServiceHelper implements NetworkServiceHelper {
    private BaseNetworkServiceHelper mBaseNetworkServiceHelper;

    public SimpleNetworkServiceHelper(BaseNetworkServiceHelper baseNetworkServiceHelper) {
        mBaseNetworkServiceHelper = baseNetworkServiceHelper;
    }

    public int getRSSData(String channelLink) {
        final int requestId = mBaseNetworkServiceHelper.createCommandId();
        Intent requestIntent = mBaseNetworkServiceHelper
                .buildRequestIntent(new GetRSSDataCommand(channelLink), requestId);
        return mBaseNetworkServiceHelper.executeRequest(requestId, requestIntent);
    }

    public int updateRSSData(ArrayList<String> channelLinks) {
        final int requestId = mBaseNetworkServiceHelper.createCommandId();
        Intent requestIntent = mBaseNetworkServiceHelper
                .buildRequestIntent(new UpdateRSSDataCommand(channelLinks), requestId);
        return mBaseNetworkServiceHelper.executeRequest(requestId, requestIntent);
    }

    @Override
    public void addListener(NetworkServiceCallbackListener callback) {
        mBaseNetworkServiceHelper.addListener(callback);
    }

    @Override
    public void removeListener(NetworkServiceCallbackListener callback) {
        mBaseNetworkServiceHelper.removeListener(callback);
    }

    @Override
    public void cancelRequest(int requestId) {
        mBaseNetworkServiceHelper.cancelRequest(requestId);
    }

    @Override
    public boolean checkCommandClass(Intent requestIntent,
                                     Class<? extends BaseNetworkServiceCommand> commandClass) {
        return mBaseNetworkServiceHelper.checkCommandClass(requestIntent, commandClass);
    }

    @Override
    public boolean isPending(int requestId) {
        return mBaseNetworkServiceHelper.isPending(requestId);
    }
}
