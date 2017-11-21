/*===============================================================================
Copyright (c) 2016 PTC Inc. All Rights Reserved.

Copyright (c) 2012-2015 Qualcomm Connected Experiences, Inc. All Rights Reserved.

Vuforia is a trademark of PTC Inc., registered in the United States and other 
countries.
===============================================================================*/


package com.vuforia.samples.VuforiaSamples.ui.ActivityList;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.vuforia.samples.VuforiaSamples.R;
import com.vuforia.samples.VuforiaSamples.utils.SoFileManager;


// This activity starts activities which demonstrate the Vuforia features
public class ActivityLauncher extends ListActivity {

    private String mActivities[] = {"Demo"};


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                R.layout.activities_list_text_view, mActivities);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activities_list);
        setListAdapter(adapter);
    }

    private void toDemo() {
        SoFileManager.loadVuforia(this, new Runnable() {
            @Override
            public void run() {
                toDemoActivity();
            }
        });
    }

    private void toDemoActivity() {
        Intent intent = new Intent();
        String packageName = getPackageName();
        String mClassToLaunch = packageName + "." + "app.demo.DemoActivity";
        intent.setClassName(packageName, mClassToLaunch);
        startActivity(intent);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {

        switch (position) {
            case 0:
                toDemo();
                return;
        }
    }
}
