package imakers.beacons;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EdgeEffect;
import android.widget.EditText;

import com.loopj.android.http.RequestParams;
import com.sromku.simple.fb.Permission;
import com.sromku.simple.fb.SimpleFacebook;
import com.sromku.simple.fb.SimpleFacebookConfiguration;
import com.sromku.simple.fb.entities.Profile;
import com.sromku.simple.fb.listeners.OnLoginListener;
import com.sromku.simple.fb.listeners.OnProfileListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;
import imakers.interfaces.MyAsyncLisener;
import imakers.tools.MyActionPanel;
import imakers.tools.MyApplication;
import imakers.tools.MyHttpClient;
import imakers.tools.MyHttpClientJSON;
import imakers.tools.MyUtils;


public class LoginActivity extends Activity {

    private MyActionPanel panel;
    Boolean isStart;
    private SimpleFacebook mSimpleFacebook;
    Boolean isLogout;


    //Tyto metody obsahuje knihovna na FB je to listener na dostání informací z profilu
    OnProfileListener listener = new OnProfileListener() {
        @Override
        public void onComplete(Profile response) {

            final JSONObject object = new JSONObject();

            try {

                object.put("name", response.getFirstName());
                object.put("surname", response.getLastName());
                object.put("email", response.getEmail());
                object.put("sex", response.getGender().toLowerCase().contains("male") ? 0:1);
                object.put("hash", ((MyApplication)getApplicationContext()).getHash());

                String androidID = Settings.Secure.getString(getContentResolver(),
                        Settings.Secure.ANDROID_ID);

                JSONObject device = new JSONObject();

                device.put("hash", androidID);
                device.put("type", 1);
                device.put("system", getAndroidVersion());
                device.put("manufacturer", ""+Build.MANUFACTURER);
                device.put("model", ""+Build.MODEL);

                object.put("device", device);

                //Volám metodu setup na API a při OK response zapisuji do paměti
                MyHttpClientJSON.post(LoginActivity.this, "setup", object, new MyAsyncLisener() {
                    @Override
                    public void onComplete(JSONObject data) {


                        if(data != null) {

                            MyUtils.SavePreferences("login", "true", LoginActivity.this);
                            MyUtils.SavePreferences("login_json", object.toString(), LoginActivity.this);

                        }

                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        @Override
        public void onException(Throwable throwable) {

        }

        @Override
        public void onThinking() {

        }

        @Override
        public void onFail(String reason) {

        }
    };

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        // permission k FB

        Permission[] permissions = new Permission[] {
                Permission.EMAIL
        };

        //Nastavování FB
        SimpleFacebookConfiguration configuration = new SimpleFacebookConfiguration.Builder()
                .setAppId(getString(R.string.app_id))
                .setNamespace("")
                .setPermissions(permissions)
                .build();

        SimpleFacebook.setConfiguration(configuration);

        isStart = getIntent().getBooleanExtra("isStart", false);
        isLogout = getIntent().getBooleanExtra("logout", false);

        panel = new MyActionPanel(this, getString(R.string.login_title));

        if(isStart || isLogout) {
            panel.hideBack();
        }


        findViewById(R.id.facebook).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSimpleFacebook.login(new OnLoginListener() {
                    @Override
                    public void onLogin() {

                        //Přihlásím se k uživateli

                        MyUtils.showDialog(LoginActivity.this);

                        JSONObject object = new JSONObject();

                        try {

                            String androidID = Settings.Secure.getString(getContentResolver(),
                                    Settings.Secure.ANDROID_ID);

                            JSONObject device = new JSONObject();

                            device.put("hash", androidID);
                            device.put("type", 1);
                            device.put("system", getAndroidVersion());
                            device.put("manufacturer", ""+Build.MANUFACTURER);
                            device.put("model", ""+Build.MODEL);

                            object.put("device", device);

                            object.put("token", mSimpleFacebook.getSession().getAccessToken());

                            if(((MyApplication)getApplicationContext()).getHash() != null) {
                                object.put("hash", ((MyApplication)getApplicationContext()).getHash());
                            }

                            MyHttpClientJSON.post(LoginActivity.this, "token-login", object, new MyAsyncLisener() {
                                @Override
                                public void onComplete(JSONObject data) {

                                    if(data != null) {

                                        try {

                                            ((MyApplication)getApplicationContext()).setHash(data.getString("hash"));
                                            if(data.getString("name").isEmpty()) {
                                                MyUtils.SavePreferences("login", "true", LoginActivity.this);
                                                MyUtils.SavePreferences("login_json", data.toString(), LoginActivity.this);
                                                mSimpleFacebook.getProfile(listener);
                                            }
                                            else {
                                                MyUtils.SavePreferences("login", "true", LoginActivity.this);
                                                MyUtils.SavePreferences("login_json", data.toString(), LoginActivity.this);
                                            }
                                            MyUtils.dissmissDialog(LoginActivity.this);

                                            if(isStart) {
                                                if(MyUtils.LoadPreferences("tutorial", LoginActivity.this).isEmpty()) {
                                                    startActivity(new Intent(LoginActivity.this, TutorialActivity.class));
                                                }
                                                else {
                                                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                                }
                                                finish();
                                            }
                                            else {
                                                //startActivity(new Intent(LoginActivity.this, MenuActivity.class));
                                                finish();
                                            }

                                        } catch (Exception e) {
                                            e.printStackTrace();
                                            MyUtils.dissmissDialog(LoginActivity.this);
                                        }

                                    }
                                    else {
                                        MyUtils.dissmissDialog(LoginActivity.this);
                                    }

                                }
                            });

                        } catch (Exception e) {
                            e.printStackTrace();
                            MyUtils.dissmissDialog(LoginActivity.this);
                        }

                    }

                    @Override
                    public void onNotAcceptingPermissions(Permission.Type type) {
                        Crouton.makeText(LoginActivity.this, getString(R.string.fb_login_failed), Style.ALERT).show();
                    }

                    @Override
                    public void onThinking() {

                    }

                    @Override
                    public void onException(Throwable throwable) {
                        Crouton.makeText(LoginActivity.this, getString(R.string.fb_login_failed), Style.ALERT).show();

                    }

                    @Override
                    public void onFail(String s) {
                        Crouton.makeText(LoginActivity.this, getString(R.string.fb_login_failed), Style.ALERT).show();

                    }
                });
            }
        });

        //registrace
        findViewById(R.id.registration_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegistrationActivity.class).putExtra("isStart", isStart));
            }
        });


        //přihlašování annonymního uživatele
        findViewById(R.id.without).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyUtils.showDialog(LoginActivity.this);

                JSONObject object = new JSONObject();


                try {

                    object.put("name", "");
                    object.put("surname", "");
                    object.put("email", "");
                    object.put("sex", 0);
                    object.put("password", "");

                    String androidID = Settings.Secure.getString(getContentResolver(),
                            Settings.Secure.ANDROID_ID);

                    JSONObject device = new JSONObject();

                    device.put("hash", androidID);
                    device.put("type", 1);
                    device.put("system", getAndroidVersion());
                    device.put("manufacturer", ""+Build.MANUFACTURER);
                    device.put("model", ""+Build.MODEL);

                    object.put("device", device);

                    MyHttpClientJSON.post(LoginActivity.this, "register", object, new MyAsyncLisener() {
                        @Override
                        public void onComplete(JSONObject data) {
                            if(data != null) {

                                try {

                                    ((MyApplication)getApplicationContext()).setHash(data.getString("hash"));

                                    MyUtils.SavePreferences("login", "false", LoginActivity.this);
                                    MyUtils.SavePreferences("login_json", data.toString(), LoginActivity.this);


                                    if(isStart) {
                                        if(MyUtils.LoadPreferences("tutorial", LoginActivity.this).isEmpty()) {
                                            startActivity(new Intent(LoginActivity.this, TutorialActivity.class));
                                        }
                                        else {
                                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                        }
                                        finish();
                                    }
                                    else {
                                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                        finish();
                                    }



                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                                MyUtils.dissmissDialog(LoginActivity.this);

                            }
                        }
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        //Přihlášení na základě uživatelských údajů
        findViewById(R.id.login_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String pass = ((EditText)findViewById(R.id.password)).getText().toString();
                String username = ((EditText)findViewById(R.id.email)).getText().toString();

                if(!pass.isEmpty() || !username.isEmpty()) {
                    MyUtils.showDialog(LoginActivity.this);
                    JSONObject object = new JSONObject();

                    try {
                        object.put("password", pass);
                        object.put("username", username);

                        JSONObject device = new JSONObject();

                        String androidID = Settings.Secure.getString(getContentResolver(),
                                Settings.Secure.ANDROID_ID);

                        device.put("hash", androidID);
                        device.put("type", 1);
                        device.put("system", getAndroidVersion());
                        device.put("manufacturer", ""+ Build.MANUFACTURER);
                        device.put("model", ""+Build.MODEL);

                        object.put("device", device);

                        MyHttpClientJSON.post(LoginActivity.this, "login", object, new MyAsyncLisener() {
                            @Override
                            public void onComplete(JSONObject data) {

                                try {
                                   ((MyApplication)getApplicationContext()).setHash(data.getString("hash"));

                                    MyUtils.SavePreferences("login", "true", LoginActivity.this);
                                    MyUtils.SavePreferences("login_json", data.toString(), LoginActivity.this);

                                    if(isStart) {
                                        if(MyUtils.LoadPreferences("tutorial", LoginActivity.this).isEmpty()) {
                                            startActivity(new Intent(LoginActivity.this, TutorialActivity.class));
                                        }
                                        else {
                                            MyUtils.SavePreferences("login", "true", LoginActivity.this);
                                            MyUtils.SavePreferences("login_json", data.toString(), LoginActivity.this);
                                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                        }
                                        finish();
                                    }
                                    else {
                                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                        finish();
                                    }

                                } catch (Exception e) {

                                    MyUtils.showToast(getString(R.string.wrong_log_in), LoginActivity.this);

                                    e.printStackTrace();
                                }

                            }
                        });

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    MyUtils.dissmissDialog(LoginActivity.this);
                }
                else {
                    MyUtils.showToast(getString(R.string.wrong_log_in), LoginActivity.this);
                }

            }
        });

    }

    public String getAndroidVersion() {
        String release = Build.VERSION.RELEASE;
        int sdkVersion = Build.VERSION.SDK_INT;
        return "SDK: " + sdkVersion + " (" + release +")";
    }

    @Override
    public void onBackPressed() {


        //když jsem se odhlásil nebo nemám ani annonymního uživatele
        if(!isStart && !isLogout) {
            super.onBackPressed();
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        mSimpleFacebook = SimpleFacebook.getInstance(this);
    }


    //důležitá metoda pro FB knihovnu
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        mSimpleFacebook.onActivityResult(this, requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

}
