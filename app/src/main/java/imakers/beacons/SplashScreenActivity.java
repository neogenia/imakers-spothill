package imakers.beacons;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import imakers.classes.Campaign;
import imakers.tools.CheckInternetConnection;
import imakers.tools.MyApplication;
import imakers.tools.MyUtils;


public class SplashScreenActivity extends Activity {

    CheckInternetConnection checkInternetConnection;
    Boolean isInternetPresent = false;
    private final int SPLASH_DISPLAY_LENGTH = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        ((MyApplication)getApplicationContext()).setMainActivity(this);
        ((MyApplication)getApplicationContext()).setCurrentSpots(new ArrayList<Integer>());

        ImageView imageView = (ImageView) findViewById(R.id.loadingGif);
        imageView.setBackgroundResource(R.drawable.loading);

        AnimationDrawable animationDrawable = (AnimationDrawable) imageView.getBackground();
        animationDrawable.start();

        checkInternetConnection = new CheckInternetConnection(getApplicationContext());
        isInternetPresent = checkInternetConnection.isConnectingToInternet();

        String android_id = Settings.Secure.getString(getContentResolver(),
                Settings.Secure.ANDROID_ID);

        //((MyApplication)getApplicationContext()).setHash(android_id);

        if (isInternetPresent) {

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {

                    String sss = MyUtils.LoadPreferences("login", SplashScreenActivity.this);


                    if(sss.isEmpty()) {
                        Intent mainIntent = new Intent(SplashScreenActivity.this, LoginActivity.class).putExtra("isStart", true);
                        SplashScreenActivity.this.startActivity(mainIntent);
                        SplashScreenActivity.this.finish();
                    }
                    else {
                        try {
                            JSONObject object = new JSONObject(MyUtils.LoadPreferences("login_json", SplashScreenActivity.this));

                            ((MyApplication)getApplicationContext()).setHash(object.getString("hash"));

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        Intent mainIntent = new Intent(SplashScreenActivity.this, MainActivity.class);
                        SplashScreenActivity.this.startActivity(mainIntent);
                        SplashScreenActivity.this.finish();
                    }

                }
            }, SPLASH_DISPLAY_LENGTH);
        } else {
            showAlertDialogInternet(SplashScreenActivity.this, "Nedostupné internetové připojení",
                    "Žádné internetové připojení, prosím připojte se k internetu", false);
        }



    }

    public void showAlertDialogInternet(Context context, String title, String message, Boolean status) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setIcon(R.drawable.ic_launcher);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                SplashScreenActivity.this.finish();
            }
        });
        builder.setNegativeButton("Nastaveni", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                startActivityForResult(new Intent(android.provider.Settings.ACTION_SETTINGS), 1);
            }
        });
        /*builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                finish();
                System.exit(0);
            }
        });*/

        builder.show();
    }

}
