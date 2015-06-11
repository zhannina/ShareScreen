package ubiss.sharescreen.processing;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by daniel on 11.06.2015.
 */
public class SVM {


    private class SVMO {

        public int c1;
        public int c2;
        public List<double[]> svs_c1;
        public List<double[]> svs_c2;
        public double[] coefs_c1;
        public double[] coefs_c2;
        public double intercept;
    }


    private double gamma;
    private int n_classes;

    private List<SVMO> svms;


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


    public static List<double[]> convertJSON2DoubleArray2D(JSONArray a) {

        List<double[]> output = new ArrayList<double[]>();
        try {
            for (int i = 0; i < a.length(); i++) {
                JSONArray a_i = a.getJSONArray(i);
                double[] output_i = new double[a_i.length()];
                for (int j = 0; j < a_i.length(); j++) {
                    output_i[j] = a_i.getDouble(j);
                }
                output.add(output_i);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return output;
    }


    public SVM(JSONObject svm_jsono) {

        Log.d("SVM", "SVM constructor");
        this.svms = new ArrayList<SVMO>();

        try {
            this.gamma = svm_jsono.getDouble("gamma");
            this.n_classes = svm_jsono.getInt("n_classes");
            Log.d("SVM", this.gamma+", " + this.n_classes);

            JSONArray svms_array = svm_jsono.getJSONArray("svms");
            for (int i = 0; i < svms_array.length(); i++) {
                JSONObject svm_i = svms_array.getJSONObject(i);
                SVMO svmo = new SVMO();
                svmo.c1 = svm_i.getInt("c1");
                svmo.c2 = svm_i.getInt("c2");
                Log.d("SVM", svmo.c1+", " + svmo.c2);
                svmo.intercept = svm_i.getDouble("intercept");
                svmo.coefs_c1 = convertJSON2DoubleArray(svm_i.getJSONArray("coefs_c1"));
                svmo.coefs_c2 = convertJSON2DoubleArray(svm_i.getJSONArray("coefs_c2"));
                svmo.svs_c1 = convertJSON2DoubleArray2D(svm_i.getJSONArray("svs_c1"));
                svmo.svs_c2 = convertJSON2DoubleArray2D(svm_i.getJSONArray("svs_c2"));

                this.svms.add(svmo);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    // Creates the RBF-Kernel-value for two given vectors and the gamma parameter:
    public static double evalKernelFunctionRBF(double[] v, double[] v2, double gamma) {
        double distance = 0;
        for (int i = 0; i < v.length; i++) {
            distance += ((v[i] - v2[i]) * (v[i] - v2[i]));
        }
        return Math.exp(-gamma * distance);
    }


    // Creates the RBF-Kernel-value for two given vectors and the gamma parameter:
    public static double evalKernelFunctionLinear(double[] v, double[] v2, double gamma) {
        double distance = 0;
        for (int i = 0; i < v.length; i++) {
            distance += v[i] * v2[i];
        }
        return distance;
    }


    // Returns the index of the maximum value in the given array:
    public static int argmax(int[] a) {
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


    /*
    Returns the class prediction for the given input point.
    "input" is an array: [featue_1, feature_2, ..., feature_n]
    */
    public int predictWithSVM(double[] input) {

        // Create array to collect the votes for the classes:
        int[] votes = new int[this.n_classes];
        for (int i = 0; i < votes.length; i++) {
            votes[i] = 0;
        }
        ;

        // Iterate over all one-vs-one problem svms:
        for (int i = 0; i < this.svms.size(); i++) {

            SVMO svm_i = this.svms.get(i);

            // Compute prediction of the current svm:
            double prediction = 0;
            // Sum of distances to support vectors (of class 1) in kernel space, weighted by dual coefficients:
            for (int j = 0; j < svm_i.coefs_c1.length; j++) {
                prediction += -svm_i.coefs_c1[j] * evalKernelFunctionLinear(input, svm_i.svs_c1.get(j), gamma);
            }
            // Sum of distances to support vectors (of class 2) in kernel space, weighted by dual coefficients:
            for (int j = 0; j < svm_i.coefs_c2.length; j++) {
                prediction += -svm_i.coefs_c2[j] * evalKernelFunctionLinear(input, svm_i.svs_c2.get(j), gamma);
            }
            // Subtract the intercept:
            prediction -= svm_i.intercept;

            // Lookup which class is indicated by the (sign of the) prediction-value:
            int vote = (int) Math.signum(prediction);
            int predicted_class;
            if (vote == -1) {
                predicted_class = svm_i.c1;
            } else {
                predicted_class = svm_i.c2;
            }
            // Add a vote for the class predicted by this svm:
            votes[predicted_class] += 1;

            Log.d("SVM", svm_i.c1 + ", " + svm_i.c2 + ", " + prediction + ", " + predicted_class);
        }
        ;

        // Compute the global prediction as the majority vote of all one-vs-one problem predictions:
        int global_prediction = argmax(votes);

        Log.d("SVM", "prediction: class " + global_prediction);

        // Return the global prediction:
        return global_prediction;

        // Alternatively, we could also return the distribution:
        // return votes;
    }
}
