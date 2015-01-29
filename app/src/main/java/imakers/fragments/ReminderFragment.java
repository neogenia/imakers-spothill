package imakers.fragments;


import android.app.Fragment;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fortysevendeg.swipelistview.BaseSwipeListViewListener;
import com.fortysevendeg.swipelistview.SwipeListView;
import com.google.gson.Gson;
import com.loopj.android.http.RequestParams;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import imakers.adapters.SpotAdapter;
import imakers.beacons.DetailSpotActivity;
import imakers.beacons.R;
import imakers.classes.Campaign;
import imakers.classes.Category;
import imakers.classes.ChangeClass;
import imakers.classes.Providers;
import imakers.interfaces.MyAsyncLisener;
import imakers.tools.MyApplication;
import imakers.tools.MyHttpClient;
import imakers.tools.MyUtils;

/**
 * A simple {@link android.app.Fragment} subclass.
 *
 */
public class ReminderFragment extends Fragment {

    int openItem = -1;
    private SwipeListView swipeListView;
    SpotAdapter test1adapter;
    List<Campaign> campaigns1test = new ArrayList<Campaign>();
    View v;
    public ReminderFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        v = inflater.inflate(R.layout.fragment_reminder, container, false);

        swipeListView = (SwipeListView)v.findViewById(R.id.listview);

        test1adapter = new SpotAdapter(getActivity(), R.layout.menu_item_spot_layout, campaigns1test, swipeListView, "reminder");

        swipeListView.setAdapter(test1adapter);

        if(campaigns1test.size() == 0) {
            v.findViewById(R.id.place_holder_text).setVisibility(View.VISIBLE);
        }
        else {
            v.findViewById(R.id.place_holder_text).setVisibility(View.GONE);
        }

        //stahování položek
        MyUtils.showDialog(getActivity());
        MyHttpClient.get(getActivity(), MyApplication.API_URL+"api/list/1/?hash="+((MyApplication)getActivity().getApplicationContext()).getHash(), new RequestParams(), new MyAsyncLisener() {
            @Override
            public void onComplete(JSONObject data) {
                if (data != null) {

                    try {



                        JSONObject cam = data.getJSONObject("campaigns");

                        Iterator i = cam.keys();

                        Gson g = new Gson();

                        while (i.hasNext()) {

                            try {
                                String key = (String)i.next();

                                JSONObject object = cam.getJSONObject(key);

                                Campaign spot = new Campaign();

                                spot.setFavourite(object.optString("favourite", null));
                                spot.setDateTo(object.optString("dateTo"));
                                spot.setRemaining(object.optInt("remaining"));
                                spot.setReminder(object.optString("reminder", null));
                                Category cat = new Category();
                                cat.setId(object.getJSONObject("category").getInt("id"));
                                cat.setIgnored(object.getJSONObject("category").optString("ignored", null));
                                cat.setTitle(object.getJSONObject("category").optString("title"));
                                spot.setCategory(cat);
                                Providers providers = new Providers();
                                providers.setId(object.getJSONObject("provider").getInt("id"));
                                providers.setIgnored(object.getJSONObject("provider").optString("ignored", null));
                                providers.setTitle(object.getJSONObject("provider").optString("title"));
                                spot.setProvider(providers);
                                spot.setIgnored(object.optString("ignored", null));
                                spot.setType(object.optInt("type"));
                                spot.setDescription(object.optString("description"));
                                spot.setId(object.optLong("id"));
                                spot.setImage(object.optString("image"));
                                spot.setTitle(object.optString("title"));
                                spot.setEvent(object.optInt("event"));

                                spot.setIsGroupCampaign(false);
                                spot.setIsSeparator(false);



                                spot.setIsGroupCampaign(false);
                                spot.setIsSeparator(false);
                                campaigns1test.add(spot);
                            } catch (Exception e) {

                            }

                        }


                    } catch (Exception e) {
                        e.printStackTrace();
                    }



                    test1adapter.notifyDataSetChanged();
                }

                if(campaigns1test.size() == 0) {
                    v.findViewById(R.id.place_holder_text).setVisibility(View.VISIBLE);
                }
                else {
                    v.findViewById(R.id.place_holder_text).setVisibility(View.GONE);
                }

                MyUtils.dissmissDialog(getActivity());

            }
        });



        swipeListView.setSwipeListViewListener(new BaseSwipeListViewListener() {

            @Override
            public int onChangeSwipeMode(int position) {

                if(campaigns1test.get(position).getIsGroupCampaign() || campaigns1test.get(position).getIsSeparator()) {
                    return SwipeListView.SWIPE_MODE_NONE;
                }
                else {
                    return SwipeListView.SWIPE_MODE_BOTH;
                }
            }

            @Override
            public void onOpened(int position, boolean toRight) {
                if(campaigns1test.get(position).getIsGroupCampaign() || campaigns1test.get(position).getIsSeparator()) {
                    swipeListView.closeAnimate(position);
                }

                if(toRight) {
                    getViewByPosition(position, swipeListView).findViewById(R.id.swipe_alarm).setVisibility(View.INVISIBLE);
                    getViewByPosition(position, swipeListView).findViewById(R.id.swipe_heart).setVisibility(View.INVISIBLE);
                    getViewByPosition(position, swipeListView).findViewById(R.id.swipe_delete).setVisibility(View.INVISIBLE);
                    getViewByPosition(position, swipeListView).findViewById(R.id.swipe_share).setVisibility(View.VISIBLE);
                }
                else {
                    getViewByPosition(position, swipeListView).findViewById(R.id.swipe_share).setVisibility(View.INVISIBLE);
                    getViewByPosition(position, swipeListView).findViewById(R.id.swipe_alarm).setVisibility(View.VISIBLE);
                    getViewByPosition(position, swipeListView).findViewById(R.id.swipe_heart).setVisibility(View.VISIBLE);
                    getViewByPosition(position, swipeListView).findViewById(R.id.swipe_delete).setVisibility(View.VISIBLE);
                }

            }

            @Override
            public void onClosed(int position, boolean fromRight) {
                campaigns1test.get(position).setIsSeparatorEnable(false);
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
                if(campaigns1test.get(position).getIsGroupCampaign() || campaigns1test.get(position).getIsSeparator()) {
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

                if(campaigns1test.get(position).getIsGroupCampaign() || campaigns1test.get(position).getIsSeparator()) {
                    swipeListView.closeAnimate(position);
                }

                if(openItem > -1) {
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

                    if(campaigns1test.get(position).getIsGroupCampaign() || campaigns1test.get(position).getIsSeparator()) {
                        swipeListView.closeAnimate(position);
                    }

                    if(!campaigns1test.get(position).getIsSeparator()) {
                        if(campaigns1test.get(position).getIsGroupCampaign() && campaigns1test.get(position).getAllowDetail() == 1) {
                            startActivity(new Intent(getActivity(), DetailSpotActivity.class).putExtra("id", campaigns1test.get(position).getId()).putExtra("name", campaigns1test.get(position).getProvider().getTitle()).putExtra("name_spot", campaigns1test.get(position).getTitle()));
                        }
                        else if(!campaigns1test.get(position).getIsGroupCampaign()) {
                            startActivity(new Intent(getActivity(), DetailSpotActivity.class).putExtra("id", campaigns1test.get(position).getId()).putExtra("name", campaigns1test.get(position).getProvider().getTitle()).putExtra("name_spot", campaigns1test.get(position).getTitle()));
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

        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;


        //nastavování parametrů pro swipe
        swipeListView.setOffsetRight(-(convertDpToPixel(80f)-width)); // left side offset
        swipeListView.setOffsetLeft(width-convertDpToPixel(240)); // left side offset
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
    public View getViewByPosition(int pos, SwipeListView listView) {
        final int firstListItemPosition = listView.getFirstVisiblePosition();
        final int lastListItemPosition = firstListItemPosition + listView.getChildCount() - 1;

        if (pos < firstListItemPosition || pos > lastListItemPosition ) {
            return listView.getAdapter().getView(pos, null, listView);
        } else {
            final int childIndex = pos - firstListItemPosition;
            return listView.getChildAt(childIndex);
        }
    }

    @Override
    public void onResume() {
        super.onResume();


        //načítání změn
        if(((MyApplication)getActivity().getApplicationContext()).getIsChange()) {
            for (int l = 0; l < ((MyApplication) getActivity().getApplicationContext()).getChange().size(); l++) {
                try {

                    ChangeClass classes = ((MyApplication) getActivity().getApplicationContext()).getChange().get(l);
                    Campaign c = null;
                    for (int i = 0; i < campaigns1test.size(); i++) {
                        if(campaigns1test.get(i).getId() != null && campaigns1test.get(i).getId().equals(classes.getId())) {
                            c = campaigns1test.get(i);
                            break;
                        }
                    }

                    if(c != null) {
                        if(classes.getType().equals("favourite")) {
                            c.setFavourite(c.getFavourite() != null ? null : "");
                        }
                        if(classes.getType().equals("reminder")) {
                            c.setReminder(c.getReminder() != null ? null : "");
                            campaigns1test.remove(c);
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
            test1adapter.notifyDataSetChanged();
        }
    }
}
