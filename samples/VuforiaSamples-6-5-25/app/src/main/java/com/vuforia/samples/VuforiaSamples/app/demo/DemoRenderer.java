/*===============================================================================
Copyright (c) 2016 PTC Inc. All Rights Reserved.

Copyright (c) 2012-2014 Qualcomm Connected Experiences, Inc. All Rights Reserved.

Vuforia is a trademark of PTC Inc., registered in the United States and other 
countries.
===============================================================================*/

package com.vuforia.samples.VuforiaSamples.app.demo;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.DisplayMetrics;
import android.util.Log;

import com.vuforia.Device;
import com.vuforia.ImageTarget;
import com.vuforia.Matrix44F;
import com.vuforia.State;
import com.vuforia.Tool;
import com.vuforia.Trackable;
import com.vuforia.TrackableResult;
import com.vuforia.Vec2F;
import com.vuforia.Vec3F;
import com.vuforia.Vuforia;
import com.vuforia.samples.SampleApplication.SampleAppRenderer;
import com.vuforia.samples.SampleApplication.SampleAppRendererControl;
import com.vuforia.samples.SampleApplication.SampleApplicationSession;
import com.vuforia.samples.SampleApplication.utils.CubeShaders;
import com.vuforia.samples.SampleApplication.utils.LoadingDialogHandler;
import com.vuforia.samples.SampleApplication.utils.Patrick;
import com.vuforia.samples.SampleApplication.utils.SampleApplication3DModel;
import com.vuforia.samples.SampleApplication.utils.SampleMath;
import com.vuforia.samples.SampleApplication.utils.SampleUtils;
import com.vuforia.samples.SampleApplication.utils.Texture;

import java.io.IOException;
import java.util.Vector;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;


// The renderer class for the DemoActivity sample.
public class DemoRenderer implements GLSurfaceView.Renderer, SampleAppRendererControl {
    private static final String LOGTAG = "DemoRenderer";

    private SampleApplicationSession vuforiaAppSession;
    private DemoActivity mActivity;
    private SampleAppRenderer mSampleAppRenderer;

    private Vector<Texture> mTextures;

    private int shaderProgramID;
    private int vertexHandle;
    private int normalHandle;
    private int textureCoordHandle;
    private int mvpMatrixHandle;
    private int texSampler2DHandle;

    //    private Teapot mTeapot;
    private Patrick mPatrick;
//    private Banana mBanana;

    //    private float kBuildingScale = 0.012f;
    private float kBuildingScale = 12f;
    private SampleApplication3DModel mBuildingsModel;

    private boolean mIsActive = false;
    private boolean mModelIsLoaded = false;

    //    private static final float OBJECT_SCALE_FLOAT = 0.003f;
//    private static final float OBJECT_SCALE_FLOAT = 0.005f;
    private static final float OBJECT_SCALE_FLOAT = 0.08f;


    private Matrix44F tappingProjectionMatrix = null;
    private Matrix44F modelViewMatrix = null;
    private Vec3F targetPositiveDimensions = null;

    // These hold the aspect ratio of both the the keyframe
    float keyframeQuadAspectRatio = 1.0f;

    public DemoRenderer(DemoActivity activity, SampleApplicationSession session) {
        mActivity = activity;
        vuforiaAppSession = session;
        // SampleAppRenderer used to encapsulate the use of RenderingPrimitives setting
        // the device mode AR/VR and stereo mode
        mSampleAppRenderer = new SampleAppRenderer(this, mActivity, Device.MODE.MODE_AR, false, 0.01f, 5f);

        targetPositiveDimensions = new Vec3F();
        modelViewMatrix = new Matrix44F();

    }

    public SampleAppRenderer getSampleAppRenderer() {
        return mSampleAppRenderer;
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
        Log.d(LOGTAG, "GLRenderer.onSurfaceCreated");

        // Call Vuforia function to (re)initialize rendering after first use
        // or after OpenGL ES context was lost (e.g. after onPause/onResume):
        vuforiaAppSession.onSurfaceCreated();

        mSampleAppRenderer.onSurfaceCreated();
    }


    // Called when the surface changed size.
    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        Log.d(LOGTAG, "GLRenderer.onSurfaceChanged");

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

        for (Texture t : mTextures) {
            GLES20.glGenTextures(1, t.mTextureID, 0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, t.mTextureID[0]);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                    GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                    GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA,
                    t.mWidth, t.mHeight, 0, GLES20.GL_RGBA,
                    GLES20.GL_UNSIGNED_BYTE, t.mData);
        }

        shaderProgramID = SampleUtils.createProgramFromShaderSrc(
                CubeShaders.CUBE_MESH_VERTEX_SHADER,
                CubeShaders.CUBE_MESH_FRAGMENT_SHADER);

        vertexHandle = GLES20.glGetAttribLocation(shaderProgramID,
                "vertexPosition");
        normalHandle = GLES20.glGetAttribLocation(shaderProgramID,
                "vertexNormal");
        textureCoordHandle = GLES20.glGetAttribLocation(shaderProgramID,
                "vertexTexCoord");
        mvpMatrixHandle = GLES20.glGetUniformLocation(shaderProgramID,
                "modelViewProjectionMatrix");
        texSampler2DHandle = GLES20.glGetUniformLocation(shaderProgramID,
                "texSampler2D");

        keyframeQuadAspectRatio = (float) mTextures
                .get(0).mHeight / (float) mTextures.get(0).mWidth;

        if (!mModelIsLoaded) {
//            mTeapot = new Teapot();
            mPatrick = new Patrick(mActivity.getResources().getAssets());
//            mBanana = new Banana(mActivity.getResources().getAssets());

            try {
                mBuildingsModel = new SampleApplication3DModel();
                mBuildingsModel.loadModel(mActivity.getResources().getAssets(),
                        "ImageTargets/Buildings.txt");
                mModelIsLoaded = true;
            } catch (IOException e) {
                Log.e(LOGTAG, "Unable to load buildings");
            }

            // Hide the Loading Dialog
            mActivity.loadingDialogHandler
                    .sendEmptyMessage(LoadingDialogHandler.HIDE_LOADING_DIALOG);
        }

    }

    public void updateConfiguration() {
        mSampleAppRenderer.onConfigurationChanged(mIsActive);
    }

    // The render function called from SampleAppRendering by using RenderingPrimitives views.
    // The state is owned by SampleAppRenderer which is controlling it's lifecycle.
    // State should not be cached outside this method.
    public void renderFrame(State state, float[] projectionMatrix) {
        // Renders video background replacing Renderer.DrawVideoBackground()
        mSampleAppRenderer.renderVideoBackground();

        GLES20.glEnable(GLES20.GL_DEPTH_TEST);

        // handle face culling, we need to detect if we are using reflection
        // to determine the direction of the culling
        GLES20.glEnable(GLES20.GL_CULL_FACE);
        GLES20.glCullFace(GLES20.GL_BACK);

        if (tappingProjectionMatrix == null) {
            tappingProjectionMatrix = new Matrix44F();
            tappingProjectionMatrix.setData(projectionMatrix);
        }

        float temp[] = {0.0f, 0.0f, 0.0f};
        targetPositiveDimensions.setData(temp);

        // Did we find any trackables this frame?
        for (int tIdx = 0; tIdx < state.getNumTrackableResults(); tIdx++) {
            TrackableResult result = state.getTrackableResult(tIdx);
            Trackable trackable = result.getTrackable();
            printUserData(trackable);
            Matrix44F modelViewMatrix_Vuforia = Tool
                    .convertPose2GLMatrix(result.getPose());
            modelViewMatrix = Tool
                    .convertPose2GLMatrix(result.getPose());
//            modelViewMatrix = modelViewMatrix_Vuforia;

            float[] modelViewMatrix = modelViewMatrix_Vuforia.getData();

//            int textureIndex = trackable.getName().equalsIgnoreCase("stones") ? 0
//                    : 1;
            int textureIndex = trackable.getName().equalsIgnoreCase("Dmall") ? 0
                    : 1;
            textureIndex = trackable.getName().equalsIgnoreCase("tarmac") ? 2
                    : textureIndex;

            ImageTarget imageTarget = (ImageTarget) result
                    .getTrackable();
            targetPositiveDimensions = imageTarget.getSize();
            // The pose delivers the center of the target, thus the dimensions
            // go from -width/2 to width/2, same for height
            temp[0] = targetPositiveDimensions.getData()[0] / 2.0f;
            temp[1] = targetPositiveDimensions.getData()[1] / 2.0f;
            targetPositiveDimensions.setData(temp);


            // deal with the modelview and projection matrices
            float[] modelViewProjection = new float[16];

            // Here we use the aspect ratio of the keyframe since it
            // is likely that it is not a perfect square

            float ratio = 1.0f;
            if (mTextures.get(textureIndex).mSuccess) {
                ratio = keyframeQuadAspectRatio;
            } else {
                ratio = targetPositiveDimensions.getData()[1]
                        / targetPositiveDimensions.getData()[0];
            }
//            add rotate if necessary
//            Matrix.setRotateM(modelViewMatrix, 0, mAngle, 0, 0, -1f);
            Matrix.rotateM(modelViewMatrix, 0, mX, 0, 1, 0);
            Matrix.rotateM(modelViewMatrix, 0, mY, 1, 0, 0);

            Matrix.scaleM(modelViewMatrix, 0, targetPositiveDimensions.getData()[0],
                    targetPositiveDimensions.getData()[0] * ratio, targetPositiveDimensions.getData()[0]);
//            Matrix.multiplyMM(modelViewMatrix, 0,
//                    projectionMatrix, 0, modelViewMatrix, 0);

//            if (!mActivity.isExtendedTrackingActive())
            {
                Matrix.translateM(modelViewMatrix, 0, 0.0f, 0.0f,
                        OBJECT_SCALE_FLOAT);
//                Matrix.scaleM(modelViewMatrix, 0, OBJECT_SCALE_FLOAT,
//                        OBJECT_SCALE_FLOAT, OBJECT_SCALE_FLOAT);
            }
//            else {
//                Matrix.rotateM(modelViewMatrix, 0, 90.0f, 1.0f, 0, 0);
//                Matrix.scaleM(modelViewMatrix, 0, kBuildingScale,
//                        kBuildingScale, kBuildingScale);
//            }

            Matrix.multiplyMM(modelViewProjection, 0, projectionMatrix, 0, modelViewMatrix, 0);

            // activate the shader program and bind the vertex/normal/tex coords
            GLES20.glUseProgram(shaderProgramID);

//            if (!mActivity.isExtendedTrackingActive())
            {
//                GLES20.glVertexAttribPointer(vertexHandle, 3, GLES20.GL_FLOAT,
//                        false, 0, mTeapot.getVertices());
//                GLES20.glVertexAttribPointer(textureCoordHandle, 2,
//                        GLES20.GL_FLOAT, false, 0, mTeapot.getTexCoords());
                GLES20.glVertexAttribPointer(vertexHandle, 3, GLES20.GL_FLOAT,
                        false, 0, mPatrick.getVertices());
                GLES20.glVertexAttribPointer(normalHandle, 3,
                        GLES20.GL_FLOAT, false, 0, mPatrick.getNormals());
                GLES20.glVertexAttribPointer(textureCoordHandle, 2,
                        GLES20.GL_FLOAT, false, 0, mPatrick.getTexCoords());

                GLES20.glEnableVertexAttribArray(vertexHandle);
                GLES20.glEnableVertexAttribArray(normalHandle);
                GLES20.glEnableVertexAttribArray(textureCoordHandle);

                // activate texture 0, bind it, and pass to shader
                GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,
                        mTextures.get(textureIndex).mTextureID[0]);
                GLES20.glUniform1i(texSampler2DHandle, 0);

                // pass the model view matrix to the shader
                GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false,
                        modelViewProjection, 0);

                // finally draw the teapot
//                GLES20.glDrawElements(GLES20.GL_TRIANGLES,
//                        mTeapot.getNumObjectIndex(), GLES20.GL_UNSIGNED_SHORT,
//                        mTeapot.getIndices());
//                GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0,
//                        mTeapot.getNumObjectVertex());
                GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0,
                        mPatrick.getNumObjectVertex());
                // disable the enabled arrays
                GLES20.glDisableVertexAttribArray(vertexHandle);
                GLES20.glDisableVertexAttribArray(normalHandle);
                GLES20.glDisableVertexAttribArray(textureCoordHandle);

            }
//            else {
//                GLES20.glDisable(GLES20.GL_CULL_FACE);
//                GLES20.glVertexAttribPointer(vertexHandle, 3, GLES20.GL_FLOAT,
//                        false, 0, mBuildingsModel.getVertices());
//                GLES20.glVertexAttribPointer(textureCoordHandle, 2,
//                        GLES20.GL_FLOAT, false, 0, mBuildingsModel.getTexCoords());
////                GLES20.glVertexAttribPointer(vertexHandle, 3, GLES20.GL_FLOAT,
////                        false, 0, mPatrick.getVertices());
////                GLES20.glVertexAttribPointer(normalHandle, 3,
////                        GLES20.GL_FLOAT, false, 0, mPatrick.getNormals());
////                GLES20.glVertexAttribPointer(textureCoordHandle, 2,
////                        GLES20.GL_FLOAT, false, 0, mPatrick.getTexCoords());
//
//                GLES20.glEnableVertexAttribArray(vertexHandle);
//                GLES20.glEnableVertexAttribArray(normalHandle);
//                GLES20.glEnableVertexAttribArray(textureCoordHandle);
//
//                GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
//                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,
//                        mTextures.get(3).mTextureID[0]);
//                GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false,
//                        modelViewProjection, 0);
//                GLES20.glUniform1i(texSampler2DHandle, 0);
//                GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0,
//                        mBuildingsModel.getNumObjectVertex());
////                GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0,
////                        mPatrick.getNumObjectVertex());
//                SampleUtils.checkGLError("Renderer DrawBuildings");
//            }

            SampleUtils.checkGLError("Render Frame");

        }

        GLES20.glDisable(GLES20.GL_DEPTH_TEST);

    }

    private void printUserData(Trackable trackable) {
        String userData = (String) trackable.getUserData();
        Log.d(LOGTAG, "UserData:Retreived User Data	\"" + userData + "\"");
    }


    public void setTextures(Vector<Texture> textures) {
        mTextures = textures;

    }


    boolean isTapOnScreenInsideTarget(float x, float y) {
        // Here we calculate that the touch event is inside the target
        Vec3F intersection;
        // Vec3F lineStart = new Vec3F();
        // Vec3F lineEnd = new Vec3F();

        DisplayMetrics metrics = new DisplayMetrics();
        mActivity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        intersection = SampleMath.getPointToPlaneIntersection(SampleMath
                        .Matrix44FInverse(tappingProjectionMatrix),
                modelViewMatrix, metrics.widthPixels, metrics.heightPixels,
                new Vec2F(x, y), new Vec3F(0, 0, 0), new Vec3F(0, 0, 1));

        // The target returns as pose the center of the trackable. The following
        // if-statement simply checks that the tap is within this range
        if ((intersection.getData()[0] >= -(targetPositiveDimensions.getData()[0]))
                && (intersection.getData()[0] <= (targetPositiveDimensions.getData()[0]))
                && (intersection.getData()[1] >= -(targetPositiveDimensions.getData()[1]))
                && (intersection.getData()[1] <= (targetPositiveDimensions.getData()[1])))
            return true;
        else
            return false;
    }


    private float mAngle;

    public float getAngle() {
        return mAngle;
    }

    public void setAngle(float angle) {
        mAngle = angle;
    }

    private float mX;
    private float mY;
    private float mZ;

    public float getX() {
        return mX;
    }

    public void setX(float mX) {
        this.mX = mX;
    }

    public float getY() {
        return mY;
    }

    public void setY(float mY) {
        this.mY = mY;
    }

    public float getZ() {
        return mZ;
    }

    public void setZ(float mZ) {
        this.mZ = mZ;
    }
}
