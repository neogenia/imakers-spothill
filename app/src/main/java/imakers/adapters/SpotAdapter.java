package imakers.adapters;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.text.Html;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fortysevendeg.swipelistview.SwipeListView;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import imakers.beacons.R;
import imakers.classes.Campaign;
import imakers.classes.Category;
import imakers.classes.Providers;
import imakers.interfaces.MyAsyncLisener;
import imakers.tools.MyApplication;
import imakers.tools.MyHttpClient;
import imakers.tools.MyUtils;

public class SpotAdapter extends ArrayAdapter<Campaign> {

    List<Campaign> list;
    Activity c;
    SwipeListView listView;
    String type;
    View v;

    public SpotAdapter(Activity context, int resource, List<Campaign> objects, SwipeListView listView, String type) {
        super(context, resource, objects);
        this.list = objects;
        this.c = context;
        this.v = LayoutInflater.from(context).inflate(resource, null);
        this.listView = listView;
        this.type = type;

    }


    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {

        try {
            //znovu zde hlídám text aby se skryl
            if (c.findViewById(R.id.place_holder_text).getVisibility() == View.VISIBLE) {
                c.findViewById(R.id.place_holder_text).setVisibility(View.INVISIBLE);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        View v = convertView;

        //nahrávám správný layout, dle typu itemu
        if (!list.get(position).getIsGroupCampaign() && !list.get(position).getIsSeparator()) {
            v = LayoutInflater.from(c).inflate(R.layout.menu_item_spot_layout, null);
        } else if (list.get(position).getIsGroupCampaign() && !list.get(position).getIsSeparator()) {
            v = LayoutInflater.from(c).inflate(R.layout.group_row, null);
        } else if (list.get(position).getIsSeparator() && list.get(position).getType() == -2) {
            v = LayoutInflater.from(c).inflate(R.layout.menu_title_layout, null);
        }

        //Tento if je pro hlavní velký item (Kampaň)
        if (!list.get(position).getIsGroupCampaign() && !list.get(position).getIsSeparator()) {


            //Zde nastavuji už zakliklé itemy ve swipu
            if (list.get(position).getFavourite() != null) {
                v.findViewById(R.id.swipe_heart).setBackgroundColor(Color.parseColor("#e9e6e6"));
                ((ImageView) ((RelativeLayout) v.findViewById(R.id.swipe_heart)).getChildAt(0)).setImageResource(R.drawable.heart_minus);
                v.findViewById(R.id.swipe_heart).setTag(1);
            }
            if (list.get(position).getReminder() != null) {
                v.findViewById(R.id.swipe_alarm).setBackgroundColor(Color.parseColor("#e9e6e6"));
                ((ImageView) ((RelativeLayout) v.findViewById(R.id.swipe_alarm)).getChildAt(0)).setImageResource(R.drawable.reminder_minus);
                v.findViewById(R.id.swipe_alarm).setTag(1);
            }
            if (list.get(position).getCategory().getIgnored() != null || list.get(position).getProvider().getIgnored() != null || list.get(position).getIgnored() != null) {
                v.findViewById(R.id.swipe_delete).setBackgroundColor(Color.parseColor("#e9e6e6"));
                ((ImageView) ((RelativeLayout) v.findViewById(R.id.swipe_delete)).getChildAt(0)).setImageResource(R.drawable.destryo_minus);
                Resources r = getContext().getResources();
                int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 18, r.getDisplayMetrics());
                ((ImageView) ((RelativeLayout) v.findViewById(R.id.swipe_delete)).getChildAt(0)).setPadding(px, px, px, px);
                v.findViewById(R.id.swipe_delete).setTag(1);
            }


            // různé barvy pro každý 2 řádek
            if (position % 2 != 0) {
                v.findViewById(R.id.front).setBackgroundColor(Color.parseColor("#fafafa"));
            } else {
                v.findViewById(R.id.front).setBackgroundColor(Color.parseColor("#FFFFFF"));
            }

            ((TextView) v.findViewById(R.id.item_text)).setText(list.get(position).getTitle());
            ((TextView) v.findViewById(R.id.item_desc)).setText(list.get(position).getDescription());

            String[] date = list.get(position).getDateTo().split(" ")[0].split("-");

            ((TextView) v.findViewById(R.id.item_counter_text)).setText(c.getString(R.string.ends)+ " " + date[2] + "." + date[1] + "." + date[0]);
            ((ImageView) v.findViewById(R.id.item_image)).setImageDrawable(null);

            switch (list.get(position).getType()) {
                case 1:
                    ((ImageView) v.findViewById(R.id.item_type)).setImageResource(R.drawable.campaigns_shopping);
                    break;
                case 2:
                    ((ImageView) v.findViewById(R.id.item_type)).setImageResource(R.drawable.campaigns_event);
                    break;
                case 3:
                    ((ImageView) v.findViewById(R.id.item_type)).setImageResource(R.drawable.campaigns_tourism);
                    break;
            }

            MyUtils.ImageLoaderLoadImage(list.get(position).getImage(), ((ImageView) v.findViewById(R.id.item_image)), c);


            final View finalV = v;

            //akce delete
            v.findViewById(R.id.swipe_delete).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View vv) {

                    if (finalV.findViewById(R.id.red_separate).getVisibility() == View.VISIBLE) {
                        finalV.findViewById(R.id.red_separate).setVisibility(View.GONE);
                    } else {
                        finalV.findViewById(R.id.red_separate).setVisibility(View.VISIBLE);
                        try {
                            ((TextView) finalV.findViewById(R.id.mark)).setText(list.get(position).getProvider().getTitle());
                            ((TextView) finalV.findViewById(R.id.region)).setText(list.get(position).getCategory().getTitle());
	                        ((TextView) finalV.findViewById(R.id.spot)).setText(c.getString(R.string.this_campaign));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        //nastavování už blokovaných věcí
                        if (list.get(position).getProvider().getIgnored() != null) {
                            ((TextView) finalV.findViewById(R.id.mark)).setBackgroundColor(Color.parseColor("#e9e6e6"));
                            ((TextView) finalV.findViewById(R.id.mark)).setTextColor(Color.BLACK);
                        }
                        if (list.get(position).getCategory().getIgnored() != null) {
                            ((TextView) finalV.findViewById(R.id.region)).setBackgroundColor(Color.parseColor("#e9e6e6"));
                            ((TextView) finalV.findViewById(R.id.region)).setTextColor(Color.BLACK);
                        }
                        if (list.get(position).getIgnored() != null) {
                            ((TextView) finalV.findViewById(R.id.spot)).setBackgroundColor(Color.parseColor("#e9e6e6"));
                            ((TextView) finalV.findViewById(R.id.spot)).setTextColor(Color.BLACK);
                        }

                        //akce pro kategorii
                        finalV.findViewById(R.id.region).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (((MyApplication) getContext().getApplicationContext()).getRemovedCampaign() == null) {
                                    ((MyApplication) getContext().getApplicationContext()).setRemovedCampaign(new ArrayList<Campaign>());
                                }

                                //Metoda, která zasíla request na server async
                                MyUtils.createAction(c, Long.valueOf(list.get(position).getCategory().getId()), 4L);


                                if (type.equals("blocked")) {

                                    // V tomto bloku přidávám do listu removed, který smaže itemy na hlavní stránce

                                    ((MyApplication) getContext().getApplicationContext()).setCurrentSpots(new ArrayList<Integer>());

                                    for (int i = 0; i < ((MyApplication) getContext().getApplicationContext()).getCampaigns().size(); i++) {
                                        ((MyApplication) getContext().getApplicationContext()).getRemovedCampaign().add(((MyApplication) getContext().getApplicationContext()).getCampaigns().get(i));
                                    }


                                    //Načítání kampaní blokovaných kampaní
                                    loadCampaigns(8L);
                                } else {
                                    if (list.get(position).getCategory().getIgnored() == null) {


                                        ((MyApplication) getContext().getApplicationContext()).setCurrentSpots(new ArrayList<Integer>());

                                        String cc = list.get(position).getCategory().getTitle();


                                        //Procházení kampaní, který se smažou z aktuální stránky
                                        for (final int[] i = {0}; i[0] < list.size(); i[0]++) {
                                            if (list.get(i[0]).getCategory() != null && cc.equals(list.get(i[0]).getCategory().getTitle())) {
                                                ((MyApplication) getContext().getApplicationContext()).getRemovedCampaign().add(list.get(i[0]));
                                                if (!type.equals("home")) {
                                                    ((MyApplication) getContext().getApplicationContext()).getCampaigns().remove(list.get(i[0]));
                                                }
                                                if (!type.equals("blocked")) {
                                                    c.runOnUiThread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            notifyDataSetChanged();
                                                            list.get(i[0]).getCategory().setIgnored(null);
                                                            list.remove(i[0]);
                                                            i[0]--;
                                                            notifyDataSetChanged();
                                                        }
                                                    });
                                                }
                                            }
                                        }


                                        //refresh
                                        if (((MyApplication) getContext().getApplicationContext()).getAdapter() != null) {
                                            c.runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    try {
                                                        ((MyApplication) getContext().getApplicationContext()).getAdapter().notifyDataSetChanged();
                                                    } catch (Exception e) {
                                                        e.printStackTrace();
                                                    }
                                                }
                                            });
                                        }

                                    }
                                }


                            }
                        });


                        //blokování spotu, tyto akceobsahují stejný systém mazání, který jsem vysvětloval u kategorií
                        finalV.findViewById(R.id.spot).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                if (((MyApplication) getContext().getApplicationContext()).getRemovedCampaign() == null) {
                                    ((MyApplication) getContext().getApplicationContext()).setRemovedCampaign(new ArrayList<Campaign>());
                                }

                                try {
                                    MyUtils.createAction(c, list.get(position).getId(), 3L);
                                } catch (Exception e) {

                                }

                                if (type.equals("blocked")) {
                                    ((MyApplication) getContext().getApplicationContext()).setCurrentSpots(new ArrayList<Integer>());

                                    for (int i = 0; i < ((MyApplication) getContext().getApplicationContext()).getCampaigns().size(); i++) {
                                        ((MyApplication) getContext().getApplicationContext()).getRemovedCampaign().add(list.get(position));
                                    }

                                    loadCampaigns(8L);
                                } else {


                                    if (list.get(position).getIgnored() == null) {

                                        if (!type.equals("blocked")) {
                                            c.runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    notifyDataSetChanged();

                                                    ((MyApplication) getContext().getApplicationContext()).getRemovedCampaign().add(list.get(position));
                                                    if (!type.equals("home")) {
                                                        ((MyApplication) getContext().getApplicationContext()).getCampaigns().remove(list.get(position));
                                                    }

                                                    list.get(position).setIgnored(null);
                                                    list.remove(position);
                                                    notifyDataSetChanged();
                                                }
                                            });
                                        }

                                        if (((MyApplication) getContext().getApplicationContext()).getAdapter() != null) {
                                            c.runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    try {
                                                        ((MyApplication) getContext().getApplicationContext()).getAdapter().notifyDataSetChanged();
                                                    } catch (Exception e) {
                                                        e.printStackTrace();
                                                    }
                                                }
                                            });
                                        }

                                    }
                                }
                            }
                        });

                        //blokování providera, tyto akceobsahují stejný systém mazání, který jsem vysvětloval u kategorií
                        finalV.findViewById(R.id.mark).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                if (((MyApplication) getContext().getApplicationContext()).getRemovedCampaign() == null) {
                                    ((MyApplication) getContext().getApplicationContext()).setRemovedCampaign(new ArrayList<Campaign>());
                                }

                                MyUtils.createAction(c, Long.valueOf(list.get(position).getProvider().getId()), 5L);

                                if (type.equals("blocked")) {

                                    ((MyApplication) getContext().getApplicationContext()).setCurrentSpots(new ArrayList<Integer>());

                                    for (int i = 0; i < ((MyApplication) getContext().getApplicationContext()).getCampaigns().size(); i++) {
                                        ((MyApplication) getContext().getApplicationContext()).getRemovedCampaign().add(((MyApplication) getContext().getApplicationContext()).getCampaigns().get(i));
                                    }


                                    loadCampaigns(8L);
                                } else {
                                    if (list.get(position).getProvider().getIgnored() == null) {

                                        String cc = list.get(position).getProvider().getTitle();

                                        for (final int[] i = {0}; i[0] < list.size(); i[0]++) {
                                            if (list.get(i[0]).getCategory() != null && cc.equals(list.get(i[0]).getProvider().getTitle())) {
                                                ((MyApplication) getContext().getApplicationContext()).getRemovedCampaign().add(list.get(i[0]));
                                                if (!type.equals("home")) {
                                                    ((MyApplication) getContext().getApplicationContext()).getCampaigns().remove(list.get(i[0]));
                                                }
                                                if (!type.equals("blocked")) {
                                                    c.runOnUiThread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            notifyDataSetChanged();
                                                            list.get(i[0]).getProvider().setIgnored(null);
                                                            list.remove(i[0]);
                                                            i[0]--;
                                                            notifyDataSetChanged();
                                                        }
                                                    });
                                                }
                                            }
                                        }

                                        if (((MyApplication) getContext().getApplicationContext()).getAdapter() != null) {
                                            c.runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    try {
                                                        ((MyApplication) getContext().getApplicationContext()).getAdapter().notifyDataSetChanged();
                                                    } catch (Exception e) {
                                                        e.printStackTrace();
                                                    }
                                                }
                                            });
                                        }

                                    }
                                }


                            }
                        });
                    }

                }
            });


            //share akce
            v.findViewById(R.id.swipe_share).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String s = c.getString(R.string.share_text_begin) + " " + list.get(position).getTitle() + (list.get(position).getProvider().getTitle().isEmpty() ? "" : " "+ c.getString(R.string.share_text_middle) + " " + list.get(position).getProvider().getTitle()) + c.getString(R.string.share_text_end) + " <a href='http://www.spothill.com'>spothill</a>.";
                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND);
                    sendIntent.putExtra(Intent.EXTRA_TEXT, Html.fromHtml(s));
                    sendIntent.setType("text/plain");
                    c.startActivity(sendIntent);
                }
            });

            v.findViewById(R.id.swipe_heart).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MyUtils.createAction(c, list.get(position).getId(), 2L);

                    Integer value = Integer.valueOf(v.getTag().toString());


                    //zjišťuji, jakou akci provést, zde by se také mohl použít list.get(position).getFavorite() == null
                    if (value.equals(0)) {
                        v.setBackgroundColor(Color.parseColor("#e9e6e6"));
                        ((ImageView) ((RelativeLayout) v).getChildAt(0)).setImageResource(R.drawable.heart_minus);
                        v.setTag(1);

                        //Procházení všech kampaní a nastavování itemu na setFavourite("");
                        if (!type.contains("home")) {
                            for (int i = 0; i < ((MyApplication) getContext().getApplicationContext()).getCampaigns().size(); i++) {
                                Campaign cc = ((MyApplication) getContext().getApplicationContext()).getCampaigns().get(i);

                                if (cc.getId() != null && cc.getId().equals(list.get(position).getId())) {
                                    ((MyApplication) getContext().getApplicationContext()).getCampaigns().get(i).setFavourite("");
                                    if (((MyApplication) getContext().getApplicationContext()).getAdapter() != null) {
                                        c.runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                try {
                                                    ((MyApplication) getContext().getApplicationContext()).getAdapter().notifyDataSetChanged();
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        });
                                    }
                                    break;
                                }
                            }
                        }

                        list.get(position).setFavourite("");


                    } else {
                        v.setBackgroundColor(Color.parseColor("#faa62f"));
                        ((ImageView) ((RelativeLayout) v).getChildAt(0)).setImageResource(R.drawable.heart_plus);
                        v.setTag(0);
                        if (!type.contains("home")) {
                            for (int i = 0; i < ((MyApplication) getContext().getApplicationContext()).getCampaigns().size(); i++) {
                                Campaign cc = ((MyApplication) getContext().getApplicationContext()).getCampaigns().get(i);

                                if (cc.getId().equals(list.get(position).getId())) {
                                    ((MyApplication) getContext().getApplicationContext()).getCampaigns().get(i).setFavourite(null);
                                    if (((MyApplication) getContext().getApplicationContext()).getAdapter() != null) {
                                        c.runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                try {
                                                    ((MyApplication) getContext().getApplicationContext()).getAdapter().notifyDataSetChanged();
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        });
                                    }
                                    break;
                                }
                            }
                        }

                        //Když jsem na stránce favorite, tak to ten item vymaže
                        list.get(position).setFavourite(null);
                        if (type.contains("favorite")) {
                            list.remove(position);
                            notifyDataSetChanged();
                        }


                    }


                }
            });


            //akce remindera chová se stejně jako u favorite
            v.findViewById(R.id.swipe_alarm).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MyUtils.createAction(c, list.get(position).getId(), 1L);

                    Integer value = Integer.valueOf(v.getTag().toString());

                    if (value.equals(0)) {
                        v.setBackgroundColor(Color.parseColor("#e9e6e6"));
                        ((ImageView) ((RelativeLayout) v).getChildAt(0)).setImageResource(R.drawable.reminder_minus);
                        v.setTag(1);


                        if (!type.contains("home")) {
                            for (int i = 0; i < ((MyApplication) getContext().getApplicationContext()).getCampaigns().size(); i++) {
                                Campaign cc = ((MyApplication) getContext().getApplicationContext()).getCampaigns().get(i);

                                if (cc.getId().equals(list.get(position).getId())) {
                                    ((MyApplication) getContext().getApplicationContext()).getCampaigns().get(i).setReminder("");
                                    if (((MyApplication) getContext().getApplicationContext()).getAdapter() != null) {
                                        c.runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                try {
                                                    ((MyApplication) getContext().getApplicationContext()).getAdapter().notifyDataSetChanged();
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        });
                                    }
                                    break;
                                }
                            }
                        }

                        list.get(position).setReminder("");


                    } else {

                        v.setBackgroundColor(Color.parseColor("#28ce82"));
                        ((ImageView) ((RelativeLayout) v).getChildAt(0)).setImageResource(R.drawable.reminder_plus);
                        v.setTag(0);

                        if (!type.contains("home")) {
                            for (int i = 0; i < ((MyApplication) getContext().getApplicationContext()).getCampaigns().size(); i++) {
                                Campaign cc = ((MyApplication) getContext().getApplicationContext()).getCampaigns().get(i);

                                if (cc.getId() != null && cc.getId().equals(list.get(position).getId())) {
                                    ((MyApplication) getContext().getApplicationContext()).getCampaigns().get(i).setReminder(null);
                                    if (((MyApplication) getContext().getApplicationContext()).getAdapter() != null) {
                                        c.runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                try {
                                                    ((MyApplication) getContext().getApplicationContext()).getAdapter().notifyDataSetChanged();
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        });
                                    }
                                    break;
                                }
                            }
                        }

                        list.get(position).setReminder(null);
                        if (type.contains("reminder")) {
                            list.remove(position);
                            notifyDataSetChanged();
                        }
                    }

                }
            });

        } else if (list.get(position).getIsGroupCampaign() && !list.get(position).getIsSeparator()) { //GroupItems, malé kampaně na hlavní stránce

            try {

                if (v == null) {
                    v = LayoutInflater.from(c).inflate(R.layout.group_row, null);
                }

                String ss = list.get(position).getTitle();
                if (ss == null) {
                    ss = "";
                }

                if (list.get(position).getAllowDetail() == 0) {
                    v.findViewById(R.id.arrow).setVisibility(View.INVISIBLE);
                }

                if (position % 2 != 0) {
                    v.findViewById(R.id.front).setBackgroundColor(Color.parseColor("#fafafa"));
                } else {
                    v.findViewById(R.id.front).setBackgroundColor(Color.parseColor("#FFFFFF"));
                }

                ((TextView) v.findViewById(R.id.group_title)).setText(ss);

                switch (list.get(position).getType()) {
                    case 1:
                        ((ImageView) v.findViewById(R.id.group_type)).setImageResource(R.drawable.groupcampaigns_shopping);
                        break;
                    case 2:
                        ((ImageView) v.findViewById(R.id.group_type)).setImageResource(R.drawable.groupcampaigns_event);
                        break;
                    case 3:
                        ((ImageView) v.findViewById(R.id.group_type)).setImageResource(R.drawable.groupcampaigns_tourism);
                        break;
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        return v;
    }

    public void setType(String type) {
        this.type = type;
    }



    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }



    // metoda pro načítání kampaní, speciálně jen pro mazání v blokacích, pro zjednodušení. Zde je duplikace, protože stejná metoda je ve fragmentu.
    void loadCampaigns(Long id) {
        c.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                notifyDataSetChanged();
                list.clear();
                notifyDataSetChanged();
            }
        });
        //notifyDataSetChanged();
        MyUtils.showDialog(c);
        MyHttpClient.get(c, MyApplication.API_URL+"api/list/" + id + "/?hash=" + ((MyApplication) c.getApplicationContext()).getHash() + "&count=40&page=1", new RequestParams(), new MyAsyncLisener() {
            @Override
            public void onComplete(JSONObject data) {
                if (data != null) {

                    try {

                        final JSONObject cam = data.getJSONObject("campaigns");

                        Iterator i = cam.keys();

                        while (i.hasNext()) {

                            try {
                                final String key = (String) i.next();

                                try {

                                    if (!key.equals("isMore")) {

                                        //Musel jsem to udělat následovně, protože databázová knihovna kolidovala s Gsonem a nakonec to blblo celé, je to takto všude, bohužél do budoucna by bylo dobré přepracovat
                                        c.runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {

                                                JSONObject object = null;
                                                try {
                                                    object = cam.getJSONObject(key);
                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                }

                                                //CopyCampaign spot = g.fromJson(object.toString(), CopyCampaign.class);
                                                final Campaign spot = new Campaign();

                                                spot.setFavourite(object.optString("favourite", null));
                                                spot.setDateTo(object.optString("dateTo"));
                                                spot.setRemaining(object.optInt("remaining"));
                                                spot.setReminder(object.optString("reminder", null));
                                                Category cat = new Category();
                                                try {
                                                    cat.setId(object.getJSONObject("category").getInt("id"));
                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                }
                                                try {
                                                    cat.setIgnored(object.getJSONObject("category").optString("ignored", null));
                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                }
                                                try {
                                                    cat.setTitle(object.getJSONObject("category").optString("title"));
                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                }
                                                spot.setCategory(cat);
                                                Providers providers = new Providers();
                                                try {
                                                    providers.setId(object.getJSONObject("provider").getInt("id"));
                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                }
                                                try {
                                                    providers.setIgnored(object.getJSONObject("provider").optString("ignored", null));
                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                }
                                                try {
                                                    providers.setTitle(object.getJSONObject("provider").optString("title"));
                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                }
                                                spot.setProvider(providers);
                                                spot.setIgnored(object.optString("ignored", null));
                                                spot.setType(object.optInt("type"));
                                                spot.setDescription(object.optString("description"));
                                                try {
                                                    spot.setId(object.getLong("id"));
                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                }
                                                spot.setImage(object.optString("image"));
                                                spot.setTitle(object.optString("title"));
                                                spot.setEvent(object.optInt("event"));

                                                spot.setIsGroupCampaign(false);
                                                spot.setIsSeparator(false);


                                                spot.setIsGroupCampaign(false);
                                                spot.setIsSeparator(false);

                                                list.add(spot);
                                            }
                                        });
                                    }

                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        }


                    } catch (Exception e) {
                        e.printStackTrace();
                    }


                }


                c.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        notifyDataSetChanged();
                        if (list.size() == 0) {
                            v.findViewById(R.id.place_holder_text).setVisibility(View.VISIBLE);
                        } else {
                            v.findViewById(R.id.place_holder_text).setVisibility(View.GONE);
                        }
                        notifyDataSetChanged();
                    }
                });


                MyUtils.dissmissDialog(c);


            }
        });

    }

}
