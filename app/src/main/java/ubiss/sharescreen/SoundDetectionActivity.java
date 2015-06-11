package ubiss.sharescreen;


import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;

import ubiss.sharescreen.gui.SensorReadingsDisplay;
import ubiss.sharescreen.processing.AudioRecordDemo;
import ubiss.sharescreen.processing.DynamicPlot;


public class SoundDetectionActivity extends ActionBarActivity {


    public static SoundDetectionActivity instance;


    private DynamicPlot dGraphView;
    private DynamicPlot fGraphView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sound);

        dGraphView = (DynamicPlot)findViewById(R.id.graph_decibel);
        dGraphView.setMaxValue(10);
        fGraphView = (DynamicPlot)findViewById(R.id.graph_frequency);
        fGraphView.setMaxValue(10);

        SoundDetectionActivity.instance = this;

        AudioRecordDemo audio = new AudioRecordDemo();
        audio.getNoiseLevel();

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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

    public void addDecibel(final double point) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d("PaintSound", "Called. " + ((point-50)/100+5));
                dGraphView.addDataPoint(((point-50)/100+5));
            }
        });
    }
    public void addFrequency(final double[] array) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                for(int i=0;i<array.length;i++)
                    array[i]=array[i]/2000;

                fGraphView.addDataArray(array);
            }
        });
    }

}
