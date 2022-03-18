package io.agritrack.content.providers;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.agritrack.data.dao.wh.AssetDAO;
import io.agritrack.data.db.MobileDB;

public class AqmIdByCageEPCProvider extends ContentProvider {

    // defining authority so that other application can access it
    static final String PROVIDER_NAME = "io.agritrack.assets";
    // defining content URI
    static final String URL = "content://" + PROVIDER_NAME + "/asset";
    // parsing the content URI
    static final Uri CONTENT_URI = Uri.parse(URL);
    static final String id = "id";
    static final String name = "name";
    static final int uriCode = 1;
    static final UriMatcher uriMatcher;
    // declaring name of the database
    static final String DATABASE_NAME = "AGRIFISH_local.db";

    // declaring table name of the database
    static final String TABLE_NAME = "Users";

    // declaring version of the database
    static final int DATABASE_VERSION = 15;

    static {

        // to match the content URI
        // every time user access table under content provider
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        // to access whole table
        uriMatcher.addURI(PROVIDER_NAME, "asset", uriCode);

        // to access a particular row
        // of the table
        uriMatcher.addURI(PROVIDER_NAME, "asset/*", uriCode);
    }
    // creating object of database
    // to perform query
    private SQLiteDatabase db;

    //private MobileDB db;

    public AqmIdByCageEPCProvider() {
    }

    @Override
    public String getType(Uri uri) {
        switch (uriMatcher.match(uri)) {
            case uriCode:
                return "vnd.android.cursor.dir/asset";
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

    @Override
    public boolean onCreate() {
        /*Context context = getContext();
        DatabaseHelper dbHelper = new DatabaseHelper(context);
        db = dbHelper.getWritableDatabase();
        if (db != null) {
            return true;
        }
        return false;*/
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        /*String epc = uri.getPathSegments().get(1);
        Cursor c = db.assetDAO().getAssetCursorByEpc(epc);
        c.setNotificationUri(getContext().getContentResolver(), uri);*//*
        return c;*/
        final Context context = getContext();
        if (context == null) {
            return null;
        }
        AssetDAO assetDao = MobileDB.getInstance(context).assetDAO();

        final Cursor c = assetDao.getAssetCursorByEpc(uri.getPathSegments().get(1));
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;

        /*SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables("asset");
        qb.appendWhere("rfid=" + uri.getPathSegments().get(0));

        Cursor c = qb.query(db, projection, selection, selectionArgs, null,
                null, sortOrder);
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;*/
    }

    // creating a database
    private static class DatabaseHelper extends SQLiteOpenHelper {

        // defining a constructor
        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        // creating a table in the database
        @Override
        public void onCreate(SQLiteDatabase db) {

        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }
    }
}





