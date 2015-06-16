package ubiss.sharescreen;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.aware.Accelerometer;
import com.aware.providers.Accelerometer_Provider;

import java.util.ArrayList;
import java.util.List;

import ubiss.sharescreen.db.DBHandler;
import ubiss.sharescreen.gui.SensorReadingsDisplay;
import ubiss.sharescreen.processing.FFT;


public class RecordingActivity extends ActionBarActivity {


    public static RecordingActivity instance;

    private DBHandler dbHandler;

    private List<double[]> buffer = new ArrayList<double[]>();

    private List<String> fftStrings = new ArrayList<String>();

    private  boolean recording = false;

    private String label = "label";

    private int current_seq_id = -1;


    protected FFT fft;
    protected List<double[]> lastFFTResult = new ArrayList<double[]>();
    protected List<double[]> lastSensorValues;
    protected int history_size = 128;

    //public SensorReadingsDisplay sensorDisplay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recording);

        this.dbHandler = DBHandler.getInstance(getApplicationContext());

        RecordingActivity.instance = this;

        this.lastSensorValues = new ArrayList<double[]>();
        this.fft = new FFT(this.history_size);

        /*this.sensorDisplay = new SensorReadingsDisplay(this);
        LinearLayout container = (LinearLayout) findViewById(R.id.container_sensor_readings_display);
        container.addView(sensorDisplay);*/

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_recording, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    public void switchRecording(View view) {

        this.recording = ! this.recording;

        if(recording) {
            this.label = ((EditText)findViewById(R.id.label_editText)).getText().toString();
            Log.d("LOGGING", "Logging started");
            Log.d("LOGGING", "label = " + this.label);
            ((Button) view).setText("Stop");
            this.current_seq_id = this.dbHandler.insertSequence(this.label); // insert new sequence
        } else {
            Log.d("LOGGING", "Logging stopped");
            this.dbHandler.insertSensorData(this.buffer, this.current_seq_id, this.fftStrings);
            buffer.clear();
            this.fftStrings.clear();
            this.lastSensorValues.clear();
            ((Button) view).setText("Start");
        }
    }


    public void exportDB(View view) {
        boolean result = this.dbHandler.exportDB();
        if (result) {
            Toast.makeText(this, "Database exported to external storage! :)",
                    Toast.LENGTH_LONG).show();
        }
    }


    public void addSensorValue(double[] vals) {

        this.lastSensorValues.add(vals);
        if (this.lastSensorValues.size() > this.history_size)
            this.lastSensorValues.remove(0);

        if(this.lastSensorValues.size() >= this.history_size && this.buffer != null && this.recording) {
            this.buffer.add(vals);

            this.lastFFTResult.clear();
            for (int d = 0; d < this.lastSensorValues.get(0).length; d++) {
                this.lastFFTResult.add(new double[this.lastSensorValues.size()]);
            }
            for (int d = 0; d < this.lastSensorValues.get(0).length; d++) {
                double[] timeseries1Dre = new double[this.lastSensorValues.size()];
                double[] timeseries1Dimg = new double[this.lastSensorValues.size()];
                for (int i = 0; i < this.lastSensorValues.size(); i++) {
                    timeseries1Dre[i] = this.lastSensorValues.get(i)[d];
                }
                this.fft.fft(timeseries1Dre, timeseries1Dimg);

                double[] fftmag = new double[this.history_size];
                for (int i = 0; i < this.history_size; i++) {
                    double mag = Math.sqrt(timeseries1Dre[i] * timeseries1Dre[i] + timeseries1Dimg[i] * timeseries1Dimg[i]);
                    fftmag[i] = mag;
                }
                this.lastFFTResult.set(d, fftmag);
            }

            StringBuffer sb = new StringBuffer();
            for (int d = 0; d < 3; d++) {
                for (int i = 0; i < this.history_size/2; i++) { // 2 since complex to complex fft is mirrored after N/2
                    double mag = Math.round(this.lastFFTResult.get(d)[i]*1000)/1000.;
                    sb.append(mag + ",");
                }
                sb.deleteCharAt(sb.length()-1);
                sb.append(";");
            }
            sb.deleteCharAt(sb.length()-1);
            this.fftStrings.add(sb.toString());
            //Log.d("FFT", sb.toString());
        }



    }


}
