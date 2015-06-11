package ubiss.sharescreen.processing;

import android.media.MediaRecorder;
import android.os.Handler;
import android.util.Log;

import java.io.File;
import java.io.IOException;

public class MediaRecorderDemo {
    private final String TAG = "MediaRecord";
    private MediaRecorder mMediaRecorder;
    public static final int MAX_LENGTH = 1000 * 60 * 10;// max recording time 1000*60*10;
    private String filePath;

    public MediaRecorderDemo(){
        this.filePath = "/dev/null";
    }

    public MediaRecorderDemo(File file) {
        this.filePath = file.getAbsolutePath();
    }

    private long startTime;
    private long endTime;


    public void startRecord() {
        // begin recording
        if (mMediaRecorder == null)
            mMediaRecorder = new MediaRecorder();
        try {

            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);// set mic

            mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);

            mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

            //prepare
            mMediaRecorder.setOutputFile(filePath);
            mMediaRecorder.setMaxDuration(MAX_LENGTH);
            mMediaRecorder.prepare();

            mMediaRecorder.start();
            // AudioRecord audioRecord.
            //start time
            startTime = System.currentTimeMillis();
            updateMicStatus();
            Log.i("ACTION_START", "startTime" + startTime);
        } catch (IllegalStateException e) {
            Log.i(TAG,
                    "call startAmr(File mRecAudioFile) failed!"
                            + e.getMessage());
        } catch (IOException e) {
            Log.i(TAG,
                    "call startAmr(File mRecAudioFile) failed!"
                            + e.getMessage());
        }
    }

    /**
     * Stop recording
     *
     */
    public long stopRecord() {
        if (mMediaRecorder == null)
            return 0L;
        endTime = System.currentTimeMillis();
        Log.i("ACTION_END", "endTime" + endTime);
        mMediaRecorder.stop();
        mMediaRecorder.reset();
        mMediaRecorder.release();
        mMediaRecorder = null;
        Log.i("ACTION_LENGTH", "Time" + (endTime - startTime));
        return endTime - startTime;
    }

    private final Handler mHandler = new Handler();
    private Runnable mUpdateMicStatusTimer = new Runnable() {
        public void run() {
            updateMicStatus();
        }
    };

    /**
     *refreshing rate
     */
    private int BASE = 1;
    private int SPACE = 100;

    private void updateMicStatus() {
        if (mMediaRecorder != null) {
            double ratio = (double)mMediaRecorder.getMaxAmplitude() /BASE;
            double db = 0;// decibel
            if (ratio > 1)
                db = 20 * Math.log10(ratio);
            Log.d(TAG, "decibels:" + db);
            mHandler.postDelayed(mUpdateMicStatusTimer, SPACE);
        }
    }
}