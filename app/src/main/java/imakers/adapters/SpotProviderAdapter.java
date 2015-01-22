package imakers.adapters;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fortysevendeg.swipelistview.SwipeListView;

import java.util.List;

import imakers.beacons.R;
import imakers.classes.Campaign;
import imakers.classes.Providers;
import imakers.tools.MyUtils;

public class SpotProviderAdapter extends ArrayAdapter<Providers> {

    List<Providers> list;
    Activity c;
    SwipeListView listView;

    public SpotProviderAdapter(Activity context, int resource, List<Providers> objects, SwipeListView listView) {
        super(context, resource, objects);
        this.list = objects;
        this.c = context;
        this.listView = listView;

    }


    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {

        View v = convertView;

        String s = "";

        if(v == null) {

            v = LayoutInflater.from(c).inflate(R.layout.menu_item_spot_layout_provider, null);


        }
            if(position % 2 != 0) {
                v.findViewById(R.id.front).setBackgroundColor(Color.parseColor("#fafafa"));
            }
            else {
                v.findViewById(R.id.front).setBackgroundColor(Color.parseColor("#FFFFFF"));
            }

            ((TextView)v.findViewById(R.id.item_text)).setText(list.get(position).getTitle());
            ((ImageView)v.findViewById(R.id.item_image)).setImageDrawable(null);

            MyUtils.ImageLoaderLoadImage(list.get(position).getImg(), ((ImageView)v.findViewById(R.id.item_image)), c);





        return v;
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }
}
