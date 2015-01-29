package imakers.tools;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;

import com.loopj.android.http.RequestParams;
import com.pnikosis.materialishprogress.ProgressWheel;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.powersave.BackgroundPowerSaver;
import org.altbeacon.beacon.startup.BootstrapNotifier;
import org.altbeacon.beacon.startup.RegionBootstrap;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import imakers.beacons.R;
import imakers.classes.Campaign;
import imakers.classes.Pair;
import imakers.classes.Spot;
import imakers.classes.SpotInitiation;
import imakers.interfaces.MyAsyncLisener;
import se.emilsjolander.sprinkles.Query;

public class MainService extends Service implements BootstrapNotifier, RangeNotifier {

    ProgressWheel whell;
    Boolean isVisible = false;
    private ArrayList<Campaign> listItems = new ArrayList<Campaign>();
    private ArrayList<Campaign> groupItems = new ArrayList<Campaign>();
    private static final int TOLERANT_EMPTY_BEACON = 0;
    List<Pair> campWaitForAdd = new ArrayList<Pair>();

    private Region mRegion;
    private BackgroundPowerSaver mBackgroundPowerSaver;
    @SuppressWarnings("unused")
    private RegionBootstrap mRegionBootstrap;

    @Override
    public void onCreate() {
        super.onCreate();


        if (((MyApplication) getApplicationContext()).getCurrentSpots() == null) {
            ((MyApplication) getApplicationContext()).setCurrentSpots(new ArrayList<Integer>());
        }

        //nastavování knihovny na hlídání spotů

        mRegion = new Region("com.neogenia.spothill", null, null, null);

        mBackgroundPowerSaver = new BackgroundPowerSaver(getApplicationContext());
        mRegionBootstrap = new RegionBootstrap(this, mRegion);

        //update spot

        if(((MyApplication)getApplicationContext()).getUpdater() == null) {
            ((MyApplication)getApplicationContext()).setUpdater(new Timer());
        }

        try {

            ((MyApplication)getApplicationContext()).getUpdater().cancel();

        } catch (Exception e) {
            e.printStackTrace();
        }

        // todo: move when there are spots only, cancel when no lefr
        ((MyApplication)getApplicationContext()).setUpdater(new Timer());
        ((MyApplication)getApplicationContext()).getUpdater().schedule(new TimerTask() {
            @Override
            public void run() {

                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        String url  = MyApplication.API_URL+"api/updates/?hash="+((MyApplication)getApplicationContext()).getHash();

                        Log.d("URL", url);

                        MyHttpClient.get(url, new RequestParams(), new MyAsyncLisener() {
                            @Override
                            public void onComplete(JSONObject data) {

                                if(data != null) {

                                    Iterator it = data.keys();

                                    while (it.hasNext()) {
                                        try {

                                            String key = (String)it.next();
                                            JSONArray array = data.getJSONArray(key);

                                            for (int i = 0; i < array.length(); i++) {
                                                int number = array.getInt(i);

                                                String[] strings = new String[2];

                                                strings[0] = ""+key;
                                                strings[1] = ""+number;

                                                final HttpRequestTaskUpdate task = new HttpRequestTaskUpdate();

                                                // needed, when used only execute, the doInBackground
                                                // was not triggered
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
                                                    task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, strings);
                                                }else{
                                                    task.execute(strings);
                                                }


                                            }

                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }

                                    }

                                }

                            }
                        });

                    }
                });

            }
        }, 20000, 20000);

    }

    public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {

        List<Beacon> actual = new ArrayList<Beacon>();
        List<String> actualHash = new ArrayList<String>();



        // když nejsou žádné kampaně, protože pro změnu při nějakých akcích používám, tak se projostotu vymažou i spoty
        if (((MyApplication) getApplicationContext()).getCampaigns().size() == 0 && ((MyApplication) getApplicationContext()).getCurrentSpots().size() > 0) {
            ((MyApplication) getApplicationContext()).setCurrentSpots(new ArrayList<Integer>());
        }

        if (beacons.size() > 0) {

            for (Iterator iterator = beacons.iterator(); iterator.hasNext(); ) {
                Beacon beacon = (Beacon) iterator.next();
                String toHash = beacon.getId2() + "." + beacon.getId3() + "." + beacon.getId1();
                actualHash.add(toHash);
                int hash = toHash.hashCode();
                actual.add(beacon);

                Boolean can = true;
                if (((MyApplication) getApplicationContext()).getCurrentSpots().contains(hash)) {
                    can = false;
                    //break;
                }

                if (can) {
                    ((MyApplication) getApplicationContext()).getCurrentSpots().add(hash);
                    new HttpRequestTask().execute(beacon);
                }
            }
        }


        //mazání kampaně po 5 minutách po opuštění spotu
        for (int i = 0; i < listItems.size(); i++) {

            final Campaign cam = listItems.get(i);

            if (cam.getSpot() != null && cam.getSpot().getHash() != null && !actualHash.contains(cam.getSpot().getHash()) && !cam.getIsStarted()) {
                cam.setIsStarted(true);
                cam.setDelete(new Timer());
                final int finalI = i;
                cam.getDelete().schedule(new TimerTask() {
                    @Override
                    public void run() {

                        try {

                            if (cam.getIsStarted()) {
                                listItems.remove(cam);
                                ((MyApplication) getApplicationContext()).getCampaigns().remove(cam);
                                cam.getDelete().cancel();
                                ((MyApplication) getApplicationContext()).getMainActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        ((MyApplication) getApplicationContext()).notificationChange();
                                    }
                                });
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                }, 300000, 10);
            } else if (cam.getSpot() != null && cam.getSpot().getHash() != null && actualHash.contains(cam.getSpot().getHash()) && cam.getIsStarted()) {
                cam.getDelete().cancel();
                cam.setIsStarted(false);
            }
        }


        //mazání group kampaní po 5 minutách
        for (int i = 0; i < groupItems.size(); i++) {

            final Campaign cam = groupItems.get(i);

            if (cam.getSpot() != null && cam.getSpot().getHash() != null && !actualHash.contains(cam.getSpot().getHash()) && !cam.getIsStarted()) {
                cam.setIsStarted(true);
                cam.setDelete(new Timer());
                final int finalI = i;
                cam.getDelete().schedule(new TimerTask() {
                    @Override
                    public void run() {

                        if (cam.getIsStarted()) {
                            groupItems.remove(cam);
                            ((MyApplication) getApplicationContext()).getCampaigns().remove(cam);
                            cam.getDelete().cancel();
                            ((MyApplication) getApplicationContext()).getMainActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    ((MyApplication) getApplicationContext()).notificationChange();
                                }
                            });
                        }

                    }
                }, 300000, 10);
            } else if (cam.getSpot() != null && cam.getSpot().getHash() != null && actualHash.contains(cam.getSpot().getHash()) && cam.getIsStarted()) {
                cam.getDelete().cancel();
                cam.setIsStarted(false);
            }
        }


        //rozřazení eventů s podmínkou
        final List<Pair> event1 = new ArrayList<Pair>();
        final List<Pair> event2 = new ArrayList<Pair>();
        final List<Pair> event3 = new ArrayList<Pair>();

        for (int i = 0; i < campWaitForAdd.size(); i++) {

            switch (campWaitForAdd.get(i).getCampaign().getEvent()) {
                case 1:
                    event1.add(campWaitForAdd.get(i));
                    break;
                case 2:
                    event2.add(campWaitForAdd.get(i));
                    break;
                case 3:
                    event3.add(campWaitForAdd.get(i));
                    break;

            }

        }


        //zde procházím event type = 1
        for (int i = 0; i < actual.size(); i++) {
            Beacon beacon = actual.get(i);
            String toHash = beacon.getId2() + "." + beacon.getId3() + "." + beacon.getId1();


            for (final int j[] = {0}; j[0] < event1.size(); j[0]++) {

                final Pair pair = event1.get(j[0]);

                if (toHash.equals(pair.getHash())) {

                    final Campaign c = pair.getCampaign();


                    //zkoumání podmínky
                    if (c.getShowDistance() >= beacon.getDistance() && pair.getTimer() != null && !pair.getIsStarted()) {
                        pair.setIsStarted(true);
                        final int finalI = i;
                        pair.getTimer().schedule(new TimerTask() {
                            @Override
                            public void run() {

                                if (pair.getIsStarted()) {
                                    ((MyApplication) getApplicationContext()).getMainActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {


                                            c.save();

                                            addCampaignAndList(c);
                                            ((MyApplication) getApplicationContext()).notificationChange();
                                            if (!c.getNotificationTitle().isEmpty()) {
                                                MyUtils.sendNotification(c, ((MyApplication) getApplicationContext()).getMainActivity());
                                            }
                                            pair.getTimer().cancel();
                                            campWaitForAdd.remove(pair);
                                            event1.remove(pair);
                                            j[0]--;
                                        }
                                    });
                                }

                            }
                        }, c.getDelay() * 1000, 10);
                    } else if (pair.getIsStarted()) { // reset
                        pair.setIsStarted(false);
                        pair.getTimer().cancel();
                    }

                }
            }

        }

        //zde zkoumám podmínky pro event type = 2
        for (final int[] i = {0}; i[0] < event2.size(); i[0]++) {
            Double distance = 0D;

            Boolean can = false;
            String badHASH = "";

            for (int j = 0; j < actual.size(); j++) {
                Beacon beacon = actual.get(j);
                String toHash = beacon.getId2() + "." + beacon.getId3() + "." + beacon.getId1();
                if (event2.get(i[0]).getHash().contains(toHash)) {
                    can = true;
                    distance = beacon.getDistance();
                    break;
                } else {
                    badHASH = toHash;
                }

            }

            //zkoumání podmínek (Hlavní IF)
            if ((event2.get(i[0]).getTime() + (event2.get(i[0]).getCampaign().getDelay() * 1000)) < System.currentTimeMillis() && can && event2.get(i[0]).getCampaign().getShowDistance() >= distance) {

                //campaigns1test.add(0, event2.get(i).getCampaign());


                event2.get(i[0]).getCampaign().save();

                addCampaignAndList(event2.get(i[0]).getCampaign());
                final int finalI = i[0];
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        if (!event2.get(finalI).getCampaign().getNotificationTitle().isEmpty()) {
                            MyUtils.sendNotification(event2.get(finalI).getCampaign(), ((MyApplication) getApplicationContext()).getMainActivity());
                        }

                        campWaitForAdd.remove(event2.get(finalI));
                        event2.remove(finalI);
                        i[0]--;
                        ((MyApplication) getApplicationContext()).notificationChange();


                    }
                });


            } else if (!can) {
                if (event2.get(i[0]).getCounter() > TOLERANT_EMPTY_BEACON) {
                    campWaitForAdd.get(campWaitForAdd.lastIndexOf(event2.get(i[0]))).setTime(System.currentTimeMillis()); // reset
                } else {
                    event2.get(i[0]).setCounter(event2.get(i[0]).getCounter() + 1);
                }
            } else {
                if (can) {
                    event2.get(i[0]).setCounter(0);
                }
            }

        }

        //zkoumání podmínek pro event type = 3
        for (final int[] i = {0}; i[0] < event3.size(); i[0]++) {

            final Pair pair = event3.get(i[0]);

            Double distance = 0D;

            Boolean can = false;

            for (int j = 0; j < actual.size(); j++) {
                Beacon beacon = actual.get(j);
                String toHash = beacon.getId2() + "." + beacon.getId3() + "." + beacon.getId1();

                if (pair != null && pair.getHash() != null && pair.getHash().equals(toHash)) {
                    can = true;
                    break;
                }
            }

            if (!can) {
                pair.setCounter(pair.getCounter() + 1);
            }

            // Hlavní if, zjišťuje jestli opustil vážně spot
            if (!can && pair.getTimer() != null && !pair.getIsStarted() && pair.getCounter() > TOLERANT_EMPTY_BEACON) {
                pair.setIsStarted(true);
                final int finalI = i[0];
                try {

                    // bez delay se zavolá hnedka přidávání
                    if (pair.getCampaign().getDelay() == 0) {

                        ((MyApplication) getApplicationContext()).getMainActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {


                                Log.v("kalkub", "4:4 adding");

                                try {

                                    pair.getCampaign().save();

                                    addCampaignAndList(pair.getCampaign());
                                    if (!pair.getCampaign().getNotificationTitle().isEmpty()) {
                                        MyUtils.sendNotification(pair.getCampaign(), ((MyApplication) getApplicationContext()).getMainActivity());
                                    }
                                    ((MyApplication) getApplicationContext()).notificationChange();
                                    pair.setIsStarted(false);
                                    pair.getTimer().cancel();
                                    campWaitForAdd.remove(pair);

                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });

                    } else { // zde je s delayem
                        pair.getTimer().schedule(new TimerTask() {
                            @Override
                            public void run() {

                                if (pair.getIsStarted() != null && pair.getIsStarted()) {

                                    ((MyApplication) getApplicationContext())
                                            .getMainActivity()
                                            .runOnUiThread(
                                                    new Runnable() {
                                                        @Override
                                                        public void run() {


                                                            try {

                                                                Log.v("kalkub", "4:4 adding");

                                                                addCampaignAndList(pair.getCampaign());

                                                                if (!pair.getCampaign().getNotificationTitle().isEmpty()) {
                                                                    MyUtils.sendNotification(pair.getCampaign(), ((MyApplication) getApplicationContext()).getMainActivity());
                                                                }

                                                                ((MyApplication) getApplicationContext()).notificationChange();
                                                                pair.setIsStarted(false);
                                                                pair.getTimer().cancel();
                                                                campWaitForAdd.remove(pair);

                                                            } catch (Exception e) {
                                                                e.printStackTrace();
                                                            }
                                                        }
                                                    }

                                            );
                                }

                            }
                        }, event3.get(i[0]).getCampaign().getDelay() * 1000, 10);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (can && event3.get(i[0]).getIsStarted()) { // reset spuštěného timeru, který přidá kampaň
                event3.get(i[0]).setIsStarted(false);
                event3.get(i[0]).getTimer().cancel();
                event3.get(i[0]).setTimer(new Timer());
                event3.get(i[0]).setCounter(0);
            } else if (can) {
                event3.get(i[0]).setCounter(0);
            }


        }

        //kontroluji duplicitní kampaně
        List<Long> list = new ArrayList<Long>();

        for (int i = 0; i < listItems.size(); i++) {
            list.add(listItems.get(i).getId());
        }
        for (int i = 0; i < groupItems.size(); i++) {
            list.add(groupItems.get(i).getId());
        }

        Collections.sort(list);


        for (int i = 0; i < list.size(); i++) {
            if (i + 1 != list.size()) {
                if (list.get(i) == list.get(i + 1)) {
                    Long id = list.get(i);

                    for (int j = 0; j < groupItems.size(); j++) {
                        if (groupItems.get(j).getId() == id) {
                            ((MyApplication) getApplicationContext()).getCampaigns().remove(groupItems.get(j));
                            groupItems.remove(j);
                            isChange = true;
                            break;
                        }
                    }

                }
            }
        }

        if (isChange) {
            isChange = false;

            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    try {
                        ((MyApplication) getApplicationContext()).getAdapter().notifyDataSetChanged();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

        }


    }

    Boolean isChange = false;



    //NEJHLAVNĚJŠÍ METODA přidávání kampaní
    void init(SpotInitiation spotInitiation) {

        Spot spot = null;


        if (spotInitiation != null) {

            try {

                spot = new Spot(spotInitiation.getMajor(), spotInitiation.getMinor(), System.currentTimeMillis(), spotInitiation.getHashSpot());
                spot.save();

            } catch (Exception e) {
                //e.printStackTrace();
            }

            if (spotInitiation.getCampaigns() != null) {

                Collection<Campaign> campaigns = spotInitiation.getCampaigns();
                for (Iterator iterator = campaigns.iterator(); iterator.hasNext(); ) {
                    final Campaign campaign = (Campaign) iterator.next();

                    Campaign c = Query.one(Campaign.class, "select * from Campaigns where id=?", "" + campaign.getId()).get(); // SQL dotaz na databázy, kde ověřuju jestli jsem tu kampaň už nevyhodnotil

                    if (c != null) {
                        campaign.setEvent(1);
                        campaign.setDelay(0);
                        campaign.setShowDistance(0);
                    }

                    campaign.setIsSeparator(false);
                    campaign.setIsGroupCampaign(false);

                    campaign.setSpot(spot);


                    //Vyhodnocování eventů

                    if (campaign.getEvent() == 1 && campaign.getDelay() == 0 && campaign.getShowDistance() == 0) {

                        campaign.save(); // ukládání do databáze

                        addCampaignToList(campaign);

                        if (!campaign.getNotificationTitle().isEmpty()) {
                            MyUtils.sendNotification(campaign, ((MyApplication) getApplicationContext()).getMainActivity());
                        }


                    } else if (campaign.getEvent() == 2 && campaign.getDelay() == 0 && campaign.getShowDistance() == 0) {

                        campaign.save();

                        addCampaignToList(campaign);

                        if (!campaign.getNotificationTitle().isEmpty()) {
                            MyUtils.sendNotification(campaign, ((MyApplication) getApplicationContext()).getMainActivity());
                        }

                        Log.v("kalkub", "2:0");

                    } else if (campaign.getEvent() == 1 && campaign.getDelay() != 0 && campaign.getShowDistance() == 0) {
                        final Timer t = new Timer();
                        t.schedule(new TimerTask() {
                            @Override
                            public void run() {

                                Log.v("kalkub", "3:1");
                                ((MyApplication) getApplicationContext()).getMainActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {


                                        campaign.save();

                                        addCampaignAndList(campaign);

                                        if (!campaign.getNotificationTitle().isEmpty()) {
                                            MyUtils.sendNotification(campaign, ((MyApplication) getApplicationContext()).getMainActivity());
                                        }

                                        ((MyApplication) getApplicationContext()).notificationChange();
                                        t.cancel();
                                    }

                                });


                            }
                        }, campaign.getDelay() * 1000, 10);


                    } else if (campaign.getEvent() == 3) {
                        campWaitForAdd.add(new Pair(spotInitiation.getHashSpot(), campaign, System.currentTimeMillis()));
                    } else {
                        campWaitForAdd.add(new Pair(spotInitiation.getHashSpot(), campaign, System.currentTimeMillis()));
                    }
                }

            }

            //přidávání lišty modré zubaté
            if (spotInitiation.getGroupCampaigns() != null) {
                Collection<Campaign> groupCampaigns = spotInitiation.getGroupCampaigns();
                for (Iterator iterator = groupCampaigns.iterator(); iterator.hasNext(); ) {
                    Campaign campaign = (Campaign) iterator.next();

                    campaign.setIsSeparator(false);
                    campaign.setIsGroupCampaign(true);
                    campaign.setSpot(spot);

                    addCampaignToGroupList(campaign);

                }
            }


            // zde je právě to mazání kampaňí, protože scan je každých 2s, tak je to rozumné dát sem
            if (((MyApplication) getApplicationContext()).getRemovedCampaign() != null && ((MyApplication) getApplicationContext()).getRemovedCampaign().size() != 0) {
                for (int i = 0; i < ((MyApplication) getApplicationContext()).getRemovedCampaign().size(); i++) {

                    if (listItems.contains(((MyApplication) getApplicationContext()).getRemovedCampaign().get(i))) {
                        listItems.remove(((MyApplication) getApplicationContext()).getRemovedCampaign().get(i));
                    } else {
                        groupItems.remove(((MyApplication) getApplicationContext()).getRemovedCampaign().get(i));
                    }

                    ((MyApplication) getApplicationContext()).getRemovedCampaign().remove(i);
                    i--;
                }
            }

            if (((MyApplication) getApplicationContext()).getRemovedCampaign() != null) {
                ((MyApplication) getApplicationContext()).getRemovedCampaign().clear();
            }

            ((MyApplication) getApplicationContext()).getCampaigns().clear();

            ((MyApplication) getApplicationContext()).getCampaigns().addAll(listItems);

            if (groupItems.size() != 0) {
                Campaign c = new Campaign();
                c.setIsSeparator(true);
                c.setIsGroupCampaign(false);
                c.setType(-2);
                c.setSpot(new Spot(-1, -1, 0L));
                ((MyApplication) getApplicationContext()).getCampaigns().add(c);
            }


            ((MyApplication) getApplicationContext()).getCampaigns().addAll(groupItems);
            if (((MyApplication) getApplicationContext()).getCampaigns().size() != 0 && ((MyApplication) getApplicationContext()).getSpotView() != null) {
                ((MyApplication) getApplicationContext()).getSpotView().findViewById(R.id.place_holder_text).setVisibility(View.VISIBLE);
            } else if (((MyApplication) getApplicationContext()).getSpotView() != null) {
                ((MyApplication) getApplicationContext()).getSpotView().findViewById(R.id.place_holder_text).setVisibility(View.GONE);
            }


            ((MyApplication) getApplicationContext()).notificationChange();

        }
    }

    // Všechno to jsou metody na přidávání kampaňí kde se hlídají duplicity

    void addCampaignToList(Campaign campaign) {

        Boolean can = true;

        for (int i = 0; i < listItems.size(); i++) {
            if (listItems.get(i).getId() == campaign.getId()) {
                can = false;
            }
        }

        if (can) {
            listItems.add(campaign);
        }
    }

    void addCampaignToGroupList(Campaign campaign) {

        Boolean can = true;

        for (int i = 0; i < listItems.size(); i++) {
            if (listItems.get(i).getId() == campaign.getId()) {
                can = false;
            }
        }

        if (can) {
            groupItems.add(campaign);
        }
    }

    void addCampaign(Campaign campaign) {
        Boolean can = true;

        for (int i = 0; i < ((MyApplication) getApplicationContext()).getCampaigns().size(); i++) {
            if (((MyApplication) getApplicationContext()).getCampaigns().get(i).getId() == campaign.getId()) {
                can = false;
            }
        }

        if (can) {
            ((MyApplication) getApplicationContext()).getCampaigns().add(campaign);
        }

    }

    void addCampaignAndList(Campaign campaign) {
        Boolean can = true;

        for (int i = 0; i < ((MyApplication) getApplicationContext()).getCampaigns().size(); i++) {
            if (((MyApplication) getApplicationContext()).getCampaigns().get(i).getId() != null && ((MyApplication) getApplicationContext()).getCampaigns().get(i).getId().equals(campaign.getId())) {
                ((MyApplication) getApplicationContext()).getCampaigns().remove(i);
                i--;
            }
        }

        if (can) {
            ((MyApplication) getApplicationContext()).getCampaigns().add(0, campaign);
        }

        can = true;

        for (int i = 0; i < listItems.size(); i++) {
            if (listItems.get(i).getId().equals(campaign.getId())) {
                listItems.remove(i);
                i--;
            }
        }

        if (can) {
            listItems.add(0, campaign);
        }

        for (int i = 0; i < listItems.size(); i++) {
            for (int j = 0; j < groupItems.size(); j++) {
                if (listItems.get(i).getId().equals(groupItems.get(j).getId())) {
                    groupItems.remove(j);
                    j--;
                }
            }
        }

        for (int i = 0; i < listItems.size(); i++) {
            for (int j = 0; j < ((MyApplication) getApplicationContext()).getCampaigns().size(); j++) {

                if (((MyApplication) getApplicationContext()).getCampaigns().get(j).getId() != null && ((MyApplication) getApplicationContext()).getCampaigns().get(j).getIsGroupCampaign() && ((MyApplication) getApplicationContext()).getCampaigns().get(j).getId().equals(listItems.get(i).getId())) {
                    ((MyApplication) getApplicationContext()).getCampaigns().remove(j);
                    j--;
                }

            }
        }

        ((MyApplication) getApplicationContext()).getMainActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {

                if (((MyApplication) getApplicationContext()).getCampaigns().size() == 0 && ((MyApplication) getApplicationContext()).getSpotView() != null) {
                    ((MyApplication) getApplicationContext()).getSpotView().findViewById(R.id.place_holder_text).setVisibility(View.VISIBLE);
                } else {
                    ((MyApplication) getApplicationContext()).getSpotView().findViewById(R.id.place_holder_text).setVisibility(View.GONE);
                }

                ((MyApplication) getApplicationContext()).notificationChange();
            }
        });

    }

    @Override
    public void didEnterRegion(Region region) {
        try {
            //Log.d(TAG, "entered region.  starting ranging");
            (((MyApplication)getApplicationContext()).getBeaconManager()).startRangingBeaconsInRegion(mRegion);
            (((MyApplication)getApplicationContext()).getBeaconManager()).setRangeNotifier(this);
            (((MyApplication)getApplicationContext()).getBeaconManager()).updateScanPeriods();
        } catch (RemoteException e) {
            //Log.e(TAG, "Cannot start ranging");
        }
    }

    @Override
    public void didExitRegion(Region region) {

    }

    @Override
    public void didDetermineStateForRegion(int i, Region region) {

    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    class HttpRequestTask extends AsyncTask<Beacon, Void, SpotInitiation> {
        @Override
        protected SpotInitiation doInBackground(Beacon... params) {
            try {

                ((MyApplication) getApplicationContext()).getMainActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //isVisible = true;

                        //whell.setVisibility(View.VISIBLE);

                    }
                });
                Beacon beacon = params[0];
                //MyUtils.showDialog(getMyActivity());

                String url = "";

                url = MyApplication.API_URL+"api/spot/" + beacon.getId2().toString() +
                        "/" + beacon.getId3().toString() + "/?hash=" + ((MyApplication) getApplicationContext()).getHash();

                RestTemplate restTemplate = new RestTemplate();

                Log.v("kalkub", url);

                restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
                SpotInitiation spotInitiation = restTemplate.getForObject(url, SpotInitiation.class);

                String toHash = beacon.getId2() + "." + beacon.getId3() + "." + beacon.getId1();

                spotInitiation.setMajor(beacon.getId2().toInt());
                spotInitiation.setMinor(beacon.getId3().toInt());

                spotInitiation.setHashSpot(toHash);

                return spotInitiation;
            } catch (Exception e) {
            }
            return null;
        }

        @Override
        protected void onPostExecute(SpotInitiation spotInitiation) {

            //MyUtils.dissmissDialog(getMyActivity());

            init(spotInitiation);

        }
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {

        ((MyApplication) getApplicationContext()).setCurrentSpots(new ArrayList<Integer>());


        super.onTaskRemoved(rootIntent);
    }

    //helper class for updater

    class HttpRequestTaskUpdate extends AsyncTask<String[], Void, SpotInitiation> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();


        }

        @Override
        protected SpotInitiation doInBackground(String[]... params) {
            try {

                ((MyApplication) getApplicationContext()).getMainActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //isVisible = true;

                        //whell.setVisibility(View.VISIBLE);

                    }
                });
                String[] beacon = params[0];
                //MyUtils.showDialog(getMyActivity());

                String url = "";

                url = MyApplication.API_URL+"api/spot/" + beacon[0] +
                        "/" + beacon[1] + "/?hash=" + ((MyApplication) getApplicationContext()).getHash();

                RestTemplate restTemplate = new RestTemplate();

                Log.v("kalkub", url);

                restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
                SpotInitiation spotInitiation = restTemplate.getForObject(url, SpotInitiation.class);

                String toHash = beacon[0] + "." + beacon[1] + ".";//beacon.getId1();

                spotInitiation.setMajor(Integer.valueOf(beacon[0]));
                spotInitiation.setMinor(Integer.valueOf(beacon[1]));

                spotInitiation.setHashSpot(toHash);

                return spotInitiation;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(SpotInitiation spotInitiation) {
            initUpdate(spotInitiation);

        }
    }


    //update method
    void initUpdate(SpotInitiation init) {


        List<Campaign> campaigns = new ArrayList<>(((MyApplication) getApplicationContext()).getCampaigns());


        // delete all capmains from spot
        for (int i = 0; i < campaigns.size(); i++) {
            Campaign c = campaigns.get(i);

            if (c.getSpot() != null && c.getSpot().getMajor() != null && c.getSpot().getMinor() != null && init.getCampaigns() != null) {
                if (c.getSpot().getMajor().intValue() == init.getMajor().intValue() && c.getSpot().getMinor().intValue() == init.getMinor().intValue()) {
                    listItems.remove(c);
                    campaigns.remove(i);

                }
            }

        }

        // add new campaints
        for (Iterator iterator = init.getCampaigns().iterator(); iterator.hasNext(); ) {
            final Campaign campaign = (Campaign) iterator.next();

            campaign.setIsGroupCampaign(false);
            campaign.setIsSeparator(false);

            listItems.add(campaign);
            ((MyApplication) getApplication()).getCampaigns().add(0, campaign);

        }

    }



}
