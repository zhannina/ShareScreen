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
import android.view.View;

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


    private  AccelerometerBR dataReceiver = new AccelerometerBR();

    private double accelValueX = 0;
    private double accelValueY = 0;
    private double accelValueZ = 0;
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

    }

    public void showPlotActivity(View view) {
        Intent intent = new Intent(this, PlottingActivity.class);
        startActivity(intent);
    }

    private class AccelerometerBR extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {


            if (intent.getAction().equals(Accelerometer.ACTION_AWARE_ACCELEROMETER)) {
                ContentValues cv = (ContentValues) intent.getExtras().get(Accelerometer.EXTRA_DATA);

                Log.d("CONTENTVALS", cv.toString());

                accelValueX = cv.getAsDouble("double_values_0");
                accelValueY = cv.getAsDouble("double_values_1");
                accelValueZ = cv.getAsDouble("double_values_2");

                if(PlottingActivity.instance != null){
                    double[] vals = {accelValueX, accelValueY, accelValueZ};
                    PlottingActivity.instance.sensorDisplay.addSensorValue(vals);
                }
                Log.d("XVAL", ""+accelValueX);
                Log.d("YVAL", ""+accelValueY);
                Log.d("ZVAL", ""+accelValueZ);
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
