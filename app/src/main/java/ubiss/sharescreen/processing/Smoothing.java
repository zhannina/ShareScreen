package ubiss.sharescreen.processing;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by daniel on 11.06.2015.
 */
public class Smoothing {

    private int window_size = 100;
    private List<double[]> buffer = new ArrayList<double[]>();


    public Smoothing(int window_size) {
        this.window_size = window_size;
    }


    public double[] smooth(double[] vals) {

        double[] output = new double[vals.length];

        this.buffer.add(vals);
        if (this.buffer.size() > this.window_size) {
            this.buffer.remove(0);
        }
        for (int d = 0; d < this.buffer.get(0).length; d++) {
            for (int i = 0; i < this.buffer.size(); i++) {
                output[d] += this.buffer.get(i)[d];
            }
            output[d] /= this.buffer.size();
        }

        return output;
    }
}
