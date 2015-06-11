package ubiss.sharescreen;

import android.media.MediaRecorder;
import android.os.Handler;
import android.util.Log;

import java.io.File;
import java.io.IOException;

public class MediaRecorderDemo {
    private final String TAG = "MediaRecord";
    private MediaRecorder mMediaRecorder;
    public static final int MAX_LENGTH = 1000 * 60 * 10;// ���¼��ʱ��1000*60*10;
    private String filePath;

    public MediaRecorderDemo(){
        this.filePath = "/dev/null";
    }

    public MediaRecorderDemo(File file) {
        this.filePath = file.getAbsolutePath();
    }

    private long startTime;
    private long endTime;

    /**
     * ��ʼ¼�� ʹ��amr��ʽ
     *
     *            ¼���ļ�
     * @return
     */
    public void startRecord() {
        // ��ʼ¼��
        /* ��Initial��ʵ����MediaRecorder���� */
        if (mMediaRecorder == null)
            mMediaRecorder = new MediaRecorder();
        try {
            /* ��setAudioSource/setVedioSource */
            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);// ������˷�
            /* ��������Ƶ�ļ��ı��룺AAC/AMR_NB/AMR_MB/Default �����ģ����Σ��Ĳ��� */
            mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
                        /*
             * ����������ļ��ĸ�ʽ��THREE_GPP/MPEG-4/RAW_AMR/Default THREE_GPP(3gp��ʽ
             * ��H263��Ƶ/ARM��Ƶ����)��MPEG-4��RAW_AMR(ֻ֧����Ƶ����Ƶ����Ҫ��ΪAMR_NB)
             */
            mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

            /* ��׼�� */
            mMediaRecorder.setOutputFile(filePath);
            mMediaRecorder.setMaxDuration(MAX_LENGTH);
            mMediaRecorder.prepare();
            /* �ܿ�ʼ */
            mMediaRecorder.start();
            // AudioRecord audioRecord.
            /* ��ȡ��ʼʱ��* */
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
     * ֹͣ¼��
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
     * ���»�Ͳ״̬
     *
     */
    private int BASE = 1;
    private int SPACE = 100;// ���ȡ��ʱ��

    private void updateMicStatus() {
        if (mMediaRecorder != null) {
            double ratio = (double)mMediaRecorder.getMaxAmplitude() /BASE;
            double db = 0;// �ֱ�
            if (ratio > 1)
                db = 20 * Math.log10(ratio);
            Log.d(TAG, "decibels��" + db);
            mHandler.postDelayed(mUpdateMicStatusTimer, SPACE);
        }
    }
}