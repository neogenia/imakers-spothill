package imakers.beacons;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.viewpagerindicator.CirclePageIndicator;

import imakers.adapters.ScreenSlideAdapter;
import imakers.beacons.R;

public class TutorialActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorial);

        findViewById(R.id.close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mainIntent = new Intent(TutorialActivity.this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                TutorialActivity.this.startActivity(mainIntent);
                TutorialActivity.this.finish();
            }
        });

        ViewPager pager = (ViewPager)findViewById(R.id.pager);

        pager.setAdapter(new ScreenSlideAdapter(getSupportFragmentManager()));
        CirclePageIndicator indicator = (CirclePageIndicator)findViewById(R.id.titles);
        indicator.setViewPager(pager);

    }

}
