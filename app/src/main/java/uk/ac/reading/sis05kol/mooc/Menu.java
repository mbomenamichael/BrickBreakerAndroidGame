package uk.ac.reading.sis05kol.mooc;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.app.Activity;
import android.content.Intent;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.graphics.drawable.AnimationDrawable;
import android.widget.TextView;

public class Menu extends Activity  {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        LinearLayout linearLayout = findViewById(R.id.layout);
        AnimationDrawable animationDrawable = (AnimationDrawable) linearLayout.getBackground();
        animationDrawable.setEnterFadeDuration(500);
        animationDrawable.setExitFadeDuration(500);
        animationDrawable.start();

        // Hides navigation/status bar on screen.
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideNavBar();
    }
    private void hideNavBar()   {
        this.getWindow().getDecorView()
                .setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_FULLSCREEN |
                                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                                // Need to ensure that not any user touch can bring back nav bar.
                                // Next four args ensure this doesn't happen.
                                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
                                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                                View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
    }
    public void mainMenu(View view) {
        startActivity(new Intent(getApplicationContext(), MainActivity.class));
    }
}
