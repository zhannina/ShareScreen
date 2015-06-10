package ubiss.sharescreen;


import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


import com.aware.Aware;
import com.aware.Aware_Preferences;

public class AmbientNoise extends ActionBarActivity {

    double frequency;
    double decibels;
    private TextView tv_noise;
    private static NoiseObserver noiseObs;
    /**
     * How frequently do we sample the microphone
     */
    public static final String FREQUENCY_PLUGIN_AMBIENT_NOISE = "frequency_plugin_ambient_noise";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.ambientnoise);


        frequency = 0;
        decibels=0;

        tv_noise = (TextView) findViewById(R.id.textview_noise);

        UI_Update();


        Aware.startPlugin(this, "com.aware.plugin.ambient_noise");
        //Aware.setSetting(getApplicationContext(), FREQUENCY_PLUGIN_AMBIENT_NOISE, 1);
        //Aware.setSetting(getApplicationContext(), "plugin_ambient_noise_sample_size", 60);
        //Aware.setSetting(getApplicationContext(), "status_plugin_ambient_noise", true);
        //Aware.setSetting(getApplicationContext(), Aware_Preferences
        Aware.setSetting(this, FREQUENCY_PLUGIN_AMBIENT_NOISE, 1);
        Aware.setSetting(this, "plugin_ambient_noise_sample_size", 60);
        Aware.setSetting(this, "status_plugin_ambient_noise", true);


        //frequency_plugin_ambient_noise:1
        //plugin_ambient_noise_sample_size:60

        noiseObs = new NoiseObserver(new Handler());
        Uri database = Uri.parse("content://com.aware.plugin.ambient_noise.provider.ambient_noise/plugin_ambient_noise");

        getContentResolver().registerContentObserver(
                database,
                true,
                noiseObs );

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
    public class NoiseObserver extends ContentObserver {
        public NoiseObserver(Handler handler) {
            super(handler);
            Log.d("NOISE", "Constructure!");
        }

        @Override
        public void onChange(boolean selfChange) {
            Log.d("NOISE", "onChnage!");
            super.onChange(selfChange);

            Uri database = Uri.parse("content://com.aware.plugin.ambient_noise.provider.ambient_noise/plugin_ambient_noise");

            Cursor raw_data = getContentResolver().query(
                    database,
                    new String[]{"double_frequency","double_decibels"},
                    null,
                    null,
                    "timestamp DESC LIMIT 1");

            if( raw_data != null && raw_data.moveToFirst() ) {
                frequency = raw_data.getDouble(0);
                decibels = raw_data.getDouble(1);
            }


            if( raw_data != null && ! raw_data.isClosed() ) raw_data.close();

            UI_Update();

        }
    }
}
