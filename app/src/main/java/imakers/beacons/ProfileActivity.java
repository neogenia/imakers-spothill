package imakers.beacons;

import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import imakers.beacons.R;
import imakers.interfaces.MyAsyncLisener;
import imakers.tools.MyActionPanel;
import imakers.tools.MyApplication;
import imakers.tools.MyHttpClientJSON;
import imakers.tools.MyUtils;

public class ProfileActivity extends Activity {

    private MyActionPanel panel;
    private boolean isMan;
    private boolean starting;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        panel = new MyActionPanel(this, "Účet");

        //isMan = true;


        //načítání dat profilu
        try {
            JSONObject object = new JSONObject(MyUtils.LoadPreferences("login_json", this));

            if(object.getInt("sex") == 0) {

                ((TextView)findViewById(R.id.man)).setTextColor(Color.WHITE);
                ((TextView)findViewById(R.id.man)).setBackgroundColor(Color.parseColor("#055296"));
                ((TextView)findViewById(R.id.woman)).setTextColor(Color.parseColor("#055296"));
                ((TextView)findViewById(R.id.woman)).setBackgroundColor(Color.TRANSPARENT);

                isMan = true;
                starting = true;

            }
            else {
                ((TextView)findViewById(R.id.woman)).setTextColor(Color.WHITE);
                ((TextView)findViewById(R.id.woman)).setBackgroundColor(Color.parseColor("#055296"));
                ((TextView)findViewById(R.id.man)).setTextColor(Color.parseColor("#055296"));
                ((TextView)findViewById(R.id.man)).setBackgroundColor(Color.TRANSPARENT);

                isMan = false;
                starting = false;
            }

            ((TextView)findViewById(R.id.email)).setText(object.getString("email"));
            ((TextView)findViewById(R.id.name)).setText(object.getString("name")+" "+object.getString("surname"));

        } catch (JSONException e) {
            e.printStackTrace();
        }


        //přepínač
        findViewById(R.id.woman).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setBackgroundColor(Color.parseColor("#055296"));

                ((TextView)v).setTextColor(Color.WHITE);
                isMan = false;
                ((TextView)findViewById(R.id.man)).setTextColor(Color.parseColor("#055296"));
                ((TextView)findViewById(R.id.man)).setBackgroundColor(Color.TRANSPARENT);
            }
        });

        findViewById(R.id.man).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setBackgroundColor(Color.parseColor("#055296"));

                ((TextView)v).setTextColor(Color.WHITE);
                isMan = true;
                ((TextView)findViewById(R.id.woman)).setTextColor(Color.parseColor("#055296"));
                ((TextView)findViewById(R.id.woman)).setBackgroundColor(Color.TRANSPARENT);
            }
        });


        //uložení změn v profilu
        ((TextView)findViewById(R.id.registration_button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String pass = ((EditText)findViewById(R.id.password)).getText().toString();
                String passOld = ((EditText)findViewById(R.id.old_password)).getText().toString();
                final String name = ((EditText)findViewById(R.id.name_edit)).getText().toString();
                final String surname = ((EditText)findViewById(R.id.sur_name_edit)).getText().toString();
                if((!pass.isEmpty() && !passOld.isEmpty()) || isMan != starting || !name.isEmpty() || !surname.isEmpty()) {
                    MyUtils.showDialog(ProfileActivity.this);
                    final JSONObject object = new JSONObject();

                    try {

                        object.put("sex", isMan ? 0:1);


                        if(!pass.isEmpty()) {
                            object.put("password", pass);
                            object.put("oldPassword", passOld);
                        }

                        final JSONObject object2 = new JSONObject(MyUtils.LoadPreferences("login_json", ProfileActivity.this));
                        object.put("name", name.isEmpty() ? object2.getString("name") : name);

                        object.put("surname", surname.isEmpty() ? object2.getString("surname") : surname);

                        object.put("hash", ((MyApplication)getApplicationContext()).getHash());

                        String androidID = Settings.Secure.getString(getContentResolver(),
                                Settings.Secure.ANDROID_ID);

                        JSONObject device = new JSONObject();

                        device.put("hash", androidID);
                        device.put("type", 1);
                        device.put("system", getAndroidVersion());
                        device.put("manufacturer", ""+ Build.MANUFACTURER);
                        device.put("model", ""+Build.MODEL);

                        object.put("device", device);

                        MyHttpClientJSON.post(ProfileActivity.this, "setup", object, new MyAsyncLisener() {
                            @Override
                            public void onComplete(JSONObject data) {



                                    if(!name.isEmpty()) {
                                        try {
                                            object2.put("name", name);
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }

                                    if(!surname.isEmpty()) {
                                        try {
                                            object2.put("surname", surname);
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }

                                    try {
                                        object2.put("sex", isMan ? 0 : 1);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

                                    MyUtils.SavePreferences("login_json", object2.toString(), ProfileActivity.this);
                                    MyUtils.showToast("Profil upraven!", ProfileActivity.this);

                                    try {
                                        ((TextView)findViewById(R.id.name)).setText(object2.getString("name")+" "+object2.getString("surname"));
                                        ((EditText)findViewById(R.id.password)).setText("");
                                        ((EditText)findViewById(R.id.old_password)).setText("");
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }



                                MyUtils.dissmissDialog(ProfileActivity.this);

                            }
                        });

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }

            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.profile, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public String getAndroidVersion() {
        String release = Build.VERSION.RELEASE;
        int sdkVersion = Build.VERSION.SDK_INT;
        return "SDK: " + sdkVersion + " (" + release +")";
    }
}
