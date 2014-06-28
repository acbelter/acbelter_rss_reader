package com.acbelter.rssreader.storage;

import android.content.*;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.text.TextUtils;

public class RSSContentProvider extends ContentProvider {
    static final String DB_NAME = "acbelter_rss_db";
    static final int DB_VERSION = 1;

    static final String TABLE_CHANNELS = "channels_table";
    static final String TABLE_ITEMS = "items_table";

    // Fields of channels table
    public static final String CHANNEL_ID = "_id";
    public static final String CHANNEL_TITLE = "title";
    public static final String CHANNEL_DESCRIPTION = "description";
    public static final String CHANNEL_LINK = "link";

    // Fields of items table
    public static final String ITEM_ID = "_id";
    public static final String ITEM_CHANNEL_ID = "channel_id";
    public static final String ITEM_TITLE = "title";
    public static final String ITEM_DESCRIPTION = "description";
    public static final String ITEM_LINK = "link";

    static final String CREATE_TABLE_CHANNELS = "create table " + TABLE_CHANNELS + "("
            + CHANNEL_ID + " integer primary key autoincrement, "
            + CHANNEL_TITLE + " text not null, "
            + CHANNEL_DESCRIPTION + " text not null, "
            + CHANNEL_LINK + " text not null);";

    static final String CREATE_TABLE_ITEMS = "create table " + TABLE_ITEMS + "("
            + ITEM_ID + " integer primary key autoincrement, "
            + ITEM_CHANNEL_ID + " integer not null, "
            + ITEM_TITLE + " text not null, "
            + ITEM_DESCRIPTION + " text not null, "
            + ITEM_LINK + " text not null, "
            + "foreign key(" + ITEM_CHANNEL_ID + ") "
            + "references " + TABLE_CHANNELS + "(" + CHANNEL_ID + "));";

    static final String DROP_TABLE_CHANNELS = "drop table if exists " + TABLE_CHANNELS;
    static final String DROP_TABLE_ITEMS = "drop table if exists " + TABLE_ITEMS;

    static final String AUTHORITY = "com.acbelter.rssreader.RSSData";
    static final String PATH_CHANNELS = "channels";
    static final String PATH_ITEMS = "items";

    public static final Uri URI_CHANNELS = Uri.parse("content://"
            + AUTHORITY + "/" + PATH_CHANNELS);
    public static final Uri URI_ITEMS = Uri.parse("content://"
            + AUTHORITY + "/" + PATH_ITEMS);

    // MIME types
    static final String TYPE_CHANNELS = "vnd.android.cursor.dir/vnd."
            + AUTHORITY + "." + PATH_CHANNELS;
    static final String TYPE_ONE_CHANNEL = "vnd.android.cursor.item/vnd."
            + AUTHORITY + "." + PATH_CHANNELS;
    static final String TYPE_ITEMS = "vnd.android.cursor.dir/vnd."
            + AUTHORITY + "." + PATH_ITEMS;
    static final String TYPE_ONE_ITEM = "vnd.android.cursor.item/vnd."
            + AUTHORITY + "." + PATH_ITEMS;

    // Codes that UriMatcher returns
    static final int CODE_CHANNELS = 1;
    static final int CODE_CHANNEL_ID = 2;
    static final int CODE_ITEMS = 3;
    static final int CODE_ITEM_ID = 4;

    private static final UriMatcher URI_MATCHER;
    static {
        URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
        URI_MATCHER.addURI(AUTHORITY, PATH_CHANNELS, CODE_CHANNELS);
        URI_MATCHER.addURI(AUTHORITY, PATH_CHANNELS + "/#", CODE_CHANNEL_ID);
        URI_MATCHER.addURI(AUTHORITY, PATH_ITEMS, CODE_ITEMS);
        URI_MATCHER.addURI(AUTHORITY, PATH_ITEMS + "/#", CODE_ITEM_ID);
    }

    RSSDatabaseHelper mDatabaseHelper;
    SQLiteDatabase mDatabase;

    @Override
    public boolean onCreate() {
        mDatabaseHelper = new RSSDatabaseHelper(getContext());
        return true;
    }

    @Override
    public String getType(Uri uri) {
        switch (URI_MATCHER.match(uri)) {
            case CODE_CHANNELS: {
                return TYPE_CHANNELS;
            }
            case CODE_CHANNEL_ID: {
                return TYPE_ONE_CHANNEL;
            }
            case CODE_ITEMS: {
                return TYPE_ITEMS;
            }
            case CODE_ITEM_ID: {
                return TYPE_ONE_ITEM;
            }
            default: {
                return null;
            }
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        final int code = URI_MATCHER.match(uri);
        switch (code) {
            case CODE_CHANNELS: {
                break;
            }
            case CODE_CHANNEL_ID: {
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    selection = CHANNEL_ID + " = " + id;
                } else {
                    selection = selection + " AND " + CHANNEL_ID + " = " + id;
                }
                break;
            }
            case CODE_ITEMS: {
                if (TextUtils.isEmpty(sortOrder)) {
                    sortOrder = ITEM_ID + " DESC";
                }
                break;
            }
            case CODE_ITEM_ID: {
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    selection = ITEM_ID + " = " + id;
                } else {
                    selection = selection + " AND " + ITEM_ID + " = " + id;
                }
                break;
            }
            default: {
                throw new IllegalArgumentException("Invalid URI: " + uri);
            }
        }

        mDatabase = mDatabaseHelper.getReadableDatabase();
        Cursor c = null;
        if (code == CODE_CHANNELS || code == CODE_CHANNEL_ID) {
            c = mDatabase.query(TABLE_CHANNELS, projection, selection,
                    selectionArgs, null, null, sortOrder);
            c.setNotificationUri(getContext().getContentResolver(), URI_CHANNELS);
        } else if (code == CODE_ITEMS || code == CODE_ITEM_ID) {
            c = mDatabase.query(TABLE_ITEMS, projection, selection,
                    selectionArgs, null, null, sortOrder);
            c.setNotificationUri(getContext().getContentResolver(), URI_ITEMS);
        }

        return c;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final int code = URI_MATCHER.match(uri);
        if (code != CODE_CHANNELS && code != CODE_ITEMS) {
            throw new IllegalArgumentException("Invalid URI: " + uri);
        }

        mDatabase = mDatabaseHelper.getWritableDatabase();
        Uri resultUri = null;
        if (code == CODE_CHANNELS) {
            long rowId = mDatabase.insert(TABLE_CHANNELS, null, values);
            resultUri = ContentUris.withAppendedId(URI_CHANNELS, rowId);
            getContext().getContentResolver().notifyChange(resultUri, null);
        } else if (code == CODE_ITEMS) {
            long rowId = mDatabase.insert(TABLE_ITEMS, null, values);
            resultUri = ContentUris.withAppendedId(URI_ITEMS, rowId);
            getContext().getContentResolver().notifyChange(resultUri, null);
        }

        return resultUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final int code = URI_MATCHER.match(uri);
        switch (code) {
            case CODE_CHANNELS: {
                break;
            }
            case CODE_CHANNEL_ID: {
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    selection = CHANNEL_ID + " = " + id;
                } else {
                    selection = selection + " AND " + CHANNEL_ID + " = " + id;
                }
                break;
            }
            case CODE_ITEMS: {
                break;
            }
            case CODE_ITEM_ID: {
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    selection = ITEM_ID + " = " + id;
                } else {
                    selection = selection + " AND " + ITEM_ID + " = " + id;
                }
                break;
            }
            default: {
                throw new IllegalArgumentException("Invalid URI: " + uri);
            }
        }

        mDatabase = mDatabaseHelper.getWritableDatabase();
        int count = 0;
        if (code == CODE_CHANNELS || code == CODE_CHANNEL_ID) {
            count = mDatabase.delete(TABLE_CHANNELS, selection, selectionArgs);
            getContext().getContentResolver().notifyChange(uri, null);
        } else if (code == CODE_ITEMS || code == CODE_ITEM_ID) {
            count = mDatabase.delete(TABLE_ITEMS, selection, selectionArgs);
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final int code = URI_MATCHER.match(uri);
        switch (code) {
            case CODE_CHANNELS: {
                break;
            }
            case CODE_CHANNEL_ID: {
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    selection = CHANNEL_ID + " = " + id;
                } else {
                    selection = selection + " AND " + CHANNEL_ID + " = " + id;
                }
                break;
            }
            case CODE_ITEMS: {
                break;
            }
            case CODE_ITEM_ID: {
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    selection = ITEM_ID + " = " + id;
                } else {
                    selection = selection + " AND " + ITEM_ID + " = " + id;
                }
                break;
            }
            default: {
                throw new IllegalArgumentException("Invalid URI: " + uri);
            }
        }

        mDatabase = mDatabaseHelper.getWritableDatabase();
        int count = 0;
        if (code == CODE_CHANNELS || code == CODE_CHANNEL_ID) {
            count = mDatabase.update(TABLE_CHANNELS, values, selection, selectionArgs);
            getContext().getContentResolver().notifyChange(uri, null);
        } else if (code == CODE_ITEMS || code == CODE_ITEM_ID) {
            count = mDatabase.update(TABLE_ITEMS, values, selection, selectionArgs);
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return count;
    }

    private class RSSDatabaseHelper extends SQLiteOpenHelper {
        public RSSDatabaseHelper(Context context) {
            super(context, DB_NAME, null, DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(CREATE_TABLE_CHANNELS);
            db.execSQL(CREATE_TABLE_ITEMS);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL(DROP_TABLE_CHANNELS);
            db.execSQL(DROP_TABLE_ITEMS);
            onCreate(db);
        }
    }
}
