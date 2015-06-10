package ubiss.sharescreen;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.aware.Accelerometer;
import com.aware.Aware;
import com.aware.Aware_Preferences;


public class MainActivity extends ActionBarActivity {

    // Test comment Daniel
    // Test comment Zhanna

    private static AccelerometerBR accelBR = new AccelerometerBR();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Aware.setSetting(getApplicationContext(), Aware_Preferences.STATUS_ACCELEROMETER, true); // to activate aware

        IntentFilter filter = new IntentFilter();
        filter.addAction(Accelerometer.ACTION_AWARE_ACCELEROMETER);
        registerReceiver(accelBR, filter);

        sendBroadcast(new Intent(Aware.ACTION_AWARE_REFRESH));
    }

    public static class AccelerometerBR extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            ContentValues raw_data = (ContentValues) intent.getParcelableExtra(Accelerometer.EXTRA_DATA);
            Log.d("DEMO", raw_data.toString());
        }
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
