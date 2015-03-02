package imakers.beacons;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.gson.Gson;
import com.loopj.android.http.RequestParams;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import imakers.adapters.BlockCategoryAdapter;
import imakers.adapters.BlockProviderAdapter;
import imakers.beacons.R;
import imakers.classes.Category;
import imakers.classes.Providers;
import imakers.interfaces.MyAsyncLisener;
import imakers.tools.MyActionPanel;
import imakers.tools.MyApplication;
import imakers.tools.MyHttpClient;
import imakers.tools.MyUtils;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

public class MenuBlockActivity extends Activity {

    private MyActionPanel panel;

    List<Providers> providers = new ArrayList<Providers>();
    List<Category> categories = new ArrayList<Category>();

    Boolean isCat;
    private StickyListHeadersListView stickyList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_block);

        stickyList = (StickyListHeadersListView)findViewById(R.id.list);


        panel = new MyActionPanel(this, "");

        isCat = getIntent().getBooleanExtra("isCat", false);

        if(isCat) {
          panel.setText(getString(R.string.blocked_categories_title));
            ((TextView)findViewById(R.id.place_holder_text)).setText(getString(R.string.no_categories));
            favoriteCategory();
        }
        else {
            panel.setText(getString(R.string.blocked_providers_title));
            ((TextView)findViewById(R.id.place_holder_text)).setText(getString(R.string.no_providers));
            favoriteMarks();
        }




    }

    //načítání kategorií
    void favoriteCategory() {

        MyUtils.showDialog(this);
        MyHttpClient.get(this, MyApplication.API_URL+"api/list/4/?hash=" + ((MyApplication)getApplicationContext()).getHash(), new RequestParams(), new MyAsyncLisener() {
            @Override
            public void onComplete(JSONObject data) {
                if (data != null) {

                    try {

                        JSONObject cam = data.getJSONObject("categories");

                        Iterator i = cam.keys();

                        Gson g = new Gson();

                        while (i.hasNext()) {

                            try {
                                String key = (String) i.next();

                                JSONObject object = cam.getJSONObject(key);

                                Category spot = g.fromJson(object.toString(), Category.class);
                                categories.add(spot);
                            } catch (Exception e) {

                            }

                        }
                        //sortování dle abecedy
                        Collections.sort(categories, new Comparator<Category>() {
                            @Override
                            public int compare(Category lhs, Category rhs) {
                                return lhs.getTitle().toLowerCase().compareTo(rhs.getTitle().toLowerCase());
                            }
                        });

                        stickyList.setAdapter(new BlockCategoryAdapter(MenuBlockActivity.this, categories));


                    } catch (Exception e) {
                        e.printStackTrace();
                    }


                }

                if (providers.size() == 0) {
                    findViewById(R.id.place_holder_text).setVisibility(View.VISIBLE);
                } else {
                    findViewById(R.id.place_holder_text).setVisibility(View.GONE);
                }

                MyUtils.dissmissDialog(MenuBlockActivity.this);

            }
        });

    }

    void favoriteMarks() {

        MyUtils.showDialog(this);
        MyHttpClient.get(this, MyApplication.API_URL+"api/list/5/?hash=" + ((MyApplication)getApplicationContext()).getHash(), new RequestParams(), new MyAsyncLisener() {
            @Override
            public void onComplete(JSONObject data) {
                if (data != null) {

                    try {

                        JSONObject cam = data.getJSONObject("providers");

                        Iterator i = cam.keys();

                        Gson g = new Gson();

                        while (i.hasNext()) {

                            try {
                                String key = (String) i.next();

                                JSONObject object = cam.getJSONObject(key);

                                Providers spot = g.fromJson(object.toString(), Providers.class);
                                providers.add(spot);
                            } catch (Exception e) {

                            }

                        }

                        Collections.sort(providers, new Comparator<Providers>() {
                            @Override
                            public int compare(Providers lhs, Providers rhs) {
                                return lhs.getTitle().toLowerCase().compareTo(rhs.getTitle().toLowerCase());
                            }
                        });

                        stickyList.setAdapter(new BlockProviderAdapter(MenuBlockActivity.this, providers));


                    } catch (Exception e) {
                        e.printStackTrace();
                    }


                }

                if (providers.size() == 0) {
                    findViewById(R.id.place_holder_text).setVisibility(View.VISIBLE);
                } else {
                    findViewById(R.id.place_holder_text).setVisibility(View.GONE);
                }

                MyUtils.dissmissDialog(MenuBlockActivity.this);

            }
        });

    }

}
