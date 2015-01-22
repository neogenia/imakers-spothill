package imakers.beacons;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.loopj.android.http.RequestParams;

import org.json.JSONObject;
import org.w3c.dom.Text;

import imakers.interfaces.MyAsyncLisener;
import imakers.tools.MyActionPanel;
import imakers.tools.MyApplication;
import imakers.tools.MyHttpClientJSON;
import imakers.tools.MyUtils;


public class RegistrationActivity extends Activity {

    private MyActionPanel panel;

    Boolean isMan;

    Boolean isStart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.registration);

        isStart = getIntent().getBooleanExtra("isStart", false);

        panel = new MyActionPanel(this, "REGISTRACE");

        isMan = true;

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


        //registrace
        findViewById(R.id.registration_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String name = ((EditText)findViewById(R.id.name)).getText().toString();
                String surname = ((EditText)findViewById(R.id.sur_name)).getText().toString();
                String email = ((EditText)findViewById(R.id.email)).getText().toString();
                String pass = ((EditText)findViewById(R.id.password)).getText().toString();

                Boolean agree = ((CheckBox)findViewById(R.id.checkBox1)).isChecked();

                //ověření podmínek
                if(name.isEmpty()) {
                    MyUtils.showToast("Prázdné jméno", RegistrationActivity.this);
                    return;
                }
                if(surname.isEmpty()) {
                    MyUtils.showToast("Prázdné příjmení", RegistrationActivity.this);
                    return;
                }
                if(email.isEmpty() && email.contains("@")) {
                    MyUtils.showToast("Prázdný email.", RegistrationActivity.this);
                    return;
                }
                if(pass.isEmpty()) {
                    MyUtils.showToast("Prázdné heslo.", RegistrationActivity.this);
                    return;
                }
                if(!agree) {
                    MyUtils.showToast("Musíte souhlasit s podmínkama.", RegistrationActivity.this);
                    return;
                }

                MyUtils.showDialog(RegistrationActivity.this);

                JSONObject object = new JSONObject();


                try {

                    object.put("name", name);
                    object.put("surname", surname);
                    object.put("email", email);
                    object.put("sex", isMan ? 0:1);
                    object.put("password", pass);

                    String androidID = Settings.Secure.getString(getContentResolver(),
                            Settings.Secure.ANDROID_ID);

                    JSONObject device = new JSONObject();

                    device.put("hash", androidID);
                    device.put("type", 1);
                    device.put("system", getAndroidVersion());
                    device.put("manufacturer", ""+Build.MANUFACTURER);
                    device.put("model", ""+Build.MODEL);

                    object.put("device", device);

                    MyHttpClientJSON.post(RegistrationActivity.this, "register", object, new MyAsyncLisener() {
                        @Override
                        public void onComplete(JSONObject data) {
                            if(data != null) {

                                try {

                                    ((MyApplication)getApplicationContext()).setHash(data.getString("hash"));

                                    MyUtils.SavePreferences("login", "true", RegistrationActivity.this);
                                    MyUtils.SavePreferences("login_json", data.toString(), RegistrationActivity.this);


                                    if(isStart) {
                                        if(MyUtils.LoadPreferences("tutorial", RegistrationActivity.this).isEmpty()) {
                                            startActivity(new Intent(RegistrationActivity.this, TutorialActivity.class));
                                        }
                                        else {
                                            startActivity(new Intent(RegistrationActivity.this, MainActivity.class));
                                        }
                                        finish();
                                    }
                                    else {
                                        startActivity(new Intent(RegistrationActivity.this, MenuActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
                                        finish();
                                    }

                                    MyUtils.showToast("Registrace byla úspěšná.", RegistrationActivity.this);


                                } catch (Exception e) {
                                    //MyUtils.showToast("Registrace nebyla úspěšná, zkuste to později", RegistrationActivity.this);
                                    e.printStackTrace();
                                }

                                MyUtils.dissmissDialog(RegistrationActivity.this);

                            }
                        }
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                }


            }
        });

    }

    public String getAndroidVersion() {
        String release = Build.VERSION.RELEASE;
        int sdkVersion = Build.VERSION.SDK_INT;
        return "SDK: " + sdkVersion + " (" + release +")";
    }

}
