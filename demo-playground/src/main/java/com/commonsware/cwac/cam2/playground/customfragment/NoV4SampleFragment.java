package com.commonsware.cwac.cam2.playground.customfragment;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.commonsware.cwac.cam2.playground.R;

/**
 * Created by sergiosilvajr on 15/03/17.
 */

public class NoV4SampleFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sample_layout,container, false);
    }
}
