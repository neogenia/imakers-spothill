package imakers.fragments;



import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import imakers.beacons.R;

/**
 * A simple {@link Fragment} subclass.
 *
 */
public class OnePageTutorial extends Fragment {


    public OnePageTutorial() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_one_page_tutorial, container, false);
    }


}
