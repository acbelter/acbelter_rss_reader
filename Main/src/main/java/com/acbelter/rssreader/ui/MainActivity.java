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
    private ItemFragment mItemFragment;

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

        if (savedInstanceState == null) {
            mChannelsFragment = new ChannelsFragment();
            mChannelItemsFragment = new ChannelItemsFragment();
            mItemFragment = new ItemFragment();
            mController.loadChannels();
            showChannelsFragment();
        } else {
            mChannelsFragment = (ChannelsFragment) mFragmentManager
                    .findFragmentByTag(TAG_CF);
            if (mChannelsFragment == null) {
                mChannelsFragment = new ChannelsFragment();
            }
            mChannelItemsFragment = (ChannelItemsFragment) mFragmentManager
                    .findFragmentByTag(TAG_CIF);
            if (mChannelItemsFragment == null) {
                mChannelItemsFragment = new ChannelItemsFragment();
            }
            mItemFragment = (ItemFragment) mFragmentManager.findFragmentByTag(TAG_IF);
            if (mItemFragment == null) {
                mItemFragment = new ItemFragment();
            }
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

    public void showChannelItems(int channelId) {
        mController.loadChannelItems(channelId);
        showChannelItemsFragment();
    }

    public void showItem(RSSItem item) {
        mItemFragment.setItem(item);
        showItemFragment();
    }

    private void finishActionMode() {
        if (mIsActionMode) {
            mActionMode.finish();
            mActionMode = null;
            mIsActionMode = false;
        }
    }

    private void showChannelsFragment() {
        FragmentTransaction ft = mFragmentManager.beginTransaction();
        ft.replace(R.id.content_frame, mChannelsFragment, TAG_CF);
        ft.commit();
    }

    private void showChannelItemsFragment() {
        FragmentTransaction ft = mFragmentManager.beginTransaction();
        ft.replace(R.id.content_frame, mChannelItemsFragment, TAG_CIF);
        ft.addToBackStack(null);
        ft.commit();
    }

    private void showItemFragment() {
        FragmentTransaction ft = mFragmentManager.beginTransaction();
        ft.replace(R.id.content_frame, mItemFragment, TAG_IF);
        ft.addToBackStack(null);
        ft.commit();
    }

    @Override
    public void setChannelsFragmentCursor(Cursor c) {
        if (mChannelsFragment != null) {
            mChannelsFragment.getAdapter().swapCursor(c);
        }
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
                showClearDataDialog();
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

        final EditText linkEditText = (EditText) dialogContent.findViewById(R.id.link_edit_text);
        dialogBuilder
            .setPositiveButton(getString(R.string.ok),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            addNewChannel(linkEditText.getText().toString());
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
