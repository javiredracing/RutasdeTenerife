package tenerife.rutas.jfernandez.rutasdetenerife;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by jfernandez on 09/06/2015.
 */
public class ActivitySplash extends Activity{
    private final long SPLASH_DISPLAY_LENGTH = 5000;
    private CountDownTimer temporizador;
    private boolean isFinished;
    private ImageView splashMain;
    private LinearLayout appTitle;
    //private Typeface font;

    /*
     *
     */
    private void startApp() {
        Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.splash);
        splashMain = (ImageView)findViewById(R.id.iv_splash_view);
        appTitle = (LinearLayout)findViewById(R.id.llSplashTitle);
       // final Typeface tf = Typeface.createFromAsset(getAssets(), "font/OpenSans-Regular.ttf");

        //((TextView) findViewById(R.id.mainActivityAietText)).setTypeface(font);
        //TextView tv = ((TextView) findViewById(R.id.mainActivityFooter));//.setTypeface(tf);
        //tv.setTypeface(tf, R.style.mainActivityFooterText);

        isFinished = false;

        temporizador = new CountDownTimer(SPLASH_DISPLAY_LENGTH, 1000){
            @Override
            public void onFinish() {
                isFinished = true;
                startApp();
            }

            @Override
            public void onTick(long millisUntilFinished) {

            }
        }.start();
    }

    @Override
    protected void onStart() {
        super.onStart();
        final Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fadein);
        final Animation inScreen = AnimationUtils.loadAnimation(this, R.anim.in_screen);
        splashMain.startAnimation(fadeIn);
        appTitle.startAnimation(inScreen);
    }

    @Override
    public void finish() {
        if (!isFinished)
            temporizador.cancel();

        super.finish();
    }

    public void skip(View v) {
        appTitle.clearAnimation();
        splashMain.clearAnimation();
        temporizador.cancel();
        startApp();
    }
}