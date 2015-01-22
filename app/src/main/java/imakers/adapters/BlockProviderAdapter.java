package imakers.adapters;

import android.app.Activity;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import imakers.beacons.R;
import imakers.classes.Campaign;
import imakers.classes.Providers;
import imakers.tools.MyUtils;
import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;

public class BlockProviderAdapter extends BaseAdapter implements StickyListHeadersAdapter {

    Activity activity;
    List<Providers> campaigns;

    public BlockProviderAdapter(Activity activity, List<Providers> campaigns) {
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

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        if (campaigns.size() == 0) {
            activity.findViewById(R.id.place_holder_text).setVisibility(View.VISIBLE);
        } else {
            activity.findViewById(R.id.place_holder_text).setVisibility(View.GONE);
        }

        View v = convertView;

        if(v == null)
            v = LayoutInflater.from(activity).inflate(R.layout.menu_item_spot_layout_provider, null);



        ((TextView)v.findViewById(R.id.item_text)).setText(campaigns.get(position).getTitle());
        MyUtils.ImageLoaderLoadImage(campaigns.get(position).getImg(), ((ImageView) v.findViewById(R.id.item_image)), activity);

        return v;
    }

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
