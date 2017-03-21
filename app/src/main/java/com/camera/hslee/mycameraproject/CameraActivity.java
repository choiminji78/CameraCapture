package com.camera.hslee.mycameraproject;


import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.hardware.Camera;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class CameraActivity extends AppCompatActivity {
    private Camera mCamera;
    private CameraPreview mPreview;
    private Button picBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_camera);

        picBtn = (Button)findViewById(R.id.captureBtn);
        picBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCamera.takePicture(null, null, mPicture);
            }
        });
        findViewById(R.id.albBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(),AlbumActivity.class);
                startActivity(i);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        startCamera();
    }
    // 오토포커스 프레임레이아웃에 클릭 달고 클릭 하면 촬영버튼 비활성화
    // 포커스 주변에 사각형 그리기?
    // 포커스 성공하면 촬영버튼 활성화 실패하면 그대로
    private void startCamera(){
        mCamera = getCameraInstance();
        mPreview = new CameraPreview(getApplicationContext(), mCamera,this);
        FrameLayout preview = (FrameLayout) findViewById(R.id.cameraFrame);
        preview.addView(mPreview);
        findViewById(R.id.albBtn).setVisibility(View.VISIBLE);
        findViewById(R.id.captureBtn).setVisibility(View.VISIBLE);
        preview.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    picBtn.setEnabled(false);
                    float x = event.getX();
                    float y = event.getY();
                    float touchMajor = event.getTouchMajor();
                    float touchMinor = event.getTouchMinor();
                    Rect touchRect = new Rect((int)(x - touchMajor / 2), (int)(y - touchMinor / 2), (int)(x + touchMajor / 2), (int)(y + touchMinor / 2));
                    this.submitFocusAreaRect(touchRect);
                }else{

                }
                    return true;
            }

            private void submitFocusAreaRect(Rect touchRect) {
                Camera.Parameters cameraParameters = mCamera.getParameters();
                if (cameraParameters.getMaxNumFocusAreas() == 0) {
                    return;
                }

                // Convert from View's width and height to +/- 1000

                Rect focusArea = new Rect();

                focusArea.set(touchRect.left * 2000 / mPreview.getWidth() - 1000,
                        touchRect.top * 2000 / mPreview.getHeight() - 1000,
                        touchRect.right * 2000 / mPreview.getWidth() - 1000,
                        touchRect.bottom * 2000 / mPreview.getHeight() - 1000);

                // Submit focus area to camera

                ArrayList<Camera.Area> focusAreas = new ArrayList<Camera.Area>();
                focusAreas.add(new Camera.Area(focusArea, 1000));

                cameraParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                cameraParameters.setFocusAreas(focusAreas);
                mCamera.setParameters(cameraParameters);

                // Start the autofocus operation

                mCamera.autoFocus(new Camera.AutoFocusCallback() {
                    @Override
                    public void onAutoFocus(boolean success, Camera camera) {
                        if (success) {
                            picBtn.setEnabled(true);
                        } else {
                            // 포커스 실패
                        }
                    }
                });
            }
        });
//        preview.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                picBtn.setEnabled(false);
//                mCamera.autoFocus(new Camera.AutoFocusCallback() {
//                    @Override
//                    public void onAutoFocus(boolean success, Camera camera) {
//                        if(success){
//                            picBtn.setEnabled(true);
//                        }else{
//                            // 포커스 실패
//                        }
//                    }
//                });
//            }
//        });
    }

    private void resetCamera(){
        if(mPreview!=null && mCamera!=null){
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
            ((FrameLayout) findViewById(R.id.cameraFrame)).removeView(mPreview);
            mPreview = null;
        }
        startCamera();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mCamera!=null){
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
            ((FrameLayout) findViewById(R.id.cameraFrame)).removeView(mPreview);
            mPreview = null;
        }
    }

    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            new SaveImageTask().execute(data);
            resetCamera();
        }
    };
    private class SaveImageTask extends AsyncTask<byte[],Void,Void>{
        @Override
        protected Void doInBackground(byte[]... data) {
            FileOutputStream outStream = null;
            // Write to SD Card
            try {
//                File dir = new File( Environment.getExternalStorageDirectory().getAbsolutePath() , "MyCameraApp");
//                File dir = new File( getApplicationContext().getFilesDir() , "MyCameraApp");
//                File dir = new File(getApplicationContext().getExternalCacheDir(),"MyCamerApp");
//                File dir = new File(getApplicationContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES),"MyCamerApp");
                File dir = new File("sdcard/Android/data/"+getPackageName(),"MyCameraApp");
                dir.mkdirs();

                String fileName = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                fileName= fileName+".jpg";
                File outFile = new File(dir, fileName);
                outStream = new FileOutputStream(outFile);
                outStream.write(data[0]);
                outStream.flush();
                outStream.close();

                Uri imgUri = Uri.fromFile(outFile);
                String imagePath = imgUri.getPath();
                Bitmap img = BitmapFactory.decodeFile(imagePath);

                ExifInterface exif = new ExifInterface(imagePath);
                int exifOri = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                int exifDegree = exifOrientationToDegrees(exifOri);
                img = rotate(img,exifDegree);

                File newImg = new File(dir,fileName);
                OutputStream ops = new FileOutputStream(newImg);
                img.compress(Bitmap.CompressFormat.JPEG,100,ops);
                ops.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
            }
            return null;
        }
    }
    public Bitmap rotate(Bitmap bitmap, int degrees) {
        if(degrees != 0 && bitmap != null) {
            Matrix m = new Matrix();
            m.setRotate(degrees, (float) bitmap.getWidth() / 2,
                    (float) bitmap.getHeight() / 2);
            try {
                Bitmap converted = Bitmap.createBitmap(bitmap, 0, 0,
                        bitmap.getWidth(), bitmap.getHeight(), m, true);
                if(bitmap != converted) {
                    bitmap.recycle();
                    bitmap = converted;
                }
            }
            catch(OutOfMemoryError ex) {
                // 메모리가 부족하여 회전을 시키지 못할 경우 그냥 원본을 반환합니다.
            }
        }
        return bitmap;
    }
    public int exifOrientationToDegrees(int exifOrientation) {
        if(exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) {
            return 90;
        } else if(exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {
            return 180;
        } else if(exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {
            return 270;
        }
        return 0;
    }
    /**
     * A safe way to get an instance of the Camera object.
     */
    public static Camera getCameraInstance() {
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        } catch (Exception e) {
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }
}