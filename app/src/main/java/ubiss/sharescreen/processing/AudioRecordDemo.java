package ubiss.sharescreen.processing;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import ubiss.sharescreen.PlottingActivity;
import ubiss.sharescreen.SoundDetectionActivity;
import ubiss.sharescreen.gui.SensorReadingsDisplay;

/**
 * Created by Pingjiang on 2015/6/11.
 */
public class AudioRecordDemo {

    protected FFT fft;
    int fft_size;
    boolean IsThreadRunning;

    private static final String TAG = "AudioRecord";
    static final int SAMPLE_RATE_IN_HZ = 8000;
    static final int BUFFER_SIZE = AudioRecord.getMinBufferSize(SAMPLE_RATE_IN_HZ,
            AudioFormat.CHANNEL_IN_DEFAULT, AudioFormat.ENCODING_PCM_16BIT);
    AudioRecord mAudioRecord;
    boolean isGetVoiceRun;
    Object mLock;

    public AudioRecordDemo() {
        mLock = new Object();
    }

    public void getNoiseLevel() {
        if (isGetVoiceRun) {
            Log.e(TAG, "recording");
            return;
        }
        mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE_IN_HZ, AudioFormat.CHANNEL_IN_DEFAULT,
                AudioFormat.ENCODING_PCM_16BIT, BUFFER_SIZE);
        if (mAudioRecord == null) {
            Log.e("sound", "mAudioRecord failed.");
        }
        isGetVoiceRun = true;

        //FFT Initialization
        fft_size=2;
        for(int i=0;i<BUFFER_SIZE;i++)
        {
            if(fft_size>BUFFER_SIZE)
                break;
            fft_size*=2;
        }
        fft_size/=2;
        //debug
        fft_size=128;

        this.fft = new FFT(fft_size);

        IsThreadRunning=false;
        new Thread(new Runnable() {
            @Override
            public void run() {

                android.os.Process.setThreadPriority(-22);
                mAudioRecord.startRecording();
                short[] buffer = new short[fft_size];
                while (isGetVoiceRun) {
                    IsThreadRunning=true;
                    mAudioRecord.startRecording();
                    //r is the length of real data, r is smaller than buffer size
                    int r = mAudioRecord.read(buffer, 0, fft_size);
                    long v = 0;
                    // get the buffer and square it
                    for (int i = 0; i < buffer.length; i++) {
                        v += buffer[i] * buffer[i];
                    }
                    // get volumn
                    double mean = v / (double) r;
                    double volume = 10 * Math.log10(mean);

                    //Frequency
                    double[] timeseries1Dre = new double[fft_size];
                    double[] timeseries1Dimg = new double[fft_size];

                    for (int i = 0; i < buffer.length; i++)
                    {
                        timeseries1Dre[i]=(double) buffer[i];
                    }

                    fft.fft(timeseries1Dre, timeseries1Dimg);

                    //draw plot
                    if(SoundDetectionActivity.instance != null){
                        //double[] vals = {accelValueX, accelValueY, accelValueZ};
                        //double[] vals = {volume/40,volume/40,volume/40};
                        SoundDetectionActivity.instance.addDecibel(volume);
                        SoundDetectionActivity.instance.addFrequency(timeseries1Dre);
                    }

                    Log.d(TAG, "decibels:" + volume%100 + ", b[1]="+buffer[1]+",t[1]"+timeseries1Dre[1]);
                    IsThreadRunning=false;
                    // repeat
                    /*synchronized (mLock) {
                        try {
                            mLock.wait(150);//delay, in ms
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }*/

                }
                mAudioRecord.stop();
                mAudioRecord.release();
                mAudioRecord = null;
            }
        }).start();
    }
}