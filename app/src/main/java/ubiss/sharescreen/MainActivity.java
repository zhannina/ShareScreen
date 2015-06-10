package ubiss.sharescreen;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.aware.Accelerometer;
import com.aware.Aware;
import com.aware.Aware_Preferences;
import com.aware.Magnetometer;
import com.aware.providers.Accelerometer_Provider;
import com.aware.utils.Aware_Plugin;
import com.aware.utils.Aware_Sensor;

import java.security.Provider;


public class MainActivity extends ActionBarActivity {
    private static Aware_Plugin.ContextProducer CONTEXT_PRODUCER = null;

    // Test comment Daniel
    // Test comment Zhanna

    private  AccelerometerBR dataReceiver = new AccelerometerBR();

    private double accelValueX = 0;
    private double accelValueY = 0;
    private double accelValueZ= 0;
    private double accelValue = 0;

    public static final String ACTION_AWARE_PLUGIN_SARSENBAYEVA = "ACTION_AWARE_UBISS";
    public static final String EXTRA_AVG_SARSENBAYEVA = "avg_plugin";
    public static final String EXTRA_OVER_THRESHOLD = "over_threshold";
    private static boolean over_threshold;
    private static int avg = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Aware.setSetting(getApplicationContext(), Aware_Preferences.STATUS_ACCELEROMETER, true); // to activate aware
        Intent refresh = new Intent(Aware.ACTION_AWARE_REFRESH);
        sendBroadcast(refresh);

        IntentFilter filter = new IntentFilter();
        filter.addAction(Accelerometer.ACTION_AWARE_ACCELEROMETER);


        registerReceiver(dataReceiver, filter);

        //Any active plugin/sensor shares its overall context using broadcasts
        CONTEXT_PRODUCER = new Aware_Plugin.ContextProducer(){
            @Override
            public void onContext() {
                Intent context_data = new Intent();
                context_data.setAction(ACTION_AWARE_PLUGIN_SARSENBAYEVA);
                context_data.putExtra(EXTRA_AVG_SARSENBAYEVA, avg);
                context_data.putExtra(EXTRA_OVER_THRESHOLD, over_threshold);
                sendBroadcast(context_data);
            }
        };

    }

    private class AccelerometerBR extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Cursor accelerometerCursor = context.getContentResolver().query(Accelerometer_Provider.Accelerometer_Data.CONTENT_URI,
                    null, null, null, Accelerometer_Provider.Accelerometer_Data.TIMESTAMP + " DESC LIMIT 1");

            if (intent.getAction().equals(Magnetometer.ACTION_AWARE_MAGNETOMETER)) {
                if (accelerometerCursor != null && accelerometerCursor.moveToFirst()) {
                    accelValueX = accelerometerCursor.getDouble(accelerometerCursor.getColumnIndex(Accelerometer_Provider.Accelerometer_Data.VALUES_0));
                    accelValueY = accelerometerCursor.getDouble(accelerometerCursor.getColumnIndex(Accelerometer_Provider.Accelerometer_Data.VALUES_1));
                    accelValueZ = accelerometerCursor.getDouble(accelerometerCursor.getColumnIndex(Accelerometer_Provider.Accelerometer_Data.VALUES_2));

                    // calculate the magnetometer value from 3 readings
                    accelValue = Math.sqrt(accelValueX * accelValueX +
                            accelValueY * accelValueY +
                            accelValueZ * accelValueZ);

                    accelValue = Math.round(accelValue * 100.00) / 100.0;
                    Log.d("accelerometer value", ""+accelValueX);
                    Log.d("accelerometer value", ""+accelValueY);
                    Log.d("accelerometer value", ""+accelValueZ);

                    ContentValues data = new ContentValues();
                    data.put(ubiss.sharescreen.Provider.ProviderData.TIMESTAMP, System.currentTimeMillis());
                    data.put(ubiss.sharescreen.Provider.ProviderData.DEVICE_ID, Aware_Preferences.DEVICE_ID);
                    data.put(ubiss.sharescreen.Provider.ProviderData.ACCEL_VALUE_X, accelValueX);
                    data.put(ubiss.sharescreen.Provider.ProviderData.ACCEL_VALUE_Y, accelValueY);
                    data.put(ubiss.sharescreen.Provider.ProviderData.ACCEL_VALUE_Z, accelValueZ);
                    getContentResolver().insert(ubiss.sharescreen.Provider.ProviderData.CONTENT_URI, data);
                    CONTEXT_PRODUCER.onContext();


                    if( accelerometerCursor != null && !accelerometerCursor.isClosed() ) {
                        accelerometerCursor.close();
                    }
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("DESTROY", "Template plugin terminated");
        Aware.setSetting(this, Aware_Preferences.STATUS_ACCELEROMETER, false);

        //Deactivate any sensors/plugins you activated here
        //...

        //Ask AWARE to apply your settings
        sendBroadcast(new Intent(Aware.ACTION_AWARE_REFRESH));
    }
}
