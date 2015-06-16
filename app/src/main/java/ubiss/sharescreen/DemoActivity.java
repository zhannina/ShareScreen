package ubiss.sharescreen;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.aware.Aware;
import com.aware.Aware_Preferences;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import ubiss.sharescreen.processing.FFT;
import ubiss.sharescreen.processing.LinearSVM;
import ubiss.sharescreen.processing.SVM;


public class DemoActivity extends ActionBarActivity {


    public static DemoActivity instance;


    //SVM svm;
    LinearSVM svm;

    protected FFT fft;
    protected List<double[]> lastFFTResult = new ArrayList<double[]>();
    protected List<double[]> lastSensorValues = new ArrayList<double[]>();
    protected int history_size = 128;

    protected double[] feature_vec = new double[189];//189

    protected int last_pred;

    double[] fftmag;
    double[] timeseries1Dre;
    double[] timeseries1Dimg;

    protected List<Integer> predBuffer = new ArrayList<Integer>();
    protected int predBufferSize = 4;


    protected int updateCounter = 0;

    public boolean listening = false;

    Runnable animator;


    private String[] images = {"undecided.png", "undecided2.png"};
    private int imageIndex = 0;
    private boolean animating = false;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_demo);

        this.fft = new FFT(this.history_size);

        StringBuilder buf = new StringBuilder();
        InputStream json = null;
        try {
            json = getApplicationContext().getAssets().open("export_linear_svm4.txt");//TODO: svm file can be changed here

            BufferedReader in =
                    new BufferedReader(new InputStreamReader(json, "UTF-8"));
            String str;
            while ((str = in.readLine()) != null) {
                buf.append(str);
            }
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.d("JSON", buf.toString());

        try {
            JSONObject jsono = new JSONObject(buf.toString());
            this.svm = new LinearSVM(jsono);//TODO: svm type can be changed here
        } catch (JSONException e) {
            e.printStackTrace();
        }

        DemoActivity.instance = this;

        updateImage("undecided.png");


    }


    public void update(double[] vals) {

        if (!this.listening)
            return;
        //Log.d("SVM", "called update");

        //double[] vals_tmp = {Math.sqrt(vals[0]*vals[0]+vals[1]*vals[1]+vals[2]*vals[2])};
        //vals = vals_tmp;

        this.lastSensorValues.add(vals);
        if (this.lastSensorValues.size() > this.history_size)
            this.lastSensorValues.remove(0);


        if (this.updateCounter == this.history_size * this.predBufferSize) {
            this.stopListening();
        }

        this.updateCounter++;


        //this.updateCounter > this.history_size &&
        //this.updateCounter < this.history_size*4 &&
        if (this.updateCounter <= this.history_size * this.predBufferSize && this.lastSensorValues.size() >= this.history_size) {

            //this.updateCounter = 0;

            this.lastFFTResult.clear();

            for (int d = 0; d < this.lastSensorValues.get(0).length; d++) {
                timeseries1Dre = new double[this.lastSensorValues.size()];
                timeseries1Dimg = new double[this.lastSensorValues.size()];
                for (int i = 0; i < this.lastSensorValues.size(); i++) {
                    timeseries1Dre[i] = this.lastSensorValues.get(i)[d];
                }
                this.fft.fft(timeseries1Dre, timeseries1Dimg);

                fftmag = new double[this.history_size];
                double mag;
                for (int i = 0; i < this.history_size; i++) {
                    mag = Math.sqrt(timeseries1Dre[i] * timeseries1Dre[i] + timeseries1Dimg[i] * timeseries1Dimg[i]);
                    fftmag[i] = mag;
                }
                this.lastFFTResult.add(fftmag);
            }


            this.lastSensorValues.clear();//TODO: DEBUG


            if (this.svm != null) {

                //StringBuffer sb = new StringBuffer();
                for (int d = 0; d < vals.length; d++) {//3
                    for (int i = 0; i < 63; i++) {
                        this.feature_vec[d * 63 + i] = this.lastFFTResult.get(d)[i + 1];
                        //sb.append(this.feature_vec[d*63+1] + ", ");
                    }
                }
                int pred = this.svm.predictWithSVM(this.feature_vec);
                /*if(this.lastFFTResult.get(2)[6] > 0.85)
                    pred = 0;
                else if(this.lastFFTResult.get(2)[6] > 0.4)
                    pred = 1;
                else pred = 2;*/

                ///*
                this.predBuffer.add(pred);
                if (this.predBuffer.size() > this.predBufferSize)
                    this.predBuffer.remove(0);

                int[] counts = new int[3]; // 3 classes
                for (Integer i : this.predBuffer) {
                    counts[i] += 1;
                }
                if (counts[2] >= this.predBufferSize / 2) pred = 2;
                else if (counts[1] >= this.predBufferSize / 2) pred = 1;
                else if (counts[0] >= this.predBufferSize / 2) pred = 0;
                //*/

                //Log.d("DEBUG", sb.toString());
                /*if(pred != last_pred) {
                    //Log.d("SVM PRED", "prediction: " + pred);
                    String text = "...";
                    if (pred == 0) text = "left";
                    else if (pred == 1) text = "right";

                    ((TextView) findViewById(R.id.prediction_textView)).setText(text);
                }*/
                this.last_pred = pred;


            }

        }


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_demo, menu);
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

    public void switchListening(View view) {

        this.listening = !this.listening;

        if (listening) {
            updateImage("undecided.png");
            this.animate();
            Log.d("LOGGING", "Listening started");
            //((Button) view).setText("Stop");
            Aware.setSetting(this, Aware_Preferences.STATUS_ACCELEROMETER, true);
            sendBroadcast(new Intent(Aware.ACTION_AWARE_REFRESH));
        } else {
            stopListening();
        }
    }


    private void stopListening() {


        Aware.setSetting(this, Aware_Preferences.STATUS_ACCELEROMETER, false);
        sendBroadcast(new Intent(Aware.ACTION_AWARE_REFRESH));

        String text = "...";
        if (this.last_pred == 0) text = "left";
        else if (this.last_pred == 1) text = "right";

        //((TextView) findViewById(R.id.prediction_textView)).setText(text);

        this.stopAnimation();
        updateImage("homer-" + text + ".jpg");

        this.updateCounter = 0;
        this.listening = false;
        Log.d("LOGGING", "Listening stopped");
        this.lastSensorValues.clear();
        //((Button) findViewById(R.id.listen_button)).setText("Listen");

    }


    private void animate(){

        this.animating = true;

        this.animator = new Runnable(){
            public void run(){

                if(animating) {
                    updateImage(images[imageIndex]);
                    imageIndex = 1 - imageIndex;
                    ((ImageView) findViewById(R.id.pred_imageView)).postDelayed(animator, 400);
                }
            }
        };
        ((ImageView) findViewById(R.id.pred_imageView)).postDelayed(this.animator, 400);
    }


    private void stopAnimation(){
       this.animating = false;
    }



    private void updateImage(String image) {
        try {
            // get input stream
            InputStream ims = getAssets().open(image);
            // load image as Drawable
            Drawable d = Drawable.createFromStream(ims, null);
            // set image to ImageView
            ((ImageView) findViewById(R.id.pred_imageView)).setImageDrawable(d);
        } catch (IOException ex) {
            return;
        }
    }
}
