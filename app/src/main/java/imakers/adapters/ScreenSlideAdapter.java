package imakers.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;

import imakers.fragments.FourPageFragment;
import imakers.fragments.OnePageTutorial;
import imakers.fragments.ThreePageTutorial;
import imakers.fragments.TwoPageTutorial;


//Adapter pro tutorial screening
public class ScreenSlideAdapter extends FragmentPagerAdapter {

    Fragment[] fragments = new Fragment[] { new OnePageTutorial(), new TwoPageTutorial(), new ThreePageTutorial(), new FourPageFragment()};

    public ScreenSlideAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        return fragments[position];
    }

    @Override
    public int getCount() {
        return fragments.length;
    }
}
