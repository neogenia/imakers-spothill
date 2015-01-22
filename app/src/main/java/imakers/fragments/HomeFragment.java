package imakers.fragments;


import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.fortysevendeg.swipelistview.BaseSwipeListViewListener;
import com.fortysevendeg.swipelistview.SwipeListView;
import com.google.gson.Gson;
import com.loopj.android.http.RequestParams;
import com.pnikosis.materialishprogress.ProgressWheel;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.Region;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import imakers.adapters.SpotAdapter;
import imakers.beacons.DetailSpotActivity;
import imakers.beacons.R;
import imakers.classes.Campaign;
import imakers.classes.ChangeClass;
import imakers.classes.Pair;
import imakers.classes.Spot;
import imakers.classes.SpotInitiation;
import imakers.interfaces.MyAsyncLisener;
import imakers.tools.MainService;
import imakers.tools.MyApplication;
import imakers.tools.MyHttpClient;
import imakers.tools.MyUtils;
import se.emilsjolander.sprinkles.ManyQuery;
import se.emilsjolander.sprinkles.Query;

public class HomeFragment extends Fragment {

    ProgressWheel whell;
    Boolean isVisible = false;
    int openItem = -1;
    //private List<Integer> currentSpots;
    private ArrayList<Campaign> listItems = new ArrayList<Campaign>();
    private ArrayList<Campaign> groupItems = new ArrayList<Campaign>();
    private SwipeListView swipeListView;
    SpotAdapter test1adapter;
    List<Campaign> campaigns1test = new ArrayList<Campaign>();
    List<Pair> campWaitForAdd = new ArrayList<Pair>();
    View v;
    int index = 0;
    Activity activity;
    Timer t;
    private static final int TOLERANT_EMPTY_BEACON = 4;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.home_layout, null);
        ((MyApplication)getMyActivity().getApplicationContext()).setSpotView(v);


        whell = (ProgressWheel) v.findViewById(R.id.progress_wheel);


        //zde je ignorování spotů dle délky ignorace, kterou mají nastavenou
        List<Spot> currentSpots = Query.all(Spot.class).get().asList();
        for (int i = 0; i < currentSpots.size(); i++) {
            if(System.currentTimeMillis() >= currentSpots.get(i).getTime()) {
                currentSpots.get(i).delete();
                currentSpots.remove(i);
                i--;
            }
        }


        ((MyApplication)getMyActivity().getApplicationContext()).setSpotView(v);
        swipeListView = (SwipeListView) v.findViewById(R.id.listview);
        ((MyApplication)getMyActivity().getApplicationContext()).setAdapter(new SpotAdapter(getMyActivity(), R.layout.menu_item_spot_layout, ((MyApplication)getMyActivity().getApplicationContext()).getCampaigns(), swipeListView, "home"));

        swipeListView.setAdapter(((MyApplication)getMyActivity().getApplicationContext()).getAdapter());

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(600);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                getMyActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (swipeListView.getAdapter().getCount() == 0) {
                            v.findViewById(R.id.place_holder_text).setVisibility(View.VISIBLE);
                        } else {
                            v.findViewById(R.id.place_holder_text).setVisibility(View.GONE);
                        }
                    }
                });
            }
        }).start();


        swipeListView.setSwipeListViewListener(new BaseSwipeListViewListener() {

            @Override
            public int onChangeSwipeMode(int position) {

                if (((MyApplication)getMyActivity().getApplicationContext()).getCampaigns().get(position).getIsGroupCampaign() || ((MyApplication)getMyActivity().getApplicationContext()).getCampaigns().get(position).getIsSeparator()) {
                    return SwipeListView.SWIPE_MODE_NONE;
                } else {
                    return SwipeListView.SWIPE_MODE_BOTH;
                }
            }

            @Override
            public void onOpened(int position, boolean toRight) {
                if (((MyApplication)getMyActivity().getApplicationContext()).getCampaigns().get(position).getIsGroupCampaign() || ((MyApplication)getMyActivity().getApplicationContext()).getCampaigns().get(position).getIsSeparator()) {
                    swipeListView.closeAnimate(position);
                }

                try {
                    if (toRight) {
                        getViewByPosition(position, swipeListView).findViewById(R.id.swipe_alarm).setVisibility(View.INVISIBLE);
                        getViewByPosition(position, swipeListView).findViewById(R.id.swipe_heart).setVisibility(View.INVISIBLE);
                        getViewByPosition(position, swipeListView).findViewById(R.id.swipe_delete).setVisibility(View.INVISIBLE);
                        getViewByPosition(position, swipeListView).findViewById(R.id.swipe_share).setVisibility(View.VISIBLE);
                    } else {
                        getViewByPosition(position, swipeListView).findViewById(R.id.swipe_share).setVisibility(View.INVISIBLE);
                        getViewByPosition(position, swipeListView).findViewById(R.id.swipe_alarm).setVisibility(View.VISIBLE);
                        getViewByPosition(position, swipeListView).findViewById(R.id.swipe_heart).setVisibility(View.VISIBLE);
                        getViewByPosition(position, swipeListView).findViewById(R.id.swipe_delete).setVisibility(View.VISIBLE);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onClosed(int position, boolean fromRight) {
                ((MyApplication)getMyActivity().getApplicationContext()).getCampaigns().get(position).setIsSeparatorEnable(false);
                try {

                    getViewByPosition(position, swipeListView).findViewById(R.id.red_separate).setVisibility(View.GONE);

                    getViewByPosition(position, swipeListView).findViewById(R.id.swipe_share).setVisibility(View.VISIBLE);
                    getViewByPosition(position, swipeListView).findViewById(R.id.swipe_alarm).setVisibility(View.VISIBLE);
                    getViewByPosition(position, swipeListView).findViewById(R.id.swipe_heart).setVisibility(View.VISIBLE);
                    getViewByPosition(position, swipeListView).findViewById(R.id.swipe_delete).setVisibility(View.VISIBLE);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onListChanged() {
            }

            @Override
            public void onMove(int position, float x) {
                if (((MyApplication)getMyActivity().getApplicationContext()).getCampaigns().get(position).getIsGroupCampaign() || ((MyApplication)getMyActivity().getApplicationContext()).getCampaigns().get(position).getIsSeparator()) {
                    swipeListView.closeAnimate(position);
                }
            }

            @Override
            public void onStartOpen(int position, int action, boolean right) {
                Log.d("kalkub", String.format("onStartOpen %d - action %d", position, action));

                if (right) {
                    getViewByPosition(position, swipeListView).findViewById(R.id.swipe_alarm).setVisibility(View.INVISIBLE);
                    getViewByPosition(position, swipeListView).findViewById(R.id.swipe_heart).setVisibility(View.INVISIBLE);
                    getViewByPosition(position, swipeListView).findViewById(R.id.swipe_delete).setVisibility(View.INVISIBLE);
                    getViewByPosition(position, swipeListView).findViewById(R.id.swipe_share).setVisibility(View.VISIBLE);
                } else {
                    getViewByPosition(position, swipeListView).findViewById(R.id.swipe_share).setVisibility(View.INVISIBLE);
                    getViewByPosition(position, swipeListView).findViewById(R.id.swipe_alarm).setVisibility(View.VISIBLE);
                    getViewByPosition(position, swipeListView).findViewById(R.id.swipe_heart).setVisibility(View.VISIBLE);
                    getViewByPosition(position, swipeListView).findViewById(R.id.swipe_delete).setVisibility(View.VISIBLE);
                }


                if (((MyApplication)getMyActivity().getApplicationContext()).getCampaigns().get(position).getIsGroupCampaign() || ((MyApplication)getMyActivity().getApplicationContext()).getCampaigns().get(position).getIsSeparator()) {
                    swipeListView.closeAnimate(position);
                }

                if (openItem > -1) {
                    swipeListView.closeAnimate(openItem);
                }

                openItem = position;

            }

            @Override
            public void onStartClose(int position, boolean right) {
                Log.d("kalkub", String.format("onStartClose %d", position));

                try {

                    getViewByPosition(position, swipeListView).findViewById(R.id.red_separate).setVisibility(View.GONE);

                    getViewByPosition(position, swipeListView).findViewById(R.id.swipe_share).setVisibility(View.VISIBLE);
                    getViewByPosition(position, swipeListView).findViewById(R.id.swipe_alarm).setVisibility(View.VISIBLE);
                    getViewByPosition(position, swipeListView).findViewById(R.id.swipe_heart).setVisibility(View.VISIBLE);
                    getViewByPosition(position, swipeListView).findViewById(R.id.swipe_delete).setVisibility(View.VISIBLE);

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onClickFrontView(int position) {
                //Log.d("swipe", String.format("onClickFrontView %d", position));
                try {

                    if (((MyApplication)getMyActivity().getApplicationContext()).getCampaigns().get(position).getIsGroupCampaign() || ((MyApplication)getMyActivity().getApplicationContext()).getCampaigns().get(position).getIsSeparator()) {
                        swipeListView.closeAnimate(position);
                    }

                    if (!((MyApplication)getMyActivity().getApplicationContext()).getCampaigns().get(position).getIsSeparator()) {
                        if (((MyApplication)getMyActivity().getApplicationContext()).getCampaigns().get(position).getIsGroupCampaign() && ((MyApplication)getMyActivity().getApplicationContext()).getCampaigns().get(position).getAllowDetail() == 1) {
                            startActivity(new Intent(getMyActivity(), DetailSpotActivity.class).putExtra("id", ((MyApplication)getMyActivity().getApplicationContext()).getCampaigns().get(position).getId()).putExtra("name", "").putExtra("name_spot", ((MyApplication)getMyActivity().getApplicationContext()).getCampaigns().get(position).getTitle()));
                        } else if (!((MyApplication)getMyActivity().getApplicationContext()).getCampaigns().get(position).getIsGroupCampaign()) {
                            startActivity(new Intent(getMyActivity(), DetailSpotActivity.class).putExtra("id", ((MyApplication)getMyActivity().getApplicationContext()).getCampaigns().get(position).getId()).putExtra("name", ((MyApplication)getMyActivity().getApplicationContext()).getCampaigns().get(position).getProvider().getTitle()).putExtra("name_spot", ((MyApplication)getMyActivity().getApplicationContext()).getCampaigns().get(position).getTitle()));
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

                //swipelistview.openAnimate(position); //when you touch front view it will open

            }

            @Override
            public void onClickBackView(int position) {
                //Log.d("swipe", String.format("onClickBackView %d", position));

                //swipeListView.closeAnimate(position);//when you touch back view it will close
            }

            @Override
            public void onDismiss(int[] reverseSortedPositions) {



            }

        });

        Display display = getMyActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;


        swipeListView.setOffsetLeft(width - convertDpToPixel(240)); // left side offset
        swipeListView.setOffsetRight(-(convertDpToPixel(80f) - width));
        swipeListView.setSwipeMode(SwipeListView.SWIPE_MODE_BOTH); // there are five swiping modes
        swipeListView.setSwipeActionLeft(SwipeListView.SWIPE_ACTION_REVEAL); //there are four swipe actions
        swipeListView.setAnimationTime(200); // animarion time
        swipeListView.setSwipeOpenOnLongPress(true); // enable or disable SwipeOpenOnLongPress

        return v;
    }

    public int convertDpToPixel(float dp) {
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        float px = dp * (metrics.densityDpi / 160f);
        return (int) px;
    }


    @Override
    public void onResume() {
        super.onResume();

        //Vypnutí a spuštění servisy, kvůli samovolnému vypnutí
        getMyActivity().stopService(new Intent(getMyActivity(), MainService.class));
        getMyActivity().startService(new Intent(getMyActivity(), MainService.class));

        if(((MyApplication)getMyActivity().getApplicationContext()).getAdapter() != null) {
            ((MyApplication)getMyActivity().getApplicationContext()).getAdapter().notifyDataSetChanged();
        }

        //Zachytávání změn
        if(((MyApplication)getActivity().getApplicationContext()).getIsChange()) {
            for (int l = 0; l < ((MyApplication) getActivity().getApplicationContext()).getChange().size(); l++) {


                Log.v("kalkub", ""+((MyApplication) getActivity().getApplicationContext()).getChange().get(0).getType());

                try {

                    ChangeClass classes = ((MyApplication) getActivity().getApplicationContext()).getChange().get(l);
                    Campaign c = null;
                    for (int i = 0; i < ((MyApplication)getMyActivity().getApplicationContext()).getCampaigns().size(); i++) {
                        if(((MyApplication)getMyActivity().getApplicationContext()).getCampaigns().get(i).getId() != null && ((MyApplication)getMyActivity().getApplicationContext()).getCampaigns().get(i).getId().equals(classes.getId())) {
                            c = ((MyApplication)getMyActivity().getApplicationContext()).getCampaigns().get(i);
                            break;
                        }
                    }

                    if(c != null) {
                        if(classes.getType().equals("favourite")) {
                            c.setFavourite(c.getFavourite() != null ? null : "");
                        }
                        if(classes.getType().equals("reminder")) {
                            c.setReminder(c.getReminder() != null ? null : "");
                        }
                        if(classes.getType().equals("blockProvider")) {

                            for (int i = 0; i < campaigns1test.size(); i++) {
                                if(campaigns1test.get(i).getProvider().getId() == c.getProvider().getId()) {
                                    campaigns1test.remove(i);
                                    i--;
                                }
                            }

                            if(((MyApplication)getActivity().getApplicationContext()).getRemovedCampaign() == null) {
                                ((MyApplication)getActivity().getApplicationContext()).setRemovedCampaign(new ArrayList<Campaign>());
                            }

                            for (int i = 0; i < ((MyApplication)getActivity().getApplicationContext()).getCampaigns().size(); i++) {
                                if(((MyApplication)getActivity().getApplicationContext()).getCampaigns().get(i).getProvider().getId() == c.getProvider().getId()) {
                                    ((MyApplication)getActivity().getApplicationContext()).getRemovedCampaign().add(((MyApplication)getActivity().getApplicationContext()).getCampaigns().get(i));
                                    ((MyApplication)getActivity().getApplicationContext()).getCampaigns().remove(i);
                                    i--;
                                }
                            }

                        }
                        if(classes.getType().equals("blockCampaign")) {
                            if(c.getIgnored() != null) {
                                c.getProvider().setIgnored(null);
                                campaigns1test.remove(c);
                            }
                            else {
                                campaigns1test.remove(c);
                            }
                        }
                        if(classes.getType().equals("blockCategory")) {

                            for (int i = 0; i < campaigns1test.size(); i++) {
                                if(campaigns1test.get(i).getCategory().getId() == c.getCategory().getId()) {
                                    campaigns1test.remove(i);
                                    i--;
                                }
                            }

                            if(((MyApplication)getActivity().getApplicationContext()).getRemovedCampaign() == null) {
                                ((MyApplication)getActivity().getApplicationContext()).setRemovedCampaign(new ArrayList<Campaign>());
                            }

                            for (int i = 0; i < ((MyApplication)getActivity().getApplicationContext()).getCampaigns().size(); i++) {
                                if(((MyApplication)getActivity().getApplicationContext()).getCampaigns().get(i).getCategory().getId() == c.getCategory().getId()) {
                                    ((MyApplication)getActivity().getApplicationContext()).getRemovedCampaign().add(((MyApplication)getActivity().getApplicationContext()).getCampaigns().get(i));
                                    ((MyApplication)getActivity().getApplicationContext()).getCampaigns().remove(i);
                                    i--;
                                }
                            }

                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            ((MyApplication)getActivity().getApplicationContext()).setChange(null);
            ((MyApplication)getActivity().getApplicationContext()).setIsChange(false);
            ((MyApplication)getMyActivity().getApplicationContext()).getAdapter().notifyDataSetChanged();
        }

    }

    public View getViewByPosition(int pos, SwipeListView listView) {
        final int firstListItemPosition = listView.getFirstVisiblePosition();
        final int lastListItemPosition = firstListItemPosition + listView.getChildCount() - 1;

        if (pos < firstListItemPosition || pos > lastListItemPosition) {
            return listView.getAdapter().getView(pos, null, listView);
        } else {
            final int childIndex = pos - firstListItemPosition;
            return listView.getChildAt(childIndex);
        }
    }

    /*public void notificationChange() {
        if (test1adapter != null) {
            test1adapter.notifyDataSetChanged();
        }
    }*/

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = activity;
    }

    private Activity getMyActivity() {
        return this.activity;
    }

}
