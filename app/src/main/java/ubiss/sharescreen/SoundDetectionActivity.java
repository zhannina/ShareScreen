package ubiss.sharescreen;


import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import ubiss.sharescreen.processing.AudioRecordDemo;

public class SoundDetectionActivity extends ActionBarActivity {

    double frequency;
    double decibels;
    private TextView tv_noise;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.ambientnoise);

        AudioRecordDemo audio = new AudioRecordDemo();
        audio.getNoiseLevel();

        frequency = 0;
        decibels=0;

        tv_noise = (TextView) findViewById(R.id.textview_noise);

        UI_Update();


    }

    private void UI_Update() {
        tv_noise.setText("frequency: "+frequency+"\ndecibel: "+decibels);
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

}
