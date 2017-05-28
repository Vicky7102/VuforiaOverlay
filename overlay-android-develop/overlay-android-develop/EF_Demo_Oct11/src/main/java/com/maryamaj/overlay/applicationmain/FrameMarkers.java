package com.maryamaj.overlay.applicationmain;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.RelativeLayout;

import com.maryamaj.overlay.api.ARListener;
import com.maryamaj.overlay.api.Control;
import com.maryamaj.overlay.applicationsettings.SampleApplicationControl;
import com.maryamaj.overlay.applicationsettings.SampleApplicationException;
import com.maryamaj.overlay.applicationsettings.SampleApplicationSession;
import com.maryamaj.overlay.gestures.ClickDetector;
import com.maryamaj.overlay.models.Point2D;
import com.maryamaj.overlay.utils.LoadingDialogHandler;
import com.maryamaj.overlay.utils.SampleApplicationGLView;
import com.maryamaj.ubitile_marker.R;
import com.vuforia.CameraDevice;
import com.vuforia.DataSet;
import com.vuforia.ObjectTracker;
import com.vuforia.STORAGE_TYPE;
import com.vuforia.State;
import com.vuforia.Trackable;
import com.vuforia.Tracker;
import com.vuforia.TrackerManager;
import com.vuforia.Vuforia;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;


// The main activity for the FrameMarkers sample.
public class FrameMarkers extends Activity implements SampleApplicationControl, Control {

    private android.graphics.Point screensize;
    private SampleApplicationSession vuforiaAppSession;
    // Our OpenGL view:
    private SampleApplicationGLView mGlView;
    // Our renderer:
    private ImageTargetRenderer mRenderer;

    private RelativeLayout mUILayout;

    private DataSet mCurrentDataset;

    private GestureDetector clickDetector;
    private Realm realm;

    LoadingDialogHandler loadingDialogHandler = new LoadingDialogHandler(this);

    // Alert Dialog used to display SDK errors
    private AlertDialog mErrorDialog;
    boolean mIsDroidDevice = false;
    private static final String TAG = "FrameMarkers";
    private List<ARListener> overlays;
    private Point2D markerCenter;
    private float depth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        markerCenter = new Point2D();
        realm = Realm.getDefaultInstance();
        TutorialManager.init(this);
        Display display = getWindowManager().getDefaultDisplay();
        screensize = new android.graphics.Point();
        display.getSize(screensize);

        vuforiaAppSession = new SampleApplicationSession(this);

        startLoadingAnimation();

        vuforiaAppSession.initAR(this, ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        clickDetector = new GestureDetector(this, new ClickDetector());

        mIsDroidDevice = android.os.Build.MODEL.toLowerCase().startsWith("droid");

        overlays = new ArrayList<>();
        overlays.add((ARListener) findViewById(R.id.text_overlay));
        overlays.add((ARListener) findViewById(R.id.sketch_overlay));
        overlays.add((ARListener) findViewById(R.id.audio_overlay));
        overlays.add((ARListener) findViewById(R.id.area_overlay));
        overlays.add((ARListener) findViewById(R.id.input_options_overlay));
        overlays.add((ARListener) findViewById(R.id.overview_overlay));
        overlays.add((ARListener) findViewById(R.id.navigation_overlay));
        overlays.add((ARListener) findViewById(R.id.switch_mode_overlay));
        overlays.add((ARListener) findViewById(R.id.follow_arrow_overlay));
    }

    @Override
    public void pauseCamera() {
        mRenderer.setVideoRenderPaused(true);
        for (ARListener listener : overlays) {
            listener.onPauseCamera();
        }
//        try {
//            vuforiaAppSession.pauseAR();
//        } catch (SampleApplicationException e) {
//            e.printStackTrace();
//        }
    }

    @Override
    public void resumeCamera() {
//        try {
//            vuforiaAppSession.resumeAR();
//        } catch (SampleApplicationException e) {
//            e.printStackTrace();
//        }
        mRenderer.setVideoRenderPaused(false);
        for (ARListener listener : overlays) {
            listener.onResumeCamera();
        }
    }

    @Override
    public boolean isCameraPaused() {
        return mRenderer == null || mRenderer.isVideoRenderPaused();
    }

    @Override
    public Realm getRealm() {
        if (realm.isClosed()) {
            realm = Realm.getDefaultInstance();
        }
        return realm;
    }

    // Called when the activity will start interacting with the user.
    @Override
    protected void onResume() {
        Log.d(TAG, "onResume");
        super.onResume();

        // This is needed for some Droid devices to force portrait
        if (mIsDroidDevice) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        try {
            vuforiaAppSession.resumeAR();
        } catch (SampleApplicationException e) {
            Log.e(TAG, e.getString());
        }

        // Resume the GL view:
        if (mGlView != null) {
            mGlView.setVisibility(View.VISIBLE);
            mGlView.onResume();
        }

        // area definition not active by default

        //		showView.bringToFront();

    }

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
        realm.close();
        System.gc();
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (clickDetector.onTouchEvent(event)) {
            Handler autofocusHandler = new Handler();
            autofocusHandler.post(() -> {
                boolean result = CameraDevice.getInstance().setFocusMode(CameraDevice.FOCUS_MODE.FOCUS_MODE_TRIGGERAUTO);
                if (!result)
                    Log.e("SingleTapUp", "Unable to trigger focus");
            });

        }
        return false;
    }

    public Point2D getMarkerCenter() {
        return markerCenter;
    }

    @Override
    public float getDepth() {
        return depth;
    }

    public void setMarkerCenter(Point2D markerCenter, float depth) {
        if (mRenderer.isVideoRenderPaused())
            return;
        this.markerCenter = markerCenter;
        this.depth = depth;
        for (ARListener listener : overlays) {
            listener.setMarkerCenter(markerCenter, depth, 0);
        }
    }

    private void startLoadingAnimation() {
        mUILayout = (RelativeLayout) View.inflate(this, R.layout.camera_overlay, null);
        mUILayout.setVisibility(View.VISIBLE);
        mUILayout.setBackgroundColor(Color.BLACK);
        loadingDialogHandler.mLoadingDialogContainer = mUILayout.findViewById(R.id.loading_indicator);
        // Shows the loading indicator at start
        loadingDialogHandler.sendEmptyMessage(LoadingDialogHandler.SHOW_LOADING_DIALOG);
        addContentView(mUILayout, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
    }

    // Initializes AR application components.
    private void initApplicationAR() {
        // Create OpenGL ES view:
        int depthSize = 16;
        int stencilSize = 0;
        boolean translucent = Vuforia.requiresAlpha();
        mGlView = new SampleApplicationGLView(this);
        mGlView.init(translucent, depthSize, stencilSize);

        mRenderer = new ImageTargetRenderer(this, vuforiaAppSession);
        mGlView.setRenderer(mRenderer);
    }

    @Override
    public boolean doInitTrackers() {
        // Indicate if the trackers were initialized correctly
        boolean result = true;

        // Initialize the marker tracker:
        TrackerManager trackerManager = TrackerManager.getInstance();
        Tracker tracker = trackerManager.initTracker(ObjectTracker.getClassType());
        if (tracker == null) {
            Log.e(TAG,
                    "Tracker not initialized. Tracker already initialized or the camera is already started");
            result = false;
        } else {
            Log.i(TAG, "Tracker successfully initialized");
        }
        return result;
    }

    @Override
    public boolean doLoadTrackersData() {
        TrackerManager tManager = TrackerManager.getInstance();

        ObjectTracker objectTracker = (ObjectTracker) tManager.getTracker(ObjectTracker.getClassType());
        if(!objectTracker.persistExtendedTracking(true)) {
            Log.d(TAG, "Persistent extended tracking NOT enabled");
        }
        else {
            Log.d(TAG, "Persistent extended tracking enabled");
        }

        if (objectTracker == null)
            return false;

        if (mCurrentDataset == null)
            mCurrentDataset = objectTracker.createDataSet();

        if (mCurrentDataset == null)
            return false;

        if (!mCurrentDataset.load("Image/Image.xml", STORAGE_TYPE.STORAGE_APPRESOURCE))
            return false;

        if (!objectTracker.activateDataSet(mCurrentDataset))
            return false;

        int numTrackables = mCurrentDataset.getNumTrackables();
        for (int count = 0; count < numTrackables; count++) {
            Trackable trackable = mCurrentDataset.getTrackable(count);
            if (!trackable.startExtendedTracking()) {
                Log.d(TAG, "Failed to start extended tracking");
            } else {
                Log.d(TAG, "Extended tacking started");
            }
            String name = "Current Dataset : " + trackable.getName();
            trackable.setUserData(name);
            Log.d(TAG, "UserData:Set the following user data " + trackable.getUserData());
        }

        return true;
    }


    @Override
    public boolean doStartTrackers() {
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
    public boolean doUnloadTrackersData() {
        // Indicate if the trackers were unloaded correctly
        return true;
    }


    @Override
    public boolean doDeinitTrackers() {
        // Indicate if the trackers were deinitialized correctly
        TrackerManager tManager = TrackerManager.getInstance();
//        tManager.deinitTracker(MarkerTracker.getClassType());
        tManager.deinitTracker(ObjectTracker.getClassType());
        return true;
    }


    @Override
    public void onInitARDone(SampleApplicationException exception) {

        if (exception == null) {
            initApplicationAR();

            mRenderer.setActive(true);

            // Now add the GL surface view. It is important
            // that the OpenGL ES surface view gets added
            // BEFORE the camera is started and video
            // background is configured.
            addContentView(mGlView, new LayoutParams(LayoutParams.MATCH_PARENT,
                    LayoutParams.MATCH_PARENT));

            // Sets the UILayout to be drawn in front of the camera
            mUILayout.bringToFront();
            //			drawingView.bringToFront();

            // Sets the layout background to transparent
            mUILayout.setBackgroundColor(Color.TRANSPARENT);

            try {
                vuforiaAppSession.startAR(CameraDevice.CAMERA_DIRECTION.CAMERA_DIRECTION_DEFAULT);
            } catch (SampleApplicationException e) {
                Log.e(TAG, e.getString());
            }

            boolean result = CameraDevice.getInstance().setFocusMode(
                    CameraDevice.FOCUS_MODE.FOCUS_MODE_CONTINUOUSAUTO);

            if (!result)
                Log.e(TAG, "Unable to enable continuous autofocus");


        } else {
            Log.e(TAG, exception.getString());
            showInitializationErrorMessage(exception.getString());
        }
    }

    // Shows initialization error messages as System dialogs
    public void showInitializationErrorMessage(String message) {
        final String errorMessage = message;
        runOnUiThread(() -> {
            if (mErrorDialog != null) {
                mErrorDialog.dismiss();
            }

            // Generates an Alert Dialog to show the error message
            AlertDialog.Builder builder = new AlertDialog.Builder(
                    FrameMarkers.this);
            builder
                    .setMessage(errorMessage)
                    .setTitle(getString(R.string.INIT_ERROR))
                    .setCancelable(false)
                    .setIcon(0)
                    .setPositiveButton(getString(R.string.button_OK),
                            (dialog, id) -> finish());

            mErrorDialog = builder.create();
            mErrorDialog.show();
        });
    }


    @Override
    public void onVuforiaUpdate(State state) {    // This callback is called every cycle
    }

    public android.graphics.Point getScreensize() {
        return screensize;
    }
}
