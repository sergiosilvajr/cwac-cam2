package com.commonsware.cwac.cam2.playground.customfragment;

import android.os.Bundle;

import com.commonsware.cwac.cam2.playground.R;
import com.commonsware.cwac.cam2.playground.customfragment.camera_extension.CustomCameraActivity;
/**
 * Created by sergiosilvajr on 14/03/17.
 */

public class MainActivityCustomCamera extends CustomCameraActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getFragmentManager()
               .beginTransaction()
               .add(R.id.current_content, new NoV4SampleFragment())
               .commit();
    }

    @Override
    protected int getCameraIdLayer() {
        return R.id.camera;
    }
}
