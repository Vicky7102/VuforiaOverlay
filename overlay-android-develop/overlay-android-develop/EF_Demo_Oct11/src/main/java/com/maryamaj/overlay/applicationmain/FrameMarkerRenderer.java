/*===============================================================================
Copyright (c) 2012-2014 Qualcomm Connected Experiences, Inc. All Rights Reserved.

Vuforia is a trademark of QUALCOMM Incorporated, registered in the United States 
and other countries. Trademarks of QUALCOMM Incorporated are used with permission.
===============================================================================*/

package com.maryamaj.overlay.applicationmain;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.IntBuffer;
import java.util.Arrays;
import java.util.Vector;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.annotation.SuppressLint;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Environment;
import android.util.Log;

import com.maryamaj.overlay.applicationsettings.SampleApplicationSession;
import com.maryamaj.overlay.models.Point2D;
import com.maryamaj.overlay.models.Point3D;
import com.maryamaj.overlay.utils.SampleMath;
import com.maryamaj.overlay.utils.SampleUtils;
import com.maryamaj.overlay.utils.Texture;
import com.maryamaj.overlay.utils.VideoBackgroundShader;
import com.vuforia.COORDINATE_SYSTEM_TYPE;
import com.vuforia.CameraDevice;
import com.vuforia.Device;
import com.vuforia.GLTextureUnit;
import com.vuforia.Matrix44F;
import com.vuforia.Mesh;
import com.vuforia.Renderer;
import com.vuforia.RenderingPrimitives;
import com.vuforia.State;
import com.vuforia.Tool;
import com.vuforia.Trackable;
import com.vuforia.TrackableResult;
import com.vuforia.VIDEO_BACKGROUND_REFLECTION;
import com.vuforia.VIEW;
import com.vuforia.Vec2F;
import com.vuforia.Vec3F;
import com.vuforia.Vec4F;
import com.vuforia.VideoBackgroundConfig;
import com.vuforia.VideoMode;
import com.vuforia.Vuforia;


// The renderer class for the FrameMarkers sample. 
@SuppressLint("Assert")
public class FrameMarkerRenderer implements GLSurfaceView.Renderer{
    private static final String TAG = "FrameMarkerRenderer";

    SampleApplicationSession vuforiaAppSession;
    FrameMarkers mActivity;

    public boolean mIsActive = false;

    private Vector<Texture> mTextures;


    private static boolean takeShot = false;


    //zero coordinate system from frame markers
    private Matrix44F mainModelViewMatrix;
    //	private State state = Renderer.getInstance().begin();

    Point2D refCoords = null;// for framemarkerID=0


    private int mViewHeight = 0;
    private int mViewWidth = 0;
    private RenderingPrimitives mRenderingPrimitives = null;
    private GLTextureUnit videoBackgroundTex = null;
    // Shader user to render the video background on AR mode
    private int vbShaderProgramID = 0;
    private int vbTexSampler2DHandle = 0;
    private int vbVertexHandle = 0;
    private int vbTexCoordHandle = 0;
    private int vbProjectionMatrixHandle = 0;


    public FrameMarkerRenderer(FrameMarkers activity,
                               SampleApplicationSession session) {


        mActivity = activity;
        vuforiaAppSession = session;

        //initiating variables for zero coordinate system from frame markers

        mainModelViewMatrix = new Matrix44F();

    }

    // Called when the surface is created or recreated.
    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        Log.d(TAG, "GLRenderer.onSurfaceCreated");

        // Call function to initialize rendering:
        initRendering();

        // Call Vuforia function to (re)initialize rendering after first use
        // or after OpenGL ES context was lost (e.g. after onPause/onResume):
        vuforiaAppSession.onSurfaceCreated();

    }

    // Called when the surface changed size.
    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        mViewWidth = width;
        mViewHeight = height;

        Log.d(TAG, "GLRenderer.onSurfaceChanged");
        // Call Vuforia function to handle render surface size changes:
        vuforiaAppSession.onSurfaceChanged(width, height);
        mRenderingPrimitives = Device.getInstance().getRenderingPrimitives();
    }

    // Called to draw the current frame.
    @Override
    public void onDrawFrame(GL10 gl) {
        if (!mIsActive)
            return;
        // Call our function to render content
        renderFrame();
    }

    void initRendering() {
        Log.d(TAG, "initRendering");

        // Define clear color
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, Vuforia.requiresAlpha() ? 0.0f
                : 1.0f);

        vbShaderProgramID = SampleUtils.createProgramFromShaderSrc(VideoBackgroundShader.VB_VERTEX_SHADER,
                VideoBackgroundShader.VB_FRAGMENT_SHADER);

        // Rendering configuration for video background
        if (vbShaderProgramID > 0)
        {
            // Activate shader:
            GLES20.glUseProgram(vbShaderProgramID);

            // Retrieve handler for texture sampler shader uniform variable:
            vbTexSampler2DHandle = GLES20.glGetUniformLocation(vbShaderProgramID, "texSampler2D");

            // Retrieve handler for projection matrix shader uniform variable:
            vbProjectionMatrixHandle = GLES20.glGetUniformLocation(vbShaderProgramID, "projectionMatrix");

            vbVertexHandle = GLES20.glGetAttribLocation(vbShaderProgramID, "vertexPosition");
            vbTexCoordHandle = GLES20.glGetAttribLocation(vbShaderProgramID, "vertexTexCoord");
            vbProjectionMatrixHandle = GLES20.glGetUniformLocation(vbShaderProgramID, "projectionMatrix");
            vbTexSampler2DHandle = GLES20.glGetUniformLocation(vbShaderProgramID, "texSampler2D");

            // Stop using the program
            GLES20.glUseProgram(0);
        }

        videoBackgroundTex = new GLTextureUnit();
    }

    void renderFrame() {

        // Clear color and depth buffer
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        // Get the state from Vuforia and mark the beginning of a rendering
        // section
        State state = Renderer.getInstance().begin();

        // Explicitly render the Video Background
        Renderer.getInstance().drawVideoBackground();

        GLES20.glEnable(GLES20.GL_DEPTH_TEST);

        // We must detect if background reflection is active and adjust the
        // culling direction.
        // If the reflection is active, this means the post matrix has been
        // reflected as well,
        // therefore standard counter clockwise face culling will result in
        // "inside out" models.
        GLES20.glEnable(GLES20.GL_CULL_FACE);
        GLES20.glCullFace(GLES20.GL_BACK);
        if (Renderer.getInstance().getVideoBackgroundConfig().getReflection() ==
                VIDEO_BACKGROUND_REFLECTION.VIDEO_BACKGROUND_REFLECTION_ON)
            GLES20.glFrontFace(GLES20.GL_CW);  // Front camera
        else
            GLES20.glFrontFace(GLES20.GL_CCW);   // Back camera


        // Did we find any trackables this frame?
        for (int tIdx = 0; tIdx < state.getNumTrackableResults(); tIdx++) {
            // Get the trackable:
            TrackableResult trackableResult = state.getTrackableResult(tIdx);
//            Trackable trackable = trackableResult.getTrackable();
//            printUserData(trackable);
            Matrix44F modelViewMatrix = Tool.convertPose2GLMatrix(trackableResult.getPose());
            float[] modelViewMatrixTrackable = Tool.convertPose2GLMatrix(
                    trackableResult.getPose()).getData();
            //for logging
//            float[][] pose2D = new float[4][4];
//            for(int i = 0; i < 4; i++) {
//                for(int k = 0;k < 4; k++) {
//                    pose2D[i][k] = modelViewMatrixTrackable[i*4 + k];
//                }
//            }
//            Log.d(TAG, "Pose (4/4): " + Arrays.deepToString(pose2D));
//            Log.d(TAG, "Pose (3/4): " + Arrays.toString(trackableResult.getPose().getData()));
            //******************beginning of zero coordinate system
            mainModelViewMatrix = modelViewMatrix;
            Vec2F cameraCoord = Tool.projectPoint(vuforiaAppSession.getCameraCalib(),
                    trackableResult.getPose(), new Vec3F(0.0f, 0.0f, 0.0f));
            int xCoord = (int) cameraPointToScreen(cameraCoord).getData()[0];
            int yCoord = (int) cameraPointToScreen(cameraCoord).getData()[1];
//                Log.d(TAG, "Marker size: " + Arrays.toString(marker.getSize().getData()));
//                Log.d(TAG, "Projected point: " + Arrays.toString(cameraCoord.getData()));
            refCoords = new Point2D(xCoord, yCoord);
            // set the position of the framemarker on the screen for calculating the relative area positions in drawingView
            mActivity.setMarkerCenter(refCoords, modelViewMatrixTrackable[14]);
            SampleUtils.checkGLError("FrameMarkers render frame");
        }
        Renderer.getInstance().end();
    }

    public static void takeScreenshot() {
        takeShot = true;
        saveScreenShot(0, 0, 100, 100, "test1.png");
    }

    public static void saveScreenShot(int x, int y, int w, int h, String filename) {

        Bitmap bmp = grabPixels3(x, y, w, h);

        try {
            String path = Environment.getExternalStorageDirectory() + "/" + filename;

            File file = new File(path);
            file.createNewFile();

            FileOutputStream fos = new FileOutputStream(file);
            bmp.compress(CompressFormat.PNG, 100, fos);

            fos.flush();

            fos.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Bitmap grabPixels3(int x, int y, int w, int h) {
        int b[] = new int[w * (y + h)];
        int bt[] = new int[w * h];
        IntBuffer ib = IntBuffer.wrap(b);
        ib.position(0);
        GLES20.glReadPixels(x, 0, w, y + h, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, ib);

        for (int i = 0, k = 0; i < h; i++, k++) {//remember, that OpenGL bitmap is incompatible with Android bitmap
            //and so, some correction need.
            for (int j = 0; j < w; j++) {
                int pix = b[i * w + j];
                int pb = (pix >> 16) & 0xff;
                int pr = (pix << 16) & 0x00ff0000;
                int pix1 = (pix & 0xff00ff00) | pr | pb;
                bt[(h - k - 1) * w + j] = pix1;
            }
        }

        Bitmap sb = Bitmap.createBitmap(bt, w, h, Bitmap.Config.ARGB_8888);
        return sb;
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

    public static void checkGlError(String glOperation) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e(TAG, glOperation + ": glError " + error);
            throw new RuntimeException(glOperation + ": glError " + error);
        }
    }

    private void printUserData(Trackable trackable) {
        String userData = (String) trackable.getUserData();
        Log.d(TAG, "UserData:Retreived User Data	\"" + userData + "\"");
    }


}
