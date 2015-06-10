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
import android.widget.Toast;

import com.aware.Accelerometer;
import com.aware.providers.Accelerometer_Provider;

import java.util.ArrayList;
import java.util.List;

import ubiss.sharescreen.db.DBHandler;


public class RecordingActivity extends ActionBarActivity {


    public static RecordingActivity instance;

    private DBHandler dbHandler;

    private List<double[]> buffer = new ArrayList<double[]>();

    private  boolean recording = false;

    private String label = "label";

    private int current_seq_id = -1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recording);

        this.dbHandler = DBHandler.getInstance(getApplicationContext());

        RecordingActivity.instance = this;
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
            this.dbHandler.insertSensorData(this.buffer, this.current_seq_id);
            buffer.clear();
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
        if(this.buffer != null && this.recording) {
            this.buffer.add(vals);
        }
    }


}
