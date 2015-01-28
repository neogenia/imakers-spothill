package imakers.tools;

import android.app.Activity;
import android.app.Application;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.view.View;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.utils.StorageUtils;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.powersave.BackgroundPowerSaver;
import org.altbeacon.beacon.startup.BootstrapNotifier;
import org.altbeacon.beacon.startup.RegionBootstrap;
import org.json.JSONObject;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import imakers.adapters.SpotAdapter;
import imakers.beacons.DetailSpotActivity;
import imakers.beacons.MainActivity;
import imakers.beacons.R;
import imakers.classes.Campaign;
import imakers.classes.ChangeClass;
import imakers.classes.Pair;
import imakers.classes.Spot;
import imakers.classes.SpotInitiation;
import imakers.fragments.HomeFragment;
import se.emilsjolander.sprinkles.Migration;
import se.emilsjolander.sprinkles.Query;
import se.emilsjolander.sprinkles.Sprinkles;

public class MyApplication extends Application {

    public enum TrackerName {
        APP_TRACKER, // Tracker used only in this app.
        GLOBAL_TRACKER, // Tracker used by all the apps from a company. eg: roll-up tracking.
        ECOMMERCE_TRACKER, // Tracker used by all ecommerce transactions from a company.
    }
    BeaconManager beaconManager;
    HashMap<TrackerName, Tracker> mTrackers = new HashMap<TrackerName, Tracker>();
    private ArrayList<Campaign> removedCampaign;
    private List<Integer> currentSpots = new ArrayList<Integer>();
    private Activity mainActivity;
    @SuppressWarnings("unused")
    private RegionBootstrap mRegionBootstrap;
    private String testBeacon;
    private Boolean isChange;
    private List<ChangeClass> change;
    private List<Campaign> campaigns;
    private SpotAdapter adapter;
    private View spotView;
    private Timer updater;

    public BeaconManager getBeaconManager() {

        if(beaconManager == null) {
            createBeaconManager();
        }

        return beaconManager;
    }

    public void setBeaconManager(BeaconManager beaconManager) {
        this.beaconManager = beaconManager;
    }

    public Timer getUpdater() {
        return updater;
    }

    public void setUpdater(Timer updater) {
        this.updater = updater;
    }

    public List<ChangeClass> getChange() {
        return change;
    }

    public void setChange(List<ChangeClass> change) {
        this.change = change;
    }

    public View getSpotView() {
        return spotView;
    }

    public void setSpotView(View spotView) {
        this.spotView = spotView;
    }

    public SpotAdapter getAdapter() {
        return adapter;
    }

    public void setAdapter(SpotAdapter adapter) {
        this.adapter = adapter;
    }

    public List<Campaign> getCampaigns() {
        return campaigns;
    }

    public void setCampaigns(List<Campaign> campaigns) {
        this.campaigns = campaigns;
    }

    public Boolean getIsChange() {

        if (isChange == null)
            return false;

        return isChange;
    }

    public Activity getMainActivity() {
        return mainActivity;
    }

    public void setMainActivity(Activity mainActivity) {
        this.mainActivity = mainActivity;
    }

    public void setIsChange(Boolean isChange) {
        this.isChange = isChange;
    }

    public ArrayList<Campaign> getRemovedCampaign() {
        return removedCampaign;
    }

    public void setRemovedCampaign(ArrayList<Campaign> removedCampaign) {
        this.removedCampaign = removedCampaign;
    }

    String hash;

    Boolean canHide;

    public Boolean getCanHide() {
        return canHide;
    }

    public void setCanHide(Boolean canHide) {
        this.canHide = canHide;
    }

    public String getHash() {

        if (hash == null) {
            try {

                hash = new JSONObject(MyUtils.LoadPreferences("login_json", getApplicationContext())).getString("hash");

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public void notificationChange() {
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    public List<Integer> getCurrentSpots() {
        return currentSpots;
    }

    public void setCurrentSpots(List<Integer> currentSpots) {
        this.currentSpots = currentSpots;
    }

    @Override
    public void onCreate() {
        super.onCreate();


        //Inicializace pro knihovnu na načítíání obrázkků
        File cacheDir = StorageUtils.getCacheDirectory(this);
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this)
                .memoryCacheExtraOptions(480, 800) // default = device screen dimensions
                .diskCacheExtraOptions(480, 800, null)
                .threadPoolSize(3) // default
                .threadPriority(Thread.NORM_PRIORITY - 1) // default
                .tasksProcessingOrder(QueueProcessingType.FIFO) // default
                .denyCacheImageMultipleSizesInMemory()
                .memoryCache(new LruMemoryCache(2 * 1024 * 1024))
                .memoryCacheSize(2 * 1024 * 1024)
                .memoryCacheSizePercentage(20) // default
                .diskCache(new UnlimitedDiscCache(cacheDir)) // default
                .diskCacheSize(50 * 1024 * 1024)
                .diskCacheFileCount(100)
                .build();

        ImageLoader.getInstance().init(config);

        setCampaigns(new ArrayList<Campaign>());

        //nastavení scan delay

        createBeaconManager();

        //Knihovna pro databázy s tvorbou tabulek
        Sprinkles sprinkles = Sprinkles.init(getApplicationContext());
        sprinkles.addMigration(new Migration() {
            @Override
            protected void onPreMigrate() {
                // do nothing
            }

            @Override
            protected void doMigration(SQLiteDatabase db) {
                db.execSQL(
                        "CREATE TABLE Spots (" +
                                "" +
                                "major INTEGER PRIMARY KEY," +
                                "minor INTEGER," +
                                "hash TEXT," +
                                "time INTEGER" +
                                ")"
                );

                db.execSQL(
                        "CREATE TABLE Campaigns (" +
                                "" +
                                "id INTEGER PRIMARY KEY" +
                                ")"
                );
            }

            @Override
            protected void onPostMigrate() {
                // do nothing
            }
        });

        stopService(new Intent(this, MainService.class));
        startService(new Intent(this, MainService.class));

    }



    synchronized Tracker getTracker(TrackerName trackerId) {
        if (!mTrackers.containsKey(trackerId)) {

            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            Tracker t = (trackerId == TrackerName.APP_TRACKER) ? analytics.newTracker("UA-52176464-3")
                    : (trackerId == TrackerName.GLOBAL_TRACKER) ? analytics.newTracker(R.xml.global_tracker)
                    : analytics.newTracker(R.xml.global_tracker);
            mTrackers.put(trackerId, t);

        }
        return mTrackers.get(trackerId);
    }


    public void createBeaconManager() {

        beaconManager = BeaconManager.getInstanceForApplication(this);
        beaconManager.setBackgroundScanPeriod(2000l);
        //beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:0-3=4c000215,i:4-19,i:20-21,i:22-23,p:24-24"));
        beaconManager.debug = true;

    }


}
