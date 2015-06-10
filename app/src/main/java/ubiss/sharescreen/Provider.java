package ubiss.sharescreen;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.os.Environment;
import android.provider.BaseColumns;
import android.util.Log;

import com.aware.Aware;
import com.aware.utils.DatabaseHelper;

import java.util.HashMap;

/**
 * Created by tatoshka87 on 19/03/2015.
 */
public class Provider extends ContentProvider {

    public static final int DATABASE_VERSION = 1;
    public static String AUTHORITY = "ubiss.sharescreen.provider";
    public static final String DATABASE_NAME = Environment.getExternalStorageDirectory() + "/UBISS/database.db";

    private static final int UBISS_PROVIDER = 1;
    private static final int UBISS_PROVIDER_ID = 2;

    public static final String[] DATABASE_TABLES = {
            "ubiss_provider",
    };

    public static final String[] TABLES_FIELDS = {
            ProviderData._ID + " integer primary key autoincrement," +
                    ProviderData.TIMESTAMP + " real default 0," +
                    ProviderData.DEVICE_ID + " text default ''," +
                    ProviderData.ACCEL_VALUE_X + " real default 0," +
                    ProviderData.ACCEL_VALUE_Y + " real default 0," +
                    ProviderData.ACCEL_VALUE_Z + " real default 0," +
                    "UNIQUE("+ProviderData.TIMESTAMP+","+ProviderData.DEVICE_ID+")"


    };

    public static final class ProviderData implements BaseColumns {
        private ProviderData(){};

        public static final Uri CONTENT_URI = Uri.parse("content://"+AUTHORITY+"/ubiss_provider");
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.aware.ubiss_provider";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.aware.ubiss_provider";

        public static final String _ID = "_id";
        public static final String TIMESTAMP = "timestamp";
        public static final String DEVICE_ID = "device_id";
        public static final String ACCEL_VALUE_X = "accel_value_x";
        public static final String ACCEL_VALUE_Y = "accel_value_y";
        public static final String ACCEL_VALUE_Z = "accel_value_z";
    }

    private static UriMatcher URIMatcher;
    private static HashMap<String, String> databaseMap;
    private static DatabaseHelper databaseHelper;
    private static SQLiteDatabase database;

    @Override
    public boolean onCreate() {

        //AUTHORITY = getContext().getPackageName() + ".provider.plugin";

        URIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        URIMatcher.addURI(AUTHORITY, DATABASE_TABLES[0], UBISS_PROVIDER);
        URIMatcher.addURI(AUTHORITY, DATABASE_TABLES[0]+"/#", UBISS_PROVIDER_ID);

        databaseMap = new HashMap<String, String>();
        databaseMap.put(ProviderData._ID, ProviderData._ID);
        databaseMap.put(ProviderData.TIMESTAMP, ProviderData.TIMESTAMP);
        databaseMap.put(ProviderData.DEVICE_ID, ProviderData.DEVICE_ID);
        databaseMap.put(ProviderData.ACCEL_VALUE_X, ProviderData.ACCEL_VALUE_X);
        databaseMap.put(ProviderData.ACCEL_VALUE_Y, ProviderData.ACCEL_VALUE_Y);
        databaseMap.put(ProviderData.ACCEL_VALUE_Z, ProviderData.ACCEL_VALUE_Z);

        return true;
    }

    private boolean initialiseDB(){
        if (databaseHelper == null) {
            databaseHelper = new DatabaseHelper( getContext(), DATABASE_NAME, null, DATABASE_VERSION, DATABASE_TABLES, TABLES_FIELDS );
        }
        if( databaseHelper != null && ( database == null || ! database.isOpen() )) {
            database = databaseHelper.getWritableDatabase();
        }
        return( database != null && databaseHelper != null);
    }

    @Override
    public Cursor query(Uri uri, String[] strings, String s, String[] strings2, String s2) {
        if( ! initialiseDB() ) {
            Log.w(AUTHORITY,"Database unavailable...");
            return null;
        }

        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        switch (URIMatcher.match(uri)) {
            case UBISS_PROVIDER:
                qb.setTables(DATABASE_TABLES[0]);
                qb.setProjectionMap(databaseMap);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        try {
            Cursor c = qb.query(database, strings, s, strings2,
                    null, null, s2);
            c.setNotificationUri(getContext().getContentResolver(), uri);
            return c;
        } catch (IllegalStateException e) {
            if (Aware.DEBUG)
                Log.e(Aware.TAG, e.getMessage());

            return null;
        }
    }

    @Override
    public String getType(Uri uri) {

        switch (URIMatcher.match(uri)) {
            case UBISS_PROVIDER:
                return ProviderData.CONTENT_TYPE;
            case UBISS_PROVIDER_ID:
                return ProviderData.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {

        if( ! initialiseDB() ) {
            Log.w(AUTHORITY,"Database unavailable...");
            return null;
        }

        ContentValues values = (contentValues != null) ? new ContentValues(
                contentValues) : new ContentValues();

        switch (URIMatcher.match(uri)) {
            case UBISS_PROVIDER:
                long column_id = database.insert(DATABASE_TABLES[0], ProviderData.DEVICE_ID, values);

                if (column_id > 0) {
                    Uri new_uri = ContentUris.withAppendedId(
                            ProviderData.CONTENT_URI,
                            column_id);
                    getContext().getContentResolver().notifyChange(new_uri,
                            null);
                    return new_uri;
                }
                throw new SQLException("Failed to insert row into " + uri);
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @Override
    public int delete(Uri uri, String s, String[] strings) {
        if( ! initialiseDB() ) {
            Log.w(AUTHORITY, "Database unavailable...");
            return 0;
        }

        int count = 0;
        switch (URIMatcher.match(uri)) {
            case UBISS_PROVIDER:
                count = database.delete(DATABASE_TABLES[0], s, strings);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String s, String[] strings) {
        if( ! initialiseDB() ) {
            Log.w(AUTHORITY,"Database unavailable...");
            return 0;
        }

        int count = 0;
        switch (URIMatcher.match(uri)) {
            case UBISS_PROVIDER:
                count = database.update(DATABASE_TABLES[0], contentValues, s,
                        strings);
                break;
            default:

                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }
}
