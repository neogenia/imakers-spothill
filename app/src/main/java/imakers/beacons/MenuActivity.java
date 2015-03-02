package imakers.beacons;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import imakers.interfaces.MyAsyncLisener;
import imakers.tools.MyActionPanel;
import imakers.tools.MyApplication;
import imakers.tools.MyHttpClientJSON;
import imakers.tools.MyUtils;


public class MenuActivity extends Activity {

    private MyActionPanel panel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

    }

    @Override
    protected void onResume() {
        super.onResume();

        //nastavení panelu a aktuální stránky
        panel = new MyActionPanel(this, getString(R.string.menu_title));
        panel.showBack();


        //anonymní onclick na blokování, tam je to udělaný dynamicky, že buď to načte categorie nebo providery
        findViewById(R.id.block_cat).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MenuActivity.this, MenuBlockActivity.class).putExtra("isCat", true));
            }
        });
        findViewById(R.id.block_pro).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MenuActivity.this, MenuBlockActivity.class).putExtra("isCat", false));
            }
        });


        //když se načte z paměti login tak kontroluju jestli není prázdný a zároveň se nesmí rovnat false, protože to by znamenalo, že je lognutý, jako anonymní uživatel
        if(!MyUtils.LoadPreferences("login", this).isEmpty() && !MyUtils.LoadPreferences("login", MenuActivity.this).contains("false")) {
            ((TextView)findViewById(R.id.login_text)).setText(getString(R.string.log_out));
            try {
                JSONObject object = new JSONObject(MyUtils.LoadPreferences("login_json", this));

                if(!object.getString("email").isEmpty()) {
                    findViewById(R.id.profile).setVisibility(View.VISIBLE);
                    findViewById(R.id.profile).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            startActivity(new Intent(MenuActivity.this, ProfileActivity.class));
                        }
                    });
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }


        //onClick na login nebo zde i obsahuje odhlášení v else bloku
        findViewById(R.id.login).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(MyUtils.LoadPreferences("login", MenuActivity.this).isEmpty() || MyUtils.LoadPreferences("login", MenuActivity.this).contains("false")) {
                    startActivity(new Intent(MenuActivity.this, LoginActivity.class).putExtra("isStart", false));
                }
                else {
                    ((TextView)findViewById(R.id.login_text)).setText(getString(R.string.log_in_user));
                    findViewById(R.id.profile).setVisibility(View.GONE);
                    MyUtils.SavePreferences("login", "", MenuActivity.this);
                    MyUtils.SavePreferences("login_json", "", MenuActivity.this);
                    startActivity(new Intent(MenuActivity.this, LoginActivity.class).putExtra("logout", true));
                }

            }
        });


        //zapnutí activity s tutorialem
        findViewById(R.id.help).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MenuActivity.this, TutorialActivity.class));
            }
        });

    }
}
