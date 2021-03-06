package com.vuforia.samples.VuforiaSamples.app.demo;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Toast;

import com.vuforia.CameraDevice;
import com.vuforia.DataSet;
import com.vuforia.ObjectTracker;
import com.vuforia.STORAGE_TYPE;
import com.vuforia.State;
import com.vuforia.Trackable;
import com.vuforia.Tracker;
import com.vuforia.TrackerManager;
import com.vuforia.Vuforia;
import com.vuforia.samples.SampleApplication.SampleApplicationControl;
import com.vuforia.samples.SampleApplication.SampleApplicationException;
import com.vuforia.samples.SampleApplication.SampleApplicationSession;
import com.vuforia.samples.SampleApplication.utils.Texture;
import com.vuforia.samples.VuforiaSamples.R;

import java.util.ArrayList;
import java.util.Vector;

/**
 *
 */
public class DemoActivity extends Activity implements SampleApplicationControl {
    private static final String TAG = "DemoActivity";

    SampleApplicationSession vuforiaAppSession;

    private DataSet mCurrentDataset;
    private int mCurrentDatasetSelectionIndex = 0;
    private ArrayList<String> mDatasetStrings = new ArrayList<String>();

    // Our OpenGL view:
    private DemoGLView mGlView;

    // Our renderer:
    private DemoRenderer mRenderer;

    private GestureDetector mGestureDetector;

    // The textures we will use for rendering:
    private Vector<Texture> mTextures;

    private boolean mSwitchDatasetAsap = false;
    private boolean mExtendedTracking = false;

    private View mloadingLayout;
    private ViewGroup mContainer;

    private final float TOUCH_SCALE_FACTOR = 180.0f / 320;

    // Called when the activity first starts or the user navigates back to an
    // activity.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);

        vuforiaAppSession = new SampleApplicationSession(this);
        setContentView(R.layout.demo);
//        mDatasetStrings.add("StonesAndChips.xml");
        mDatasetStrings.add("Dmall.xml");
        mDatasetStrings.add("Tarmac.xml");

        vuforiaAppSession.initAR(this);
        mGestureDetector = new GestureDetector(this, new GestureListener());
        // Load any sample specific textures:
        mTextures = new Vector<Texture>();
        loadTextures();
        initView();
    }

    // Process Single Tap event to trigger autofocus
    private class GestureListener extends
            GestureDetector.SimpleOnGestureListener {
        // Used to set autofocus one second after a manual focus is triggered
        private final Handler autofocusHandler = new Handler();


        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }


        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            float dx = distanceX;
            float dy = distanceY;

            mRenderer.setX(mRenderer.getX() + (dx * TOUCH_SCALE_FACTOR));
            mRenderer.setY(mRenderer.getY() - (dy * TOUCH_SCALE_FACTOR));
            mGlView.requestRender();

//            return super.onScroll(e1, e2, distanceX, distanceY);
            return true;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            // Verify that the tap happened inside the target
            if (mRenderer != null && mRenderer.isTapOnScreenInsideTarget(e.getX(),
                    e.getY())) {
                autofocusHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "clicked", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                boolean result = CameraDevice.getInstance().setFocusMode(
                        CameraDevice.FOCUS_MODE.FOCUS_MODE_TRIGGERAUTO);
                if (!result)
                    Log.e("SingleTapConfirmed", "Unable to trigger focus");

                // Generates a Handler to trigger continuous auto-focus
                // after 1 second
                autofocusHandler.postDelayed(new Runnable() {
                    public void run() {
                        final boolean autofocusResult = CameraDevice.getInstance().setFocusMode(
                                CameraDevice.FOCUS_MODE.FOCUS_MODE_CONTINUOUSAUTO);

                        if (!autofocusResult)
                            Log.e("SingleTapConfirmed", "Unable to re-enable continuous auto-focus");
                    }
                }, 1000L);
            }
            return true;
        }
    }


    // We want to load specific textures from the APK, which we will later use
    // for rendering.

    private void loadTextures() {
        mTextures.add(Texture.loadTextureFromApk("patrick/patrick.png",
                getAssets()));
//        mTextures.add(Texture.loadTextureFromApk("ImageTargets/Buildings.jpeg",
//                getAssets()));
    }


    // Called when the activity will start interacting with the user.
    @Override
    protected void onResume() {
        Log.d(TAG, "onResume");
        super.onResume();

        showLoading(true);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        vuforiaAppSession.onResume();
    }


    // Callback for configuration changes the activity handles itself
    @Override
    public void onConfigurationChanged(Configuration config) {
        Log.d(TAG, "onConfigurationChanged");
        super.onConfigurationChanged(config);

        vuforiaAppSession.onConfigurationChanged();
    }


    // Called when the system is about to start resuming a previous activity.
    @Override
    protected void onPause() {
        Log.d(TAG, "onPause");
        super.onPause();

        if (mGlView != null) {
            mGlView.setVisibility(View.INVISIBLE);
            mGlView.onPause();
        }

        try {
            vuforiaAppSession.pauseAR();
        } catch (SampleApplicationException e) {
            Log.e(TAG, e.getString());
        }
    }


    // The final call you receive before your activity is destroyed.
    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy");
        super.onDestroy();

        try {
            vuforiaAppSession.stopAR();
        } catch (SampleApplicationException e) {
            Log.e(TAG, e.getString());
        }

        // Unload texture:
        mTextures.clear();
        mTextures = null;

        System.gc();
    }

    private void initView() {
        mloadingLayout = findViewById(R.id.ll_loading);
        mloadingLayout.setVisibility(View.VISIBLE);
        mContainer = (ViewGroup) findViewById(R.id.rl_container);

        // Create OpenGL ES view:
        int depthSize = 16;
        int stencilSize = 0;
        boolean translucent = Vuforia.requiresAlpha();

        mGlView = new DemoGLView(this);
        mGlView.init(translucent, depthSize, stencilSize);

        mRenderer = new DemoRenderer(this, vuforiaAppSession);
        mRenderer.setActive(true);
        mRenderer.setTextures(mTextures);
        mGlView.setRenderer(mRenderer);

        mContainer.removeAllViews();
        mContainer.addView(mGlView, new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT));
        mRenderer.updateConfiguration();
    }


    // Methods to load and destroy tracking data.
    @Override
    public boolean doLoadTrackersData() {
        TrackerManager tManager = TrackerManager.getInstance();
        ObjectTracker objectTracker = (ObjectTracker) tManager
                .getTracker(ObjectTracker.getClassType());
        if (objectTracker == null)
            return false;

        if (mCurrentDataset == null)
            mCurrentDataset = objectTracker.createDataSet();

        if (mCurrentDataset == null)
            return false;

        if (!mCurrentDataset.load(
                mDatasetStrings.get(mCurrentDatasetSelectionIndex),
                STORAGE_TYPE.STORAGE_APPRESOURCE))
            return false;

        if (!objectTracker.activateDataSet(mCurrentDataset))
            return false;

        int numTrackables = mCurrentDataset.getNumTrackables();
        for (int count = 0; count < numTrackables; count++) {
            Trackable trackable = mCurrentDataset.getTrackable(count);
            if (isExtendedTrackingActive()) {
                trackable.startExtendedTracking();
            }

            String name = "Current Dataset : " + trackable.getName();
            trackable.setUserData(name);
            Log.d(TAG, "UserData:Set the following user data "
                    + (String) trackable.getUserData());
        }

        return true;
    }


    @Override
    public boolean doUnloadTrackersData() {
        // Indicate if the trackers were unloaded correctly
        boolean result = true;

        TrackerManager tManager = TrackerManager.getInstance();
        ObjectTracker objectTracker = (ObjectTracker) tManager
                .getTracker(ObjectTracker.getClassType());
        if (objectTracker == null)
            return false;

        if (mCurrentDataset != null && mCurrentDataset.isActive()) {
            if (objectTracker.getActiveDataSet(0).equals(mCurrentDataset)
                    && !objectTracker.deactivateDataSet(mCurrentDataset)) {
                result = false;
            } else if (!objectTracker.destroyDataSet(mCurrentDataset)) {
                result = false;
            }

            mCurrentDataset = null;
        }

        return result;
    }

    @Override
    public void onVuforiaResumed() {
        if (mGlView != null) {
            showLoading(false);
            mGlView.setVisibility(View.VISIBLE);
            mGlView.onResume();
        }
    }

    @Override
    public void onVuforiaStarted() {
        Log.w(TAG, "onVuforiaStarted: ");
        mRenderer.updateConfiguration();

        // Set camera focus mode
        if (!CameraDevice.getInstance().setFocusMode(CameraDevice.FOCUS_MODE.FOCUS_MODE_CONTINUOUSAUTO)) {
            // If continuous autofocus mode fails, attempt to set to a different mode
            if (!CameraDevice.getInstance().setFocusMode(CameraDevice.FOCUS_MODE.FOCUS_MODE_TRIGGERAUTO)) {
                CameraDevice.getInstance().setFocusMode(CameraDevice.FOCUS_MODE.FOCUS_MODE_NORMAL);
            }
        }
//        showLoading(false);
    }


    public void showLoading(boolean show) {
        if (show) {
            mloadingLayout.setVisibility(View.VISIBLE);
        } else {
            mloadingLayout.setVisibility(View.GONE);
        }
    }


    @Override
    public void onInitARDone() {
        Log.w(TAG, "onInitARDone: ");
        showLoading(false);
        mRenderer.setHasInitAr(true);
        mRenderer.setActive(true);
        mGlView.requestRender();
        vuforiaAppSession.startAR(CameraDevice.CAMERA_DIRECTION.CAMERA_DIRECTION_DEFAULT);
    }

    @Override
    public void onARException(SampleApplicationException exception) {
        if (exception != null) {
            Log.e(TAG, exception.getString());
            showInitializationErrorMessage(exception.getString());
        }
    }

    // Shows initialization error messages as System dialogs
    public void showInitializationErrorMessage(String message) {
        showToast(message);
    }


    @Override
    public void onVuforiaUpdate(State state) {
        if (mSwitchDatasetAsap) {
            mSwitchDatasetAsap = false;
            TrackerManager tm = TrackerManager.getInstance();
            ObjectTracker ot = (ObjectTracker) tm.getTracker(ObjectTracker
                    .getClassType());
            if (ot == null || mCurrentDataset == null
                    || ot.getActiveDataSet(0) == null) {
                Log.d(TAG, "Failed to swap datasets");
                return;
            }

            doUnloadTrackersData();
            doLoadTrackersData();
        }
    }

    @Override
    public void doStartCamera(){
        vuforiaAppSession.startCamera(CameraDevice.CAMERA_DIRECTION.CAMERA_DIRECTION_DEFAULT);
    }

    @Override
    public boolean doInitTrackers() {
        Log.w(TAG, "doInitTrackers: ");
        // Indicate if the trackers were initialized correctly
        boolean result = true;

        TrackerManager tManager = TrackerManager.getInstance();
        Tracker tracker;

        // Trying to initialize the image tracker
        tracker = tManager.initTracker(ObjectTracker.getClassType());
        if (tracker == null) {
            Log.e(
                    TAG,
                    "Tracker not initialized. Tracker already initialized or the camera is already started");
            result = false;
        } else {
            Log.i(TAG, "Tracker successfully initialized");
        }
        return result;
    }


    @Override
    public boolean doStartTrackers() {
        Log.w(TAG, "doStartTrackers: ");
        // Indicate if the trackers were started correctly
        boolean result = true;

        Tracker objectTracker = TrackerManager.getInstance().getTracker(
                ObjectTracker.getClassType());
        if (objectTracker != null)
            objectTracker.start();

        return result;
    }


    @Override
    public boolean doStopTrackers() {
        // Indicate if the trackers were stopped correctly
        boolean result = true;

        Tracker objectTracker = TrackerManager.getInstance().getTracker(
                ObjectTracker.getClassType());
        if (objectTracker != null)
            objectTracker.stop();

        return result;
    }


    @Override
    public boolean doDeinitTrackers() {
        // Indicate if the trackers were deinitialized correctly
        boolean result = true;

        TrackerManager tManager = TrackerManager.getInstance();
        tManager.deinitTracker(ObjectTracker.getClassType());

        return result;
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return mGestureDetector.onTouchEvent(event);
    }


    boolean isExtendedTrackingActive() {
        return mExtendedTracking;
    }

    private void showToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }
}
