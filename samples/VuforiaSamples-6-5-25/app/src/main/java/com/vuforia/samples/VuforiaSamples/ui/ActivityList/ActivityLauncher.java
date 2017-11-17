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

    private String mActivities[] = {"Demo", "Image Targets", "VuMark", "Cylinder Targets",
            "Multi Targets", "User Defined Targets", "Object Reco", "Cloud Reco",
            "Text Reco", "Virtual Buttons"};


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
        SoFileManager.copyVuforia(this);
        if (SoFileManager.loadVuforia(this)) {
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    toDemoActivity();
                }
            });
        } else {
            Toast.makeText(this, "没有驱动文件", Toast.LENGTH_SHORT).show();
        }
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

        Intent intent = new Intent(this, AboutScreen.class);
        intent.putExtra("ABOUT_TEXT_TITLE", mActivities[position]);

        switch (position) {
            case 0:
                toDemo();
                return;
            case 1:
                intent.putExtra("ACTIVITY_TO_LAUNCH",
                        "app.ImageTargets.ImageTargets");
                intent.putExtra("ABOUT_TEXT", "ImageTargets/IT_about.html");
                break;
            case 2:
                intent.putExtra("ACTIVITY_TO_LAUNCH",
                        "app.VuMark.VuMark");
                intent.putExtra("ABOUT_TEXT", "VuMark/VM_about.html");
                break;
            case 3:
                intent.putExtra("ACTIVITY_TO_LAUNCH",
                        "app.CylinderTargets.CylinderTargets");
                intent.putExtra("ABOUT_TEXT", "CylinderTargets/CY_about.html");
                break;
            case 4:
                intent.putExtra("ACTIVITY_TO_LAUNCH",
                        "app.MultiTargets.MultiTargets");
                intent.putExtra("ABOUT_TEXT", "MultiTargets/MT_about.html");
                break;
            case 5:
                intent.putExtra("ACTIVITY_TO_LAUNCH",
                        "app.UserDefinedTargets.UserDefinedTargets");
                intent.putExtra("ABOUT_TEXT",
                        "UserDefinedTargets/UD_about.html");
                break;
            case 6:
                intent.putExtra("ACTIVITY_TO_LAUNCH",
                        "app.ObjectRecognition.ObjectTargets");
                intent.putExtra("ABOUT_TEXT", "ObjectRecognition/OR_about.html");
                break;
            case 7:
                intent.putExtra("ACTIVITY_TO_LAUNCH",
                        "app.CloudRecognition.CloudReco");
                intent.putExtra("ABOUT_TEXT", "CloudReco/CR_about.html");
                break;
            case 8:
                intent.putExtra("ACTIVITY_TO_LAUNCH",
                        "app.TextRecognition.TextReco");
                intent.putExtra("ABOUT_TEXT", "TextReco/TR_about.html");
                break;
            case 9:
                intent.putExtra("ACTIVITY_TO_LAUNCH",
                        "app.VirtualButtons.VirtualButtons");
                intent.putExtra("ABOUT_TEXT", "VirtualButtons/VB_about.html");
                break;
        }

        startActivity(intent);

    }
}
