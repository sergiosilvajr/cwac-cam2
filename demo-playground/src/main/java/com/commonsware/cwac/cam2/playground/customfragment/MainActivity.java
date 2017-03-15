package com.commonsware.cwac.cam2.playground.customfragment;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.commonsware.cwac.cam2.playground.R;
import com.commonsware.cwac.cam2.playground.customfragment.copypaste.MyCameraActivity;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        getSupportFragmentManager().beginTransaction().add(R.id.camera,new SampleFragment()).commit();

        startActivityForResult(new MyCameraActivity.IntentBuilder(this).build(), 3333);

    }
}
