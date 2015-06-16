package ubiss.sharescreen;


import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import com.aware.Aware;
import com.aware.Aware_Preferences;
import com.aware.utils.Aware_Plugin;


import ubiss.sharescreen.processing.Smoothing;

public class MainActivity extends ActionBarActivity implements SensorEventListener {
    private static Aware_Plugin.ContextProducer CONTEXT_PRODUCER = null;


    private Smoothing smoothing = new Smoothing(25);

    private double accelValueX = 0;
    private double accelValueY = 0;
    private double accelValueZ = 0;

    private double accelValue = 0;

    private SensorManager senSensorManager;
    private Sensor senAccelerometer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        senSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        senAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        senSensorManager.registerListener(this, senAccelerometer , SensorManager.SENSOR_DELAY_GAME);


    }
    public void showSoundDetectionActivity(View view) {
        Intent intent = new Intent(this, SoundDetectionActivity.class);
        startActivity(intent);
    }

    public void showPlotActivity(View view) {
        Intent intent = new Intent(this, PlottingActivity.class);
        startActivity(intent);
    }

    public void showRecordActivity(View view) {
        Intent intent = new Intent(this, RecordingActivity.class);
        startActivity(intent);
    }

    public void showVibrationActivity(View view) {
        Intent intent = new Intent(this, VibrationActivity.class);
        startActivity(intent);
    }

    public void showDemoActivity(View view) {
        Intent intent = new Intent(this, DemoActivity.class);
        startActivity(intent);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

        Sensor mySensor = sensorEvent.sensor;

        if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            accelValueX = sensorEvent.values[0];
            accelValueY = sensorEvent.values[1];
            accelValueZ = sensorEvent.values[2];

            double[] vals = {accelValueX, accelValueY, accelValueZ};
            vals = smoothing.smooth(vals);

            if(PlottingActivity.instance != null){
                PlottingActivity.instance.sensorDisplay.addSensorValue(vals);
            }
            if(RecordingActivity.instance != null){
                RecordingActivity.instance.addSensorValue(vals);
            }

            if(DemoActivity.instance != null){
                DemoActivity.instance.update(vals);
            }
            Log.d("XVALNEW", ""+accelValueX);
            Log.d("YVALNEW", ""+accelValueY);
            Log.d("ZVALNEW", ""+accelValueZ);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("DESTROY", "Template plugin terminated");
        Aware.setSetting(this, Aware_Preferences.STATUS_ACCELEROMETER, false);
        Aware.setSetting(this, Aware_Preferences.STATUS_MAGNETOMETER, false);
        Aware.setSetting(this, Aware_Preferences.STATUS_GYROSCOPE, false);

        sendBroadcast(new Intent(Aware.ACTION_AWARE_REFRESH));
        senSensorManager.unregisterListener(this);
    }
}
