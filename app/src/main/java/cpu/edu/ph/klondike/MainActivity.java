package cpu.edu.ph.klondike;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

public class MainActivity extends Activity
{
    private MainGameView mainGameView;
    private static final String TAG = MainActivity.class.getSimpleName();

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        );
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        this.mainGameView = new MainGameView(this);

        setContentView(mainGameView);
    }

    public void restartGame() {
        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }

    protected void onPause() {
        super.onPause();
        mainGameView.stopGame();
    }

    protected void onResume() {
        super.onResume();
        mainGameView.startGame();
    }
}
