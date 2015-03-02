package imakers.beacons;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import imakers.fragments.FavoriteFragment;
import imakers.fragments.HistoryFragment;
import imakers.fragments.HomeFragment;
import imakers.fragments.ReminderFragment;
import imakers.tools.MyActionPanel;
import imakers.tools.MyApplication;


public class MainActivity extends Activity {

    //SwipeListView swipelistview;

    MyActionPanel panel;

    HomeFragment fragment;

    int actual;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        ((MyApplication)getApplicationContext()).setMainActivity(this);

        ((MyApplication)getApplicationContext()).setCanHide(true);


        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
        } else {
            if (!mBluetoothAdapter.isEnabled()) {
                Toast.makeText(this, getString(R.string.bt_not_activated), Toast.LENGTH_LONG).show();
            }
        }

        actual = 0;

        panel = new MyActionPanel(this, getString(R.string.campaigns_title));

        panel.hideBack();
        panel.showMenu(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, MenuActivity.class));
            }
        });


        // přepínání mezi obrazovkama
        findViewById(R.id.menu_three).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(actual != 2) {
                    reset();
                    favorite();
                    actual = 2;
                    panel.setText(getString(R.string.favourites_title));
                    ((ImageView)findViewById(R.id.menu_three)).setImageResource(R.drawable.ic_menu_three_active);
                    ((ImageView)findViewById(R.id.menu_three)).setBackgroundColor(Color.parseColor("#e9e6e6"));
                }


            }
        });

        findViewById(R.id.menu_one).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(actual != 0) {

                    reset();
                    home();
                    actual = 0;
                    panel.setText(getString(R.string.campaigns_title));
                    ((ImageView)findViewById(R.id.menu_one)).setImageResource(R.drawable.ic_menu_first);
                    ((ImageView)findViewById(R.id.menu_one)).setBackgroundColor(Color.parseColor("#e9e6e6"));
                }
            }
        });

        findViewById(R.id.menu_four).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(actual != 3) {
                    reset();
                    historie();
                    actual = 3;
                    panel.setText(getString(R.string.history_title));
                    ((ImageView)findViewById(R.id.menu_four)).setImageResource(R.drawable.ic_menu_four_active);
                    ((ImageView)findViewById(R.id.menu_four)).setBackgroundColor(Color.parseColor("#e9e6e6"));
                }
            }
        });

        findViewById(R.id.menu_two).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(actual != 1) {
                    reset();
                    reminder();
                    actual = 1;
                    panel.setText(getString(R.string.reminder_title));
                    ((ImageView)findViewById(R.id.menu_two)).setImageResource(R.drawable.ic_menu_two_active);
                    ((ImageView)findViewById(R.id.menu_two)).setBackgroundColor(Color.parseColor("#e9e6e6"));
                }
            }
        });

        home();


    }

    void reset() {
        ((ImageView)findViewById(R.id.menu_one)).setBackgroundColor(Color.TRANSPARENT);
        ((ImageView)findViewById(R.id.menu_two)).setBackgroundColor(Color.TRANSPARENT);
        ((ImageView)findViewById(R.id.menu_three)).setBackgroundColor(Color.TRANSPARENT);
        ((ImageView)findViewById(R.id.menu_four)).setBackgroundColor(Color.TRANSPARENT);
        ((ImageView)findViewById(R.id.menu_one)).setImageResource(R.drawable.ic_menu_first_no_active);
        ((ImageView)findViewById(R.id.menu_two)).setImageResource(R.drawable.ic_menu_two);
        ((ImageView)findViewById(R.id.menu_three)).setImageResource(R.drawable.ic_menu_three);
        ((ImageView)findViewById(R.id.menu_four)).setImageResource(R.drawable.ic_menu_four);


    }



    void home() {

        HomeFragment newFragment = new HomeFragment();


        FragmentTransaction transaction = getFragmentManager().beginTransaction();


        if(fragment == null) {
            fragment = newFragment;
        }
        else {
            newFragment = fragment;
        }

        transaction.replace(R.id.frame_layout, newFragment);

        transaction.commit();

    }


    void favorite() {

        Fragment newFragment = new FavoriteFragment();
        FragmentTransaction transaction = getFragmentManager().beginTransaction();

        transaction.replace(R.id.frame_layout, newFragment);

        transaction.commit();

    }

    void historie() {

        Fragment newFragment = new HistoryFragment();
        FragmentTransaction transaction = getFragmentManager().beginTransaction();

        transaction.replace(R.id.frame_layout, newFragment);

        transaction.commit();

    }

    void reminder() {

        Fragment newFragment = new ReminderFragment();
        FragmentTransaction transaction = getFragmentManager().beginTransaction();

        transaction.replace(R.id.frame_layout, newFragment);

        transaction.commit();

    }

}
