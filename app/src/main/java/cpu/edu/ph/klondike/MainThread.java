package cpu.edu.ph.klondike;

import android.graphics.Canvas;
import android.view.SurfaceHolder;

// NOTE: most likely no touch
public class MainThread extends Thread {
    private boolean running;
    private SurfaceHolder holder;
    private MainGameView gameView;
    private static final String TAG = MainThread.class.getSimpleName();

    private static final int MAX_FPS = 30; // adjust this to change FPS cap
    private static final int MAX_FRAMESKIPS = 5;
    private static final int FRAME_PERIOD = 1000 / MAX_FPS;

    public MainThread(SurfaceHolder holder, MainGameView gameView) {
        super();
        this.holder = holder;
        this.gameView = gameView;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    @Override
    public void run() {
        Canvas canvas = null;

        long startTime;
        long deltaTime = 0;
        long sleepTime = 0;
        int framesSkipped;

        while (running) {
            try {
                canvas = holder.lockCanvas();
                synchronized (holder) {
                    startTime = System.currentTimeMillis();
                    framesSkipped = 0;
                    gameView.update();
                    gameView.onDraw(canvas);
                    deltaTime = System.currentTimeMillis() - startTime;
                    sleepTime = FRAME_PERIOD - deltaTime;

                    if (sleepTime > 0) {
                        try {
                            // Log.d("Framesguardian", "Exec time: " + deltaTime + " ms");
                            // Log.d("Framesguardian", "Sleeping for " + sleepTime + " ms");
                            Thread.sleep(sleepTime);
                        }
                        catch (InterruptedException ex) {
                            /// do nothing
                        }
                    }

                    while (sleepTime < 0 && framesSkipped < MAX_FRAMESKIPS) {
                        gameView.update();
                        sleepTime += FRAME_PERIOD;
                        framesSkipped++;
                    }
                    // Log.d("Framesguardian", "Frames skipped " + framesSkipped);
                }
            }
            finally {
                if (canvas != null) {
                    holder.unlockCanvasAndPost(canvas);
                }
            }
        }
    }
}
