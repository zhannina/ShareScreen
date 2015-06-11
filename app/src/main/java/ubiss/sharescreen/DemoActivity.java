package ubiss.sharescreen;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import ubiss.sharescreen.processing.FFT;
import ubiss.sharescreen.processing.SVM;


public class DemoActivity extends ActionBarActivity {


    public static DemoActivity instance;


    SVM svm;

    protected FFT fft;
    protected List<double[]> lastFFTResult = new ArrayList<double[]>();
    protected List<double[]> lastSensorValues = new ArrayList<double[]>();
    protected int history_size = 128;

    protected double[] feature_vec = new double[189];

    protected int last_pred;

    double[] fftmag;
    double[] timeseries1Dre;
    double[] timeseries1Dimg;

    protected List<Integer> predBuffer = new ArrayList<Integer>();
    protected int predBufferSize = 20;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo);

        this.fft = new FFT(this.history_size);

        StringBuilder buf = new StringBuilder();
        InputStream json = null;
        try {
            json = getApplicationContext().getAssets().open("export_svm.txt");

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
            this.svm = new SVM(jsono);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        DemoActivity.instance = this;
    }




    public void update(double[] vals){

        this.lastSensorValues.add(vals);
        if (this.lastSensorValues.size() > this.history_size)
            this.lastSensorValues.remove(0);

        if(this.lastSensorValues.size() >= this.history_size) {

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


            if(this.svm != null){

                //StringBuffer sb = new StringBuffer();
                for(int d = 0; d < 3; d++){
                    for (int i = 0; i < 63; i++) {
                        this.feature_vec[d*63+i] = this.lastFFTResult.get(d)[i+1];
                        //sb.append(this.feature_vec[d*63+1] + ", ");
                    }
                }
                int pred = this.svm.predictWithSVM(this.feature_vec);


                /*
                this.predBuffer.add(pred);
                if(this.predBuffer.size() > this.predBufferSize)
                    this.predBuffer.remove(0);

                int[] counts = new int[3]; // 3 classes
                for(Integer i : this.predBuffer){
                    counts[i] += 1;
                }
                if (counts[0] > 10) pred = 0;
                else if (counts[1] > 10) pred = 1;
                else if (counts[2] > 10) pred = 2;
                */

                //Log.d("DEBUG", sb.toString());
                if(pred != last_pred) {
                    //Log.d("SVM PRED", "prediction: " + pred);
                    String text = "...";
                    if (pred == 0) text = "left";
                    else if (pred == 1) text = "right";

                    ((TextView) findViewById(R.id.prediction_textView)).setText(text);
                }
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
}
