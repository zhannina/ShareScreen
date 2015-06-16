package ubiss.sharescreen.processing;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by daniel on 12.06.2015.
 */
public class LinearSVM {


    public static double[] convertJSON2DoubleArray(JSONArray a) {
        double[] output = new double[a.length()];
        try {
            for (int i = 0; i < a.length(); i++) {
                output[i] = a.getDouble(i);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return output;
    }


    // Returns the index of the maximum value in the given array:
    public static int argmax(double[] a) {
        double value_max = a[0];
        int index_max = 0;
        for (int i = 1; i < a.length; i++) {
            if (a[i] > value_max) {
                index_max = i;
                value_max = a[i];
            }
        }
        return index_max;
    }



    private class SVMFunc{

        public int c;
        public double[] coefs;
        public double intercept;

    }


    private List<SVMFunc> functions;
    private int n_classes;


    public LinearSVM(JSONObject svm_jsono) {

        Log.d("LinearSVM", "LinearSVM constructor");
        this.functions = new ArrayList<SVMFunc>();

        try {
            this.n_classes = svm_jsono.getInt("n_classes");

            JSONArray svms_array = svm_jsono.getJSONArray("functions");
            for (int i = 0; i < svms_array.length(); i++) {
                JSONObject svm_i = svms_array.getJSONObject(i);
                SVMFunc svmFunc = new SVMFunc();
                svmFunc.c = svm_i.getInt("c");
                svmFunc.intercept = svm_i.getDouble("intercept");
                svmFunc.coefs = convertJSON2DoubleArray(svm_i.getJSONArray("coefs"));
                this.functions.add(svmFunc);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    /*
    Returns the class prediction for the given input point.
    "input" is an array: [featue_1, feature_2, ..., feature_n]
    */
    public int predictWithSVM(double[] input) {

        // Iterate over all classes:
        double[] scores = new double[this.n_classes];
        SVMFunc func;
        for (int i = 0; i < n_classes; i++) {

            func = this.functions.get(i);

            // Compute prediction of the current svm:
            double prediction = 0;
            for (int j = 0; j < func.coefs.length; j++) {
                prediction += func.coefs[j] * input[j];
            }

            // Subtract the intercept:
            prediction += func.intercept;

            scores[i] = prediction;
        }


        // Compute the global prediction as the class with the highest score:
        int global_prediction = argmax(scores);

        Log.d("Linear SVM", "prediction: class " + global_prediction);

        // Return the global prediction:
        return global_prediction;
    }
}
