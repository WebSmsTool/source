/*
 * Copyright 2012 software2012team23
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package at.tugraz.ist.akm.providers;

import java.util.HashMap;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import at.tugraz.ist.akm.content.DefaultPreferences;
import at.tugraz.ist.akm.preferences.PreferencesProvider;
import at.tugraz.ist.akm.trace.LogClient;

public class ApplicationContentProvider extends ContentProvider {

    private static final String AUTHORITY = ApplicationContentProvider.class.getCanonicalName();
    public static final String CONFIGURATION_TABLE_NAME = "ValuesTable";
    private static final String DATABASE_NAME = "ApplicationContentDB";
    
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY);
    public static final Uri PREFERENCES_URI = Uri.withAppendedPath(CONTENT_URI, CONFIGURATION_TABLE_NAME); 
    
    private static final UriMatcher URI_MATCHER;
    private static HashMap<String, String> mContentMap;
    private DataBaseHelper mDbHelper;
    private LogClient mLog = new LogClient(this);

    
    public ApplicationContentProvider()
    {
    	mLog.debug("constructing content provider api instance [" + getClass().getSimpleName() + "]");
    }
    
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        int count = 0;
        switch (URI_MATCHER.match(uri)) {
        case 1:
            try {
                count = db.delete(CONFIGURATION_TABLE_NAME, selection + "=?", selectionArgs);
                db.close();
            } catch (Exception ex) {
                mLog.error("failed to delete [" + CONFIGURATION_TABLE_NAME + "] " + ex.getMessage());
            }
            break;
        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public Uri insert(Uri uri, ContentValues initialValues) {
        if (URI_MATCHER.match(uri) != 1) {
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        ContentValues values;
        if (initialValues != null) {
            values = new ContentValues(initialValues);
        } else {
            values = new ContentValues();
        }

        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        long rowId = 0;

        rowId = db.insert(CONFIGURATION_TABLE_NAME, null, values);
        db.close();
        if (rowId > 0) {
            Uri noteUri = ContentUris.withAppendedId(PreferencesProvider.Content.CONTENT_URI, rowId);
            getContext().getContentResolver().notifyChange(noteUri, null);
            return noteUri;
        }

        throw new SQLException("Failed to insert row into " + uri);
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
            String sortOrder) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        switch (URI_MATCHER.match(uri)) {
        case 1:
            try {
                qb.setTables(CONFIGURATION_TABLE_NAME);
                qb.setProjectionMap(mContentMap);
            } catch (Exception ex) {

            }
            break;

        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor cursor = null;

        try {
            cursor = qb.query(db, projection, selection + " =? ", selectionArgs, null, null, sortOrder);

            cursor.setNotificationUri(getContext().getContentResolver(), uri);
        } catch (Exception ex) {
            mLog.info("Query" + ex.toString());
        }
        return cursor;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        int count = 0;
        switch (URI_MATCHER.match(uri)) {
        case 1:
            try {
                count = db
                        .update(CONFIGURATION_TABLE_NAME, values, selection + "=?", selectionArgs);
                db.close();
            } catch (Exception ex) {
                mLog.error("failed to update [" + CONFIGURATION_TABLE_NAME + "] " + ex.getMessage());
            }
            break;

        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public String getType(Uri uri) {
        switch (URI_MATCHER.match(uri)) {
        case 1:
            return PreferencesProvider.Content.CONTENT_TYPE;

        default:
            throw new IllegalArgumentException("unknown URI" + uri);
        }
    }

    @Override
    public boolean onCreate() {
        mLog.debug(getClass().getSimpleName() + ".onCreate()");
        if (mDbHelper == null) {
            mDbHelper = new DataBaseHelper(getContext());
        }
        return true;
    }

    static {
        URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
        URI_MATCHER.addURI(AUTHORITY, CONFIGURATION_TABLE_NAME, 1);

        mContentMap = new HashMap<String, String>();
        mContentMap.put(PreferencesProvider.Content._ID, PreferencesProvider.Content._ID);
        mContentMap.put(PreferencesProvider.Content.NAME, PreferencesProvider.Content.NAME);
        mContentMap.put(PreferencesProvider.Content.VALUE, PreferencesProvider.Content.VALUE);
    }

    private static class DataBaseHelper extends SQLiteOpenHelper {
        private LogClient mLog = new LogClient(this);

        DataBaseHelper(Context context) {
            super(context, DATABASE_NAME, null, 1);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            mLog.info("onCreate sqlitedatabase invoked");
            db.execSQL("CREATE TABLE " + CONFIGURATION_TABLE_NAME + " (" + PreferencesProvider.Content._ID
                    + " INTEGER PRIMARY KEY AUTOINCREMENT," + PreferencesProvider.Content.NAME
                    + " VARCHAR(255)," + PreferencesProvider.Content.VALUE + " VARCHAR(255)" + ");");
            DefaultPreferences.storeDefaultPreferences(db);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + CONFIGURATION_TABLE_NAME);
            onCreate(db);
        }
    }
}