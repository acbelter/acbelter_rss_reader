package com.acbelter.rssreader.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import com.acbelter.nslib.NetworkApplication;
import com.acbelter.nslib.NetworkServiceCallbackListener;
import com.acbelter.rssreader.R;
import com.acbelter.rssreader.core.Constants;
import com.acbelter.rssreader.core.RSSChannel;
import com.acbelter.rssreader.core.RSSItem;
import com.acbelter.rssreader.network.SimpleNetworkServiceHelper;
import com.acbelter.rssreader.network.command.GetRSSDataCommand;
import com.acbelter.rssreader.network.command.UpdateRSSDataCommand;

import java.util.ArrayList;
import java.util.Set;

public class MainActivity extends ActionBarActivity implements NetworkServiceCallbackListener {
    private FragmentManager mFragmentManager;

    private SimpleNetworkServiceHelper mServiceHelper;
    private int mGetRequestId = -1;
    private int mUpdateRequestId = -1;

    private Controller mController;

    private static final String TAG_CF = ChannelsFragment.class.getSimpleName();
    private static final String TAG_CIF = ChannelItemsFragment.class.getSimpleName();
    private static final String TAG_IF = ItemFragment.class.getSimpleName();

    private NetworkApplication getApp() {
        return (NetworkApplication) getApplication();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mFragmentManager = getSupportFragmentManager();
        mServiceHelper = new SimpleNetworkServiceHelper(getApp().getNetworkServiceHelper());
        mController = new Controller(this);
        writeFirstRunRSSChannels();

        if (savedInstanceState == null) {
            showChannelsFragment();
        } else {
            mGetRequestId = savedInstanceState.getInt(Constants.KEY_GET_REQUEST_ID);
            mUpdateRequestId = savedInstanceState.getInt(Constants.KEY_UPDATE_REQUEST_ID);
        }
    }

    private void writeFirstRunRSSChannels() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (prefs.getBoolean(Constants.PREF_FIRST_RUN, true)) {
            String[] rssChannelsLinks = getResources().getStringArray(R.array.init_rss_channels);
            for (String rssLink : rssChannelsLinks) {
                RSSChannel channel = new RSSChannel();
                channel.setRssLink(rssLink);
                mController.insertChannel(channel);
            }
            prefs.edit().putBoolean(Constants.PREF_FIRST_RUN, false).commit();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(Constants.KEY_GET_REQUEST_ID, mGetRequestId);
        outState.putInt(Constants.KEY_UPDATE_REQUEST_ID, mUpdateRequestId);
    }

    public void deleteChannels(Set<Long> channelIds) {
        for (Long id : channelIds) {
            mController.deleteChannelWithItems(id);
        }
        channelIds.clear();
    }

    private void showChannelsFragment() {
        FragmentTransaction ft = mFragmentManager.beginTransaction();
        ft.replace(R.id.content_frame, new ChannelsFragment(), TAG_CF);
        ft.commit();
    }

    public void showChannelItemsFragment(final long channelId) {
        FragmentTransaction ft = mFragmentManager.beginTransaction();
        ft.replace(R.id.content_frame, ChannelItemsFragment.newInstance(channelId), TAG_CIF);
        ft.addToBackStack(null);
        ft.commit();
    }

    public void showItemFragment(final RSSItem item) {
        FragmentTransaction ft = mFragmentManager.beginTransaction();
        ft.replace(R.id.content_frame, ItemFragment.newInstance(item), TAG_IF);
        ft.addToBackStack(null);
        ft.commit();
    }

    public void showDuplicateChannelToast() {
        Toast.makeText(getApplicationContext(), getString(R.string.toast_duplicate_channel),
                Toast.LENGTH_LONG).show();
    }

    private void showChannelsUpdatedToast() {
        Toast.makeText(getApplicationContext(), getString(R.string.toast_channels_updated),
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.update_item: {
                LoadingDialogFragment loading =
                        (LoadingDialogFragment) getSupportFragmentManager()
                                .findFragmentByTag(LoadingDialogFragment.class.getSimpleName());
                if (loading == null) {
                    LoadingDialogFragment newLoading = new LoadingDialogFragment();
                    newLoading.show(mFragmentManager, LoadingDialogFragment.class.getSimpleName());
                    mUpdateRequestId = mServiceHelper.updateRSSData(mController.getChannelsRSSLinks());
                }
                return true;
            }
            case R.id.add_item: {
                showAddChannelDialog();
                return true;
            }
            case R.id.clear_data_item: {
                showClearDataDialog();
                return true;
            }
            default: {
                return super.onOptionsItemSelected(item);
            }
        }
    }

    public static class LoadingDialogFragment extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            ProgressDialog progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage(getString(R.string.loading) + "...");
            return progressDialog;
        }

        public void setProgress(int progress) {
            ((ProgressDialog) getDialog()).setMessage(
                    getString(R.string.loading) + " " + progress + "%");
        }

        @Override
        public void onCancel(DialogInterface dialog) {
            super.onCancel(dialog);
            ((MainActivity) getActivity()).cancelCommand();
        }
    }

    private void updateLoadingProgress(final int progress) {
        LoadingDialogFragment loading =
                (LoadingDialogFragment) getSupportFragmentManager()
                        .findFragmentByTag(LoadingDialogFragment.class.getSimpleName());
        if (loading != null) {
            loading.setProgress(progress);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mServiceHelper.addListener(this);

        if (mGetRequestId != -1 && mUpdateRequestId != -1 &&
                !mServiceHelper.isPending(mGetRequestId) &&
                !mServiceHelper.isPending(mUpdateRequestId)) {
            dismissLoadingDialog();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mServiceHelper.removeListener(this);
    }

    public void cancelCommand() {
        mServiceHelper.cancelRequest(mGetRequestId);
        mServiceHelper.cancelRequest(mUpdateRequestId);
    }

    private void dismissLoadingDialog() {
        LoadingDialogFragment loading =
                (LoadingDialogFragment) getSupportFragmentManager()
                        .findFragmentByTag(LoadingDialogFragment.class.getSimpleName());
        if (loading != null) {
            loading.dismiss();
        }
    }

    private void addNewChannel(String RSSLink) {
        if (mController.isChannelExists(RSSLink)) {
            showDuplicateChannelToast();
            return;
        }

        LoadingDialogFragment loading = new LoadingDialogFragment();
        loading.show(mFragmentManager, LoadingDialogFragment.class.getSimpleName());
        mGetRequestId = mServiceHelper.getRSSData(RSSLink);
    }

    private void showClearDataDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder
                .setMessage(R.string.message_clear_data)
                .setPositiveButton(getString(R.string.yes),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                mController.clearData();
                                dialog.dismiss();
                            }
                        })
                .setNegativeButton(getString(R.string.no),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }
                        });

        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();
    }

    private void showAddChannelDialog() {
        View dialogContent = getLayoutInflater().inflate(R.layout.dialog_add_channel, null);

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setView(dialogContent);

        final EditText RSSLinkEditText = (EditText) dialogContent.findViewById(R.id.rss_link_edit_text);
        dialogBuilder
            .setPositiveButton(getString(R.string.ok),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            addNewChannel(RSSLinkEditText.getText().toString());
                            dialog.dismiss();
                        }
                    })
            .setNegativeButton(getString(R.string.cancel),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                        }
                    });

        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();
    }

    private void processException(int exceptionCode) {
        switch (exceptionCode) {
            case Constants.CODE_URL_EXCEPTION: {
                break;
            }
            case Constants.CODE_IO_EXCEPTION: {
                Toast.makeText(getApplicationContext(), getString(R.string.toast_io_exception),
                        Toast.LENGTH_SHORT).show();
                break;
            }
            case Constants.CODE_ACCESS_EXCEPTION: {
                Toast.makeText(getApplicationContext(), getString(R.string.toast_access_denied),
                        Toast.LENGTH_SHORT).show();
                break;
            }
            case Constants.CODE_PARSE_EXCEPTION: {
                Toast.makeText(getApplicationContext(), getString(R.string.toast_parse_exception),
                        Toast.LENGTH_SHORT).show();
                break;
            }
            case Constants.CODE_UNKNOWN_EXCEPTION: {
                break;
            }
        }
    }

    @Override
    public void onServiceCallback(int requestId, Intent requestIntent,
                                  int resultCode, Bundle data) {
        if (mServiceHelper.checkCommandClass(requestIntent, GetRSSDataCommand.class)) {
            if (resultCode == GetRSSDataCommand.RESPONSE_SUCCESS) {
                mGetRequestId = -1;
                RSSChannel channel = data.getParcelable(Constants.KEY_RSS_CHANNEL);
                ArrayList<RSSItem> items = data.getParcelableArrayList(Constants.KEY_RSS_ITEMS);

                long channelId = mController.insertChannel(channel);
                mController.insertChannelItems(channelId, items);
                dismissLoadingDialog();
            } else if (resultCode == GetRSSDataCommand.RESPONSE_FAILURE) {
                mGetRequestId = -1;
                dismissLoadingDialog();
                int exceptionCode = data.getInt(Constants.KEY_EXCEPTION_CODE);
                processException(exceptionCode);
            }
        } else if (mServiceHelper.checkCommandClass(requestIntent, UpdateRSSDataCommand.class)) {
            if (resultCode == UpdateRSSDataCommand.RESPONSE_SUCCESS) {
                mUpdateRequestId = -1;
                RSSChannel channel = data.getParcelable(Constants.KEY_RSS_CHANNEL);
                ArrayList<RSSItem> items = data.getParcelableArrayList(Constants.KEY_RSS_ITEMS);
                mController.updateChannel(channel, items);

                dismissLoadingDialog();
                showChannelsUpdatedToast();
            } else if (resultCode == UpdateRSSDataCommand.RESPONSE_PROGRESS) {
                RSSChannel channel = data.getParcelable(Constants.KEY_RSS_CHANNEL);
                ArrayList<RSSItem> items = data.getParcelableArrayList(Constants.KEY_RSS_ITEMS);
                mController.updateChannel(channel, items);

                int progress = data.getInt(UpdateRSSDataCommand.EXTRA_PROGRESS);
                updateLoadingProgress(progress);
            } else if (resultCode == UpdateRSSDataCommand.RESPONSE_FAILURE) {
                mUpdateRequestId = -1;
                dismissLoadingDialog();
                int exceptionCode = data.getInt(Constants.KEY_EXCEPTION_CODE);
                processException(exceptionCode);
            }
        }
    }
}
