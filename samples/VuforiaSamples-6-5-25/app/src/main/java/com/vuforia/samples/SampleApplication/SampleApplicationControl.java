/*===============================================================================
Copyright (c) 2016-2017 PTC Inc. All Rights Reserved.

Copyright (c) 2012-2014 Qualcomm Connected Experiences, Inc. All Rights Reserved.

Vuforia is a trademark of PTC Inc., registered in the United States and other 
countries.
===============================================================================*/

package com.vuforia.samples.SampleApplication;

import com.vuforia.State;


//  Interface to be implemented by the activity which uses SampleApplicationSession
public interface SampleApplicationControl {

    // To be called to initialize the trackers
    boolean doInitTrackers();


    // To be called to load the trackers' data
    boolean doLoadTrackersData();


    // To be called to start tracking with the initialized trackers and their
    // loaded data
    boolean doStartTrackers();


    //拆分doStartTrackers出一个异常回调
    void doStartCamera();


    // To be called to stop the trackers
    boolean doStopTrackers();


    // To be called to destroy the trackers' data
    boolean doUnloadTrackersData();


    // To be called to deinitialize the trackers
    boolean doDeinitTrackers();


    // This callback is called after the Vuforia initialization is complete,
    // the trackers are initialized, their data loaded and
    // tracking is ready to start
//    void onInitARDone(SampleApplicationException e);
    //初始化AR完成的回调
    void onInitARDone();


    // This callback is called every cycle
    void onVuforiaUpdate(State state);


    // This callback is called on Vuforia resume
    void onVuforiaResumed();


    // This callback is called once Vuforia has been started
    void onVuforiaStarted();

    //拆分onInitARDone出一个异常回调
    void onARException(SampleApplicationException e);

}
