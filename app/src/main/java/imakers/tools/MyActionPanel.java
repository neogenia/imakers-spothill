package imakers.tools;

import android.app.Activity;
import android.content.DialogInterface;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import imakers.beacons.R;

public class MyActionPanel {

    ImageView back, menu, share;
    TextView text;
    Activity activity;

    public MyActionPanel(Activity ac, String text) {
        activity = ac;
        this.text = (TextView)ac.findViewById(R.id.header_text);
        back = (ImageView)ac.findViewById(R.id.back_button);
        menu = (ImageView)ac.findViewById(R.id.menu_button);
        share = (ImageView)ac.findViewById(R.id.share);
        this.text.setText(text);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.finish();
            }
        });
    }

    public void showBack() {
        back.setVisibility(View.VISIBLE);
    }

    public void hideBack() {
        back.setVisibility(View.GONE);
    }

    public void showMenu(View.OnClickListener click) {
        menu.setVisibility(View.VISIBLE);
        menu.setOnClickListener(click);
    }

    public void showShare(View.OnClickListener click) {
        share.setVisibility(View.VISIBLE);
        share.setOnClickListener(click);
    }

    public void hideMenu() {
        menu.setVisibility(View.GONE);
    }

    public void editIconBack(int i) {
        back.setImageResource(i);
    }

    public void setText(String text) {
        this.text.setText(text);
    }

    public void setActionForBack(View.OnClickListener lisener) {

        back.setOnClickListener(lisener);

    }

}
