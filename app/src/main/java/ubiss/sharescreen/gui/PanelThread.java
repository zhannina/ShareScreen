package ubiss.sharescreen.gui;

/**
 * Created by daniel on 03.12.2014.
 */
import android.graphics.Canvas;
import android.view.SurfaceHolder;

class PanelThread extends Thread {

    private final SurfaceHolder surfaceHolder;
    private AbstractDrawingPanel panel;
    private boolean running = false;

    public PanelThread(SurfaceHolder surfaceHolder, AbstractDrawingPanel panel) {
        this.surfaceHolder = surfaceHolder;
        this.panel = panel;
    }

    public void setRunning(boolean run) {
        this.running = run;
    }

    @Override
    public void run() {

        Canvas c;
        while (this.running) {
            c = null;
            try {
                c = this.surfaceHolder.lockCanvas(null);
                synchronized (this.surfaceHolder) {
                    this.panel.postInvalidate();
                }
            } finally {
                if (c != null) {
                    this.surfaceHolder.unlockCanvasAndPost(c);
                }
            }
        }
    }
}
