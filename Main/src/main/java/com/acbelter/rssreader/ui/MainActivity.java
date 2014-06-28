package com.acbelter.rssreader.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.view.ActionMode;
import android.util.Log;
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
import com.acbelter.rssreader.network.GetRSSDataCommand;
import com.acbelter.rssreader.network.SimpleNetworkServiceHelper;

import java.util.ArrayList;

public class MainActivity extends ActionBarActivity implements NetworkServiceCallbackListener,
        ControllerUICallback {
    private FragmentManager mFragmentManager;
    private ActionMode mActionMode;
    private boolean mIsActionMode;
    private ActionMode.Callback mActionModeCallback;

    private SimpleNetworkServiceHelper mServiceHelper;
    private int mRequestId = -1;

    private Controller mController;

    private ChannelsFragment mChannelsFragment;
    private ChannelItemsFragment mChannelItemsFragment;

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

        if (savedInstanceState == null) {
            initFragments();
            showChannelsFragment();
            mController.loadChannels();
        } else {
            mChannelsFragment = (ChannelsFragment) mFragmentManager.findFragmentByTag(
                    ChannelsFragment.class.getSimpleName());
            mChannelItemsFragment = (ChannelItemsFragment) mFragmentManager.findFragmentByTag(
                    ChannelItemsFragment.class.getSimpleName());
        }

        mActionModeCallback = new ActionMode.Callback() {
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                mode.getMenuInflater().inflate(R.menu.menu_context, menu);
                mIsActionMode = true;
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                if (item.getItemId() == R.id.delete_items) {
                    // TODO
                    //deleteSelectedItems();
                }
                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                // TODO
                //clearSelection();
                mIsActionMode = false;
                mActionMode = null;
            }
        };

    }

    private void initFragments() {
        mChannelsFragment = new ChannelsFragment();
        mChannelItemsFragment = new ChannelItemsFragment();
        FragmentTransaction ft = mFragmentManager.beginTransaction();
        //ItemFragment itemFragment = new ItemFragment();
        ft.add(R.id.content_frame, mChannelsFragment, ChannelsFragment.class.getSimpleName());
        ft.add(R.id.content_frame, mChannelItemsFragment, ChannelItemsFragment.class
                .getSimpleName());
        //ft.hide(mChannelItemsFragment);
        //ft.add(itemFragment, ItemFragment.class.getSimpleName());
        ft.commit();
    }

    public void showChannelItems(int channelId) {
        showChannelItemsFragment();
        mController.loadChannelItems(channelId);
    }


    private void finishActionMode() {
        if (mIsActionMode) {
            mActionMode.finish();
            mActionMode = null;
            mIsActionMode = false;
        }
    }

    public void showChannelsFragment() {
        FragmentTransaction ft = mFragmentManager.beginTransaction();
        //ft.hide(mItemFragment);
        ft.hide(mChannelItemsFragment);
        ft.show(mChannelsFragment);
        ft.commit();
    }

    @Override
    public void setChannelsFragmentCursor(Cursor c) {
        if (mChannelsFragment != null) {
            mChannelsFragment.getAdapter().swapCursor(c);
        }
    }

    public void showChannelItemsFragment() {
        FragmentTransaction ft = mFragmentManager.beginTransaction();
        //ft.hide(mItemFragment);
        ft.hide(mChannelsFragment);
        ft.show(mChannelItemsFragment);
        ft.addToBackStack(null);
        ft.commit();
    }

    @Override
    public void setChannelItemsFragmentCursor(Cursor c) {
        if (mChannelItemsFragment != null) {
            mChannelItemsFragment.getAdapter().swapCursor(c);
        }
    }

    public void showDuplicateChannelToast() {
        Toast.makeText(getApplicationContext(), getString(R.string.toast_duplicate_channel),
                Toast.LENGTH_LONG).show();
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
                // TODO Update
                return true;
            }
            case R.id.add_item: {
                showAddChannelDialog();
                return true;
            }
            case R.id.clear_data_item: {
                mController.clearData();
                return true;
            }
            default: {
                return super.onOptionsItemSelected(item);
            }
        }
    }

    public static class LoadingDialogFragment extends DialogFragment {
        private String mLoadingString;

        public static LoadingDialogFragment newInstance(String loadingString) {
            LoadingDialogFragment ldf = new LoadingDialogFragment();
            ldf.mLoadingString = loadingString;
            return ldf;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            ProgressDialog progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage(mLoadingString);
            return progressDialog;
        }

        @Override
        public void onCancel(DialogInterface dialog) {
            super.onCancel(dialog);
            ((MainActivity) getActivity()).cancelCommand();
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        mServiceHelper.addListener(this);

        if (mRequestId != -1 && !mServiceHelper.isPending(mRequestId)) {
            dismissLoadingDialog();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mServiceHelper.removeListener(this);
    }

    public SimpleNetworkServiceHelper getNetworkServiceHelper() {
        return mServiceHelper;
    }


    public void cancelCommand() {
        mServiceHelper.cancelRequest(mRequestId);
    }

    private void dismissLoadingDialog() {
        LoadingDialogFragment loading =
                (LoadingDialogFragment) getSupportFragmentManager()
                        .findFragmentByTag(LoadingDialogFragment.class.getSimpleName());
        if (loading != null) {
            loading.dismiss();
        }
    }

    private void addNewChannel(String link) {
        if (mController.isChannelExists(link)) {
            showDuplicateChannelToast();
            return;
        }

        LoadingDialogFragment loading = LoadingDialogFragment.newInstance(
                getString(R.string.loading_channel));
        loading.show(mFragmentManager, LoadingDialogFragment.class.getSimpleName());
        // FIXME Set mRequestId = -1
        mRequestId = mServiceHelper.getRSSData(link);
    }

    private void showAddChannelDialog() {
        View dialogContent = getLayoutInflater().inflate(R.layout.dialog_add_channel, null);

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setView(dialogContent);

        final EditText linkEditText = (EditText) dialogContent.findViewById(R.id.link_edit_text);
        dialogBuilder
            .setPositiveButton(getString(R.string.ok),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            boolean exists = mController.isChannelExists(linkEditText.getText()
                                    .toString());
                            Log.d("DEBUG", "CHANNEL EXISTS: " + exists);
                            addNewChannel(linkEditText.getText().toString());
                            dialog.dismiss();
                        }
                    })
            .setNegativeButton(getString(R.string.cancel),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });

        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();
    }

    @Override
    public void onServiceCallback(int requestId, Intent requestIntent,
                                  int resultCode, Bundle data) {
        if (mServiceHelper.checkCommandClass(requestIntent, GetRSSDataCommand.class)) {
            if (resultCode == GetRSSDataCommand.RESPONSE_SUCCESS) {
                dismissLoadingDialog();

                RSSChannel channel = data.getParcelable(Constants.KEY_RSS_CHANNEL);
                ArrayList<RSSItem> items = data.getParcelableArrayList(Constants.KEY_RSS_ITEMS);

                int channelId = mController.insertChannel(channel);
                mController.insertChannelItems(channelId, items);
            } else if (resultCode == GetRSSDataCommand.RESPONSE_PROGRESS) {
                // TODO For the future
            } else if (resultCode == GetRSSDataCommand.RESPONSE_FAILURE) {
                dismissLoadingDialog();
                Toast.makeText(getApplicationContext(), getString(R.string.no_rss_data),
                        Toast.LENGTH_LONG).show();
            }
        }
    }
}
