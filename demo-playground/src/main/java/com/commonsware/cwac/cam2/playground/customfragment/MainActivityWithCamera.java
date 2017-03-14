package com.commonsware.cwac.cam2.playground.customfragment;

import android.os.Bundle;

import com.commonsware.cwac.cam2.playground.R;
import com.commonsware.cwac.cam2.playground.customfragment.camera.MyCameraActivity;


public class MainActivityWithCamera extends MyCameraActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        getSupportFragmentManager().beginTransaction().add(R.id.camera,new SampleFragment()).commit();

    }

    @Override
    protected int getCameraIdLayer() {
        return R.id.camera;
    }
}
