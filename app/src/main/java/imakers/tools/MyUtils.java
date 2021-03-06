package imakers.tools;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.loopj.android.http.RequestParams;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.SimpleBitmapDisplayer;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import imakers.beacons.DetailSpotActivity;
import imakers.beacons.R;
import imakers.classes.Campaign;
import imakers.interfaces.MyAsyncLisener;


public class MyUtils {

    public static Logger Log(Activity ac) {
        return LoggerFactory.getLogger(ac.getClass());
    }

    public static Logger Log(Context ac) {
        return LoggerFactory.getLogger(ac.getClass());
    }

    public static void SavePreferences(String key, String value, Context ac) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ac);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public static String LoadPreferences(String key, Context ac) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ac);
        String strSavedMem = preferences.getString(key, "");
        return strSavedMem;
    }


    //parsování JSON
    public static JSONObject getJSONObject(String response, Activity ac) {
        JSONObject obj = null;
        try {

            if (response == null) {
                return null;
            }


            obj = new JSONObject(response);

            if (obj != null) {
                return obj;
            }


            MyUtils.Log(ac).debug("RESPONSE: \n {}", response);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }


    //metody pro zobrazování/skrytí dialogu
    private static ProgressDialog dialog;

    public static void showDialog(final Activity activity, final String text) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {

                dialog = ProgressDialog.show(activity, "", text);
                dialog.setCancelable(true);

            }
        });
    }


    public static void showDialog(final Activity activity) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dialog = ProgressDialog.show(activity, "", activity.getString(R.string.loading_text));
                dialog.setCancelable(true);
            }
        });
    }

    public static void dissmissDialog(final Activity activity) {
        if (dialog != null && activity != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {

                        dialog.dismiss();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    public static void showToast(final String string, final Activity context) {
        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, string, Toast.LENGTH_LONG).show();
            }
        });
    }


    //nastavení pro obrázek
    private static DisplayImageOptions options;

    private static ImageLoader getImageLoader() {

        if (options == null) {
            options = new DisplayImageOptions.Builder()
                    .resetViewBeforeLoading(false)  // default
                    .delayBeforeLoading(200)
                    .cacheInMemory(true) // default
                    .cacheOnDisc(true) // default
                    .considerExifParams(true) // default
                    .imageScaleType(ImageScaleType.IN_SAMPLE_INT) // default
                    .bitmapConfig(Bitmap.Config.RGB_565)
                    .displayer(new SimpleBitmapDisplayer()) // default
                    .handler(new Handler()) // default
                    .build();
        }

        return ImageLoader.getInstance();
    }

    //načítání obrázků
    public static void ImageLoaderLoadImage(String source, ImageView target, Activity ac) {
        if (source.equals("#") || source.isEmpty()) {
            return;
        }
        getImageLoader().displayImage(source, target, options);
    }

    public static void createAction(final Activity activity, Long id, Long actionId) {

        Log.v("kalkub", MyApplication.API_URL+"api/action/" + actionId + "/" + id + "/?hash=" + ((MyApplication) activity.getApplicationContext()).getHash());

        MyHttpClient.get(activity, MyApplication.API_URL+"api/action/" + actionId + "/" + id + "/?hash=" + ((MyApplication) activity.getApplicationContext()).getHash(), new RequestParams(), new MyAsyncLisener() {
            @Override
            public void onComplete(JSONObject data) {
                if (data != null) {
                    Toast.makeText(activity, "DATA: " + data.toString(), Toast.LENGTH_LONG).show();
                }
            }
        });

    }

    public static void sendNotification(Campaign campaign, Context activity) {

        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);


        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(activity)
                        .setContentTitle(campaign.getTitle())
                        .setContentText(campaign.getTitle())
                        .setSmallIcon(R.drawable.ic_launcher);

        builder.setSound(alarmSound);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(activity);
        stackBuilder.addNextIntent(new Intent(activity, DetailSpotActivity.class).putExtra("id", campaign.getId()).putExtra("notify", true).putExtra("name", campaign.getProvider() == null ? "" : campaign.getProvider().getTitle()).putExtra("name_spot", campaign.getTitle()).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(campaign.getId().intValue(), PendingIntent.FLAG_CANCEL_CURRENT);
        builder.setContentIntent(resultPendingIntent);
        builder.setAutoCancel(true);
        NotificationManager notificationManager =
                (NotificationManager) activity.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(campaign.getId().intValue(), builder.build());
    }

}
