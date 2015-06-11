package ubiss.sharescreen.db;

/**
 * Created by daniel on 03.12.2014.
 */

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.media.MediaScannerConnection;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.List;


public class DBHandler extends SQLiteOpenHelper {

    // DB constants:
    private static final String DATABASE_NAME = "ubiss_db";
    private static final int DATABASE_VERSION = 8;

    // DB structure constants:

    // Table taps:
    private static final String TABLE_SENSOR_DATA = "sensor_data";
    private static final String SD_COL_ID = "id";
    private static final String SD_COL_ACC_X = "accX";
    private static final String SD_COL_ACC_Y = "accY";
    private static final String SD_COL_ACC_Z = "accZ";
    private static final String SD_COL_FFT = "fft";
    /*private static final String SD_COL_ROT_X = "rotX";
    private static final String SD_COL_ROT_Y = "rotY";
    private static final String SD_COL_ROT_Z = "rotZ";
    private static final String SD_COL_ROTRATE_X = "rotrateX";
    private static final String SD_COL_ROTRATE_Y = "rotrateY";
    private static final String SD_COL_ROTRATE_Z = "rotrateZ";*/
    private static final String SD_COL_TIME = "timestamp";
    private static final String SD_COL_SEQ_ID = "seq_id";

    private static final String TABLE_SEQUENCES = "sequences";
    private static final String SEQ_COL_ID = "id";
    private static final String SEQ_COL_TIME = "timestamp";
    private static final String SEQ_COL_LABEL = "label";

    private Context context;

    /**
     * Singleton-Pattern instance.
     */
    private static DBHandler instance;

    /**
     * Returns the singleton-instance (and creates it with the given context, if
     * it is necessary).
     *
     * @param context
     * @return
     */
    public static DBHandler getInstance(Context context) {

        if (instance == null)
            instance = new DBHandler(context.getApplicationContext());

        return instance;
    }

    /**
     * Returns the singleton-instance, or null if it doesn't exist.
     *
     * @return
     */
    public static DBHandler getInstanceIfExists() {

        return instance;
    }

    /**
     * Private Constructor.
     *
     * @param context
     */
    private DBHandler(Context context) {

        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SENSOR_DATA);
        String createSensorDataTableString = "CREATE TABLE " + TABLE_SENSOR_DATA + "("
                + SD_COL_ID + " INTEGER PRIMARY KEY," + SD_COL_ACC_X
                + " REAL," + SD_COL_ACC_Y + " REAL,"
                + SD_COL_ACC_Z + " REAL, " + SD_COL_FFT + " TEXT, " + SD_COL_SEQ_ID + " INTEGER)";
        db.execSQL(createSensorDataTableString);

        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SEQUENCES);
        String createSequenceTableString = "CREATE TABLE " + TABLE_SEQUENCES + "("
                + SEQ_COL_ID + " INTEGER PRIMARY KEY," + SEQ_COL_LABEL
                + " TEXT," + SEQ_COL_TIME
                + " TIMESTAMP NOT NULL DEFAULT current_timestamp)";
        db.execSQL(createSequenceTableString);


    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SENSOR_DATA);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SEQUENCES);
        onCreate(db);
    }




    public int insertSequence(String label) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(SEQ_COL_LABEL, label);

        int id = (int) db.insert(TABLE_SEQUENCES, null, values);
        Log.d("DATABASE", "inserted sequence with id " + id + " and label " + label);

        db.close();
        return id;
    }

/*


    public void insertSensorData(float[] data, int seqID) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(SD_COL_ACC_X, data[0]);
        values.put(SD_COL_ACC_Y, data[1]);
        values.put(SD_COL_ACC_Z, data[2]);
        values.put(SD_COL_ROT_X, data[3]);
        values.put(SD_COL_ROT_Y, data[4]);
        values.put(SD_COL_ROT_Z, data[5]);
        values.put(SD_COL_ROTRATE_X, data[6]);
        values.put(SD_COL_ROTRATE_Y, data[7]);
        values.put(SD_COL_ROTRATE_Z, data[8]);
        values.put(SD_COL_SEQ_ID, seqID);

        int id = (int) db.insert(TABLE_SENSOR_DATA, null, values);
        Log.d("DATABASE", "inserted data with id: " + id);

        db.close();
    }

*/
    public void insertSensorData(List<double[]> dataList, int seqID, List<String> fftStrings) {

        SQLiteDatabase db = this.getWritableDatabase();

        String sql = "INSERT INTO " + TABLE_SENSOR_DATA + " ("+SD_COL_ACC_X+","+SD_COL_ACC_Y+","
                +SD_COL_ACC_Z + "," + SD_COL_FFT + ", " + SD_COL_SEQ_ID + ") VALUES (?,?,?,?,?)";
        SQLiteStatement statement = db.compileStatement(sql);
        db.beginTransaction();
        double[] vals;
        for (int i = 0; i < dataList.size(); i++) {
            vals = dataList.get(i);
            statement.clearBindings();
            statement.bindDouble(1, vals[0]);
            statement.bindDouble(2, vals[1]);
            statement.bindDouble(3, vals[2]);
            statement.bindString(4, fftStrings.get(i));
            statement.bindLong(5, seqID);
            statement.execute();
        }
        db.setTransactionSuccessful();
        db.endTransaction();

        Log.d("DATABASE", "inserted " + dataList.size() + " data entries");
        db.close();
    }

    /**
     * Export database to external storage, so it can be accessed from / copied
     * to a computer.
     *
     * @return
     */
    public boolean exportDB() {

        File sd = Environment.getExternalStorageDirectory();

        File data = Environment.getDataDirectory();
        FileChannel source = null;
        FileChannel destination = null;
        String currentDBPath = "/data/" + "ubiss.sharescreen" + "/databases/"
                + DATABASE_NAME;
        String backupDBPath = DATABASE_NAME;
        File currentDB = new File(data, currentDBPath);
        File backupDB = new File(sd, backupDBPath);

        Log.d("RESTORE", backupDB.toString());
        try {
            source = new FileInputStream(currentDB).getChannel();
            destination = new FileOutputStream(backupDB).getChannel();
            destination.transferFrom(source, 0, source.size());
            source.close();
            destination.close();
            MediaScannerConnection.scanFile(this.context, new String[]{backupDB.getAbsolutePath()}, null, null);
            return true;
        } catch (IOException e) {
            e.printStackTrace();

        }
        return false;
    }

}
