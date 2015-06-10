package ubiss.sharescreen;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.aware.Accelerometer;
import com.aware.Aware;
import com.aware.Aware_Preferences;


public class Vibration extends ActionBarActivity {

    private Vibrator vibrator;
    private Button bt_vibration;
    boolean IsVibrating;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.vibration);

        IsVibrating = false;

        bt_vibration = (Button)findViewById(R.id.button_vibration);//获取按钮资源
        bt_vibration.setOnClickListener(new Button.OnClickListener() {//创建监听
            public void onClick(View v) {

                if(IsVibrating)//Vibrating
                {
                    vibrator.cancel();
                    bt_vibration.setText("Start Vibration");
                    IsVibrating = false;
                }else{
                    long [] pattern = {100,400,100,400};    // stop start stop start
                    vibrator.vibrate(pattern, 2);           // Start,repeat twice
                    bt_vibration.setText("Stop Vibration");
                    IsVibrating = true;
                }

            }

        });

        //setting vibration pattern
        vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);




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
