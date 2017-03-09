package com.camera.hslee.mycameraproject;


import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
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
        FrameLayout preview = (FrameLayout) findViewById(R.id.activity_camera);
        preview.addView(mPreview);
        preview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                picBtn.setEnabled(false);
                mCamera.autoFocus(new Camera.AutoFocusCallback() {
                    @Override
                    public void onAutoFocus(boolean success, Camera camera) {
                        if(success){
                            picBtn.setEnabled(true);
                        }
                    }
                });
            }
        });
    }

    private void resetCamera(){
        if(mPreview!=null && mCamera!=null){
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
            ((FrameLayout) findViewById(R.id.activity_camera)).removeView(mPreview);
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
            ((FrameLayout) findViewById(R.id.activity_camera)).removeView(mPreview);
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
                String fileName = String.format("%d.jpg", System.currentTimeMillis());
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


//                refreshGallery(outFile);
                Log.d("MyCameraApp", "onPictureTaken - wrote bytes: " + data.length + " to " + outFile.getAbsolutePath());
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
            }
            return null;
        }
    }
    public Bitmap rotate(Bitmap bitmap, int degrees)
    {
        if(degrees != 0 && bitmap != null)
        {
            Matrix m = new Matrix();
            m.setRotate(degrees, (float) bitmap.getWidth() / 2,
                    (float) bitmap.getHeight() / 2);

            try
            {
                Bitmap converted = Bitmap.createBitmap(bitmap, 0, 0,
                        bitmap.getWidth(), bitmap.getHeight(), m, true);
                if(bitmap != converted)
                {
                    bitmap.recycle();
                    bitmap = converted;
                }
            }
            catch(OutOfMemoryError ex)
            {
                // 메모리가 부족하여 회전을 시키지 못할 경우 그냥 원본을 반환합니다.
            }
        }
        return bitmap;
    }
    public int exifOrientationToDegrees(int exifOrientation)
    {
        if(exifOrientation == ExifInterface.ORIENTATION_ROTATE_90)
        {
            return 90;
        }
        else if(exifOrientation == ExifInterface.ORIENTATION_ROTATE_180)
        {
            return 180;
        }
        else if(exifOrientation == ExifInterface.ORIENTATION_ROTATE_270)
        {
            return 270;
        }
        return 0;
    }
    public void refreshGallery(File file){
        Intent mediaScanIntent = new Intent( Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        mediaScanIntent.setData(Uri.fromFile(file));
        sendBroadcast(mediaScanIntent);
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
