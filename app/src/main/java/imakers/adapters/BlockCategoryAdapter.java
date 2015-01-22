package imakers.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SectionIndexer;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import imakers.beacons.R;
import imakers.classes.Campaign;
import imakers.classes.Category;
import imakers.classes.Spot;
import imakers.tools.MyUtils;
import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;


//Zde využívám knihovnu na
public class BlockCategoryAdapter extends BaseAdapter implements StickyListHeadersAdapter {

    Activity activity;
    List<Category> campaigns;

    public BlockCategoryAdapter(Activity activity, List<Category> campaigns) {
        this.activity = activity;
        this.campaigns = campaigns;
    }

    @Override
    public int getCount() {
        return campaigns.size();
    }

    @Override
    public Object getItem(int position) {
        return campaigns.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    //Metoda, která vykresluje prvky
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        //Tento blok skrývá a zobrazuje placeHolder text
        if (campaigns.size() == 0) {
            activity.findViewById(R.id.place_holder_text).setVisibility(View.VISIBLE);
        } else {
            activity.findViewById(R.id.place_holder_text).setVisibility(View.GONE);
        }

        View v = convertView;

        if(v == null)
            v = LayoutInflater.from(activity).inflate(R.layout.menu_item_spot_layout_provider, null);



        ((TextView)v.findViewById(R.id.item_text)).setText(campaigns.get(position).getTitle());

        ((ImageView) v.findViewById(R.id.item_image)).setVisibility(View.GONE);

        return v;
    }


    //Vykresluje hlavičku
    @Override
    public View getHeaderView(int i, View view, ViewGroup viewGroup) {

        View vvv = view;

        if(vvv == null) {
            vvv = LayoutInflater.from(activity).inflate(R.layout.header_text_layout, null);
        }

        LinearLayout ll = (LinearLayout)vvv;

        for (int j = 0; j < ll.getChildCount(); j++) {

            View v = ll.getChildAt(j);

            if(v instanceof TextView) {


                try {

                    ((TextView)v).setText(String.valueOf(campaigns.get(i).getTitle().subSequence(0,1).charAt(0)).toUpperCase());

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }




        }

        return vvv;
    }

    @Override
    public long getHeaderId(int i) {
        return campaigns.get(i).getTitle().subSequence(0, 1).charAt(0);
    }
}
