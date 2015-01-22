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
public class FourPageFragment extends Fragment {


    public FourPageFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_four_page, container, false);
    }


}
