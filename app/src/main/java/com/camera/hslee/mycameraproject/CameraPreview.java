package com.camera.hslee.mycameraproject;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;

/**
 * Created by ekchoi on 2017-03-07.
 */

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    private SurfaceHolder mHolder;
    private Camera mCamera;
    private Activity mActivity;
    public CameraPreview(Context context, Camera camera, Activity activity){
        super(context);
        mActivity = activity;
        mCamera=camera;
        mHolder = getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            Camera.Parameters parameters = mCamera.getParameters();
            int rotation = mActivity.getWindowManager().getDefaultDisplay().getRotation();
            int degress = 0;
            switch (rotation){
                case Surface.ROTATION_0 : degress = 0; break;
                case Surface.ROTATION_90: degress = 90; break;
                case Surface.ROTATION_180: degress = 180; break;
                case Surface.ROTATION_270: degress = 270; break;
            }
            int result = (90-degress + 360) % 360;
            parameters.setRotation(result);

            mCamera.setDisplayOrientation(result);
            mCamera.setParameters(parameters);
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
//        if (mCamera != null) {
//            mCamera.stopPreview();
//            mCamera.release();
//            mCamera = null;
//        }
    }
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        // If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.

        if (mHolder.getSurface() == null){
            // preview surface does not exist
            return;
        }

        // stop preview before making changes
        try {
            mCamera.stopPreview();
        } catch (Exception e){
            // ignore: tried to stop a non-existent preview
        }

        // set preview size and make any resize, rotate or
        // reformatting changes here

        // start preview with new settings
        try {
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();

        } catch (Exception e){
            Log.d("Camera", "Error starting camera preview: " + e.getMessage());
        }
    }
}

