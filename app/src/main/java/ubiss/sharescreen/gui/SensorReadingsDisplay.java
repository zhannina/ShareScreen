package ubiss.sharescreen.gui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.SurfaceHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by daniel on 03.12.2014.
 */
public class SensorReadingsDisplay extends AbstractDrawingPanel {

    protected Paint drawing_bg_paint;
    protected Rect drawing_bg_rect;
    protected Paint[] drawing_point_paints;

    protected List<double[]> lastSensorValues;
    protected int history_size = 1000;

    protected float[] scaling_factors = {20, 20, 20};

    public SensorReadingsDisplay(Context context) {
        super(context);
        getHolder().addCallback(this);
        this.lastSensorValues = new ArrayList<double[]>();
    }


    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        super.surfaceCreated(surfaceHolder);

        // Background paint:
        this.drawing_bg_paint = new Paint();
        this.drawing_bg_paint.setStrokeWidth(1);
        this.drawing_bg_paint.setStyle(Paint.Style.FILL);
        this.drawing_bg_paint.setColor(Color.rgb(220, 220, 220));

        // Point paints for the different plots:
        this.drawing_point_paints = new Paint[3];
        for(int i = 0; i < this.drawing_point_paints.length; i++){
            Paint p = new Paint();
            p.setStrokeWidth(5);
            p.setStyle(Paint.Style.FILL);
            p.setTextSize(40);
            this.drawing_point_paints[i] = p;
        }
        this.drawing_point_paints[0].setColor(Color.rgb(255, 0, 0));
        this.drawing_point_paints[1].setColor(Color.rgb(0, 255, 0));
        this.drawing_point_paints[2].setColor(Color.rgb(0, 0, 255));

        // Background rectangle:
        this.drawing_bg_rect = new Rect(0, 0, this.drawing_surface_w, this.drawing_surface_h);

        // Start the drawing thread:
        this.initDrawingThread();
    }


    public void onDraw(Canvas canvas) {


        //Fake values, uncomment for testing without sensors:
        //double[] debug = {Math.random()*10-5, Math.random()*10-5, Math.random()*10-5};
        //addSensorValue(debug);

        // Draw background:
        canvas.drawRect(this.drawing_bg_rect, this.drawing_bg_paint);


        // skip drawing plot if there are no values to draw:
        if(this.lastSensorValues.size() == 0)
            return;

        // Plot values:
        float val = 0;
        float x = 0;
        float y = 0;
        float last_x = 0;
        float last_y = 0;

        for (int d = 0; d < this.lastSensorValues.get(0).length; d++) { // iterate over dimensions
            for (int i = 0; i < this.lastSensorValues.size(); i++) { // iterate over time series

                val = (float) this.lastSensorValues.get(i)[d]; // get i-th value of time series
                x = this.drawing_surface_w * i * 1f / this.history_size; // compute x location on screen
                y = this.drawing_surface_h/2 - this.scaling_factors[d] * val; // compute y location on screen

                if (i > 0) // draw a line between the (i-1)-th value and the i-th one:
                    canvas.drawLine(last_x, last_y, x, y, this.drawing_point_paints[d]);

                last_x = x;
                last_y = y;
            }

            // print last value along each dimension as number in top left screen corner:
            canvas.drawText(val+"", 20, 50 + d*50, this.drawing_point_paints[d]);
        }
    }


    /**
     * Add a sensor value to the plot.
     * The plot displays the last X sensor values (currently hardcoded in SensorReadingsDisplay.java).
     * @param vals Array of doubles, for example from accelerometer: vals[0] = x, vals[1] = y, vals[2] = z
     */
    public void addSensorValue(double[] vals) {
        this.lastSensorValues.add(vals);
        if(this.lastSensorValues.size() > this.history_size)
            this.lastSensorValues.remove(0);
    }
}