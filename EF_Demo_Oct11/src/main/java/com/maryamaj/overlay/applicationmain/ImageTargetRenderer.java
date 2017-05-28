/*===============================================================================
Copyright (c) 2016 PTC Inc. All Rights Reserved.

Copyright (c) 2012-2014 Qualcomm Connected Experiences, Inc. All Rights Reserved.

Vuforia is a trademark of PTC Inc., registered in the United States and other 
countries.
===============================================================================*/

package com.maryamaj.overlay.applicationmain;

import android.content.res.Configuration;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

import com.maryamaj.overlay.applicationsettings.SampleApplicationSession;
import com.maryamaj.overlay.models.Point2D;
import com.maryamaj.overlay.utils.LoadingDialogHandler;
import com.maryamaj.overlay.utils.SampleUtils;
import com.vuforia.CameraDevice;
import com.vuforia.Device;
import com.vuforia.Matrix44F;
import com.vuforia.Renderer;
import com.vuforia.State;
import com.vuforia.Tool;
import com.vuforia.Trackable;
import com.vuforia.TrackableResult;
import com.vuforia.Vec2F;
import com.vuforia.Vec3F;
import com.vuforia.VideoBackgroundConfig;
import com.vuforia.VideoMode;
import com.vuforia.Vuforia;

import java.util.Arrays;
import java.util.Vector;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;


// The renderer class for the ImageTargets sample. 
public class ImageTargetRenderer implements GLSurfaceView.Renderer, SampleAppRendererControl {
    private static final String TAG = "ImageTargetRenderer";

    private SampleApplicationSession vuforiaAppSession;
    private FrameMarkers mActivity;
    private SampleAppRenderer mSampleAppRenderer;

    private int shaderProgramID;
    private int vertexHandle;
    private int textureCoordHandle;
    private int mvpMatrixHandle;
    private int texSampler2DHandle;

    private boolean mIsActive = false;
    private boolean mModelIsLoaded = false;

    private static final float OBJECT_SCALE_FLOAT = 0.003f;


    public ImageTargetRenderer(FrameMarkers activity, SampleApplicationSession session) {
        mActivity = activity;
        vuforiaAppSession = session;
        // SampleAppRenderer used to encapsulate the use of RenderingPrimitives setting
        // the device mode AR/VR and stereo mode
        mSampleAppRenderer = new SampleAppRenderer(this, mActivity, Device.MODE.MODE_AR, false, 0.01f, 5f);
    }


    // Called to draw the current frame.
    @Override
    public void onDrawFrame(GL10 gl) {
        if (!mIsActive)
            return;

        // Call our function to render content from SampleAppRenderer class
        mSampleAppRenderer.render();
    }


    public void setActive(boolean active) {
        mIsActive = active;

        if (mIsActive)
            mSampleAppRenderer.configureVideoBackground();
    }


    // Called when the surface is created or recreated.
    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        Log.d(TAG, "GLRenderer.onSurfaceCreated");

        // Call Vuforia function to (re)initialize rendering after first use
        // or after OpenGL ES context was lost (e.g. after onPause/onResume):
        vuforiaAppSession.onSurfaceCreated();

        mSampleAppRenderer.onSurfaceCreated();
    }


    // Called when the surface changed size.
    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        Log.d(TAG, "GLRenderer.onSurfaceChanged");

        // Call Vuforia function to handle render surface size changes:
        vuforiaAppSession.onSurfaceChanged(width, height);

        // RenderingPrimitives to be updated when some rendering change is done
        mSampleAppRenderer.onConfigurationChanged(mIsActive);

        initRendering();
    }


    // Function for initializing the renderer.
    private void initRendering() {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, Vuforia.requiresAlpha() ? 0.0f
                : 1.0f);
        // Hide the Loading Dialog
        mActivity.loadingDialogHandler.sendEmptyMessage(LoadingDialogHandler.HIDE_LOADING_DIALOG);
    }

    public void updateConfiguration() {
        mSampleAppRenderer.onConfigurationChanged(mIsActive);
    }

    // The render function called from SampleAppRendering by using RenderingPrimitives views.
    // The state is owned by SampleAppRenderer which is controlling it's lifecycle.
    // State should not be cached outside this method.

    public boolean isVideoRenderPaused() {
        return mSampleAppRenderer.isVideoRenderPaused();
    }

    public void setVideoRenderPaused(boolean videoRenderPaused) {
        mSampleAppRenderer.setVideoRenderPaused(videoRenderPaused);
    }

    public void renderFrame(State state, float[] projectionMatrix) {
        // Renders video background replacing Renderer.DrawVideoBackground()
        mSampleAppRenderer.renderVideoBackground();

        GLES20.glEnable(GLES20.GL_DEPTH_TEST);

        // handle face culling, we need to detect if we are using reflection
        // to determine the direction of the culling
        GLES20.glEnable(GLES20.GL_CULL_FACE);
        GLES20.glCullFace(GLES20.GL_BACK);

        // Did we find any trackables this frame?
        for (int tIdx = 0; tIdx < state.getNumTrackableResults(); tIdx++) {
            TrackableResult result = state.getTrackableResult(tIdx);
//            Trackable trackable = result.getTrackable();
//            printUserData(trackable);
            Matrix44F modelViewMatrix_Vuforia = Tool
                    .convertPose2GLMatrix(result.getPose());
            float[] modelViewMatrix = modelViewMatrix_Vuforia.getData();
            // deal with the modelview and projection matrices
//            float[] modelViewProjection = new float[16];

//            Matrix.multiplyMM(modelViewProjection, 0, projectionMatrix, 0, modelViewMatrix, 0);
//            float[] projection = new float[4];
//            Matrix.multiplyMV(projection, 0, modelViewProjection, 0, new float[]{0.0f, 0.0f, 0.0f, 1}, 0);
//            Log.d(TAG, "Projection: " + Arrays.toString(projection));
            Vec2F cameraCoord = Tool.projectPoint(vuforiaAppSession.getCameraCalib(), result.getPose(), new Vec3F(0.0f, 0.0f, 0.0f));
            int xCoord = (int) cameraPointToScreen(cameraCoord).getData()[0];
            int yCoord = (int) cameraPointToScreen(cameraCoord).getData()[1];
//            Log.d(TAG, "Projected point: " + Arrays.toString(cameraCoord.getData()));
            Point2D refCoords = new Point2D(xCoord, yCoord);
//            Log.d(TAG, "Projected point on screen: " + xCoord + ", " + yCoord);
            // set the position of the framemarker on the screen for calculating the relative area positions in drawingView
            mActivity.setMarkerCenter(refCoords, modelViewMatrix[14]);
            // activate the shader program and bind the vertex/normal/tex coords

            SampleUtils.checkGLError("Render Frame");

        }

        GLES20.glDisable(GLES20.GL_DEPTH_TEST);

    }

    public Vec2F cameraPointToScreen(Vec2F cameraPoint) {
        Vec2F screencoord;
        VideoMode videoMode = CameraDevice.getInstance().getVideoMode(CameraDevice.MODE.MODE_DEFAULT);
        VideoBackgroundConfig config = Renderer.getInstance().getVideoBackgroundConfig();

        int xOffset = ((int) config.getPosition().getData()[0]);
        int yOffset = ((int) config.getPosition().getData()[1]);


        if (vuforiaAppSession.getConfig().orientation == Configuration.ORIENTATION_PORTRAIT) {


            // camera image is rotated 90 degrees
            int rotatedX = videoMode.getHeight() - (int) cameraPoint.getData()[1];
            int rotatedY = (int) cameraPoint.getData()[0];

            float[] temp = {rotatedX * config.getSize().getData()[0] / (float) videoMode.getHeight() + xOffset,
                    rotatedY * config.getSize().getData()[1] / (float) videoMode.getWidth() + yOffset};
            screencoord = new Vec2F(temp);
            return screencoord;
        } else {
            float[] temp = {cameraPoint.getData()[0] * config.getSize().getData()[0] / (float) videoMode.getWidth() + xOffset,
                    cameraPoint.getData()[1] * config.getSize().getData()[1] / (float) videoMode.getHeight() + yOffset};
            screencoord = new Vec2F(temp);
            return screencoord;
        }
    }

    private void printUserData(Trackable trackable) {
        String userData = (String) trackable.getUserData();
        Log.d(TAG, "UserData:Retreived User Data	\"" + userData + "\"");
    }

}
