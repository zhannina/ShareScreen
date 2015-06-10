package ubiss.sharescreen.gui;

/**
 * Created by daniel on 03.12.2014.
 */
import android.content.Context;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public abstract class AbstractDrawingPanel extends SurfaceView implements
        SurfaceHolder.Callback {

    protected PanelThread thread;

    protected int drawing_surface_w;
    protected int drawing_surface_h;


    public AbstractDrawingPanel(Context context) {
        super(context);

    }

    public void surfaceCreated(SurfaceHolder holder) {

        this.drawing_surface_w = this.getWidth();
        this.drawing_surface_h = this.getHeight();
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        try {
            this.thread.setRunning(false);
            this.thread.join();
        } catch (InterruptedException e) {
        }
    }

    public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int width, int height) {
        this.drawing_surface_w = width;
        this.drawing_surface_h = height;
    }

    protected void initDrawingThread(){
        this.setWillNotDraw(false);
        this.thread = new PanelThread(getHolder(), this);
        this.thread.setRunning(true);
        this.thread.start();
    }

}
