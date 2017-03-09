package com.camera.hslee.mycameraproject;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

public class AlbumActivity extends AppCompatActivity {
    Activity act = this;
    GridView gridView;
    GridAdapter mAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album);
        gridView = (GridView) findViewById(R.id.albGridView);
        mAdapter = new GridAdapter();
        gridView.setAdapter(mAdapter);
    }

    private class GridAdapter extends BaseAdapter {
        LayoutInflater inflater;
        File dir;
        ArrayList<File> pictureList;

        public GridAdapter() {
            pictureList = new ArrayList<>();
            listUpdate();
//            dir = new File(getApplicationContext().getFilesDir(), "MyCameraApp");
//            dir = new File(getApplicationContext().getExternalCacheDir(),"MyCamerApp");
//            File dir = new File(getApplicationContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES),"MyCamerApp");
            dir = new File("sdcard/Android/data/"+getPackageName(),"MyCameraApp");

            File[] childFiles = dir.listFiles();
            for(File f:childFiles)
                pictureList.add(f);
            Collections.sort(pictureList);

            inflater = (LayoutInflater) act.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
        public void listUpdate(){
            Collections.sort(pictureList);
            inflater = (LayoutInflater) act.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            if(mAdapter!=null)
                mAdapter.notifyDataSetChanged();
        }
        @Override
        public int getCount() {
            if(pictureList!=null) {
                return pictureList.size();
            }else{
                return 0;
            }
        }

        @Override
        public Object getItem(int position) {
            if(pictureList!=null) {
                return pictureList.get(position);
            }else{
                return null;
            }
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.item, parent, false);
            }
            final File file = (File) getItem(position);
            if(file!=null) {
                final Drawable picture = Drawable.createFromPath(file.getAbsolutePath());
                ImageView img = (ImageView) convertView.findViewById(R.id.itemImg);
                img.setImageDrawable(picture);
                img.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(AlbumActivity.this, R.style.Dialog)
                                .setTitle("사진 삭제").setMessage("해당 사진을 삭제 하시겠습니까?")
                                .setPositiveButton("네", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if(file.delete()){
                                            pictureList.remove(position);
                                        }
                                        listUpdate();
                                        Toast.makeText(act, "삭제되었습니다.", Toast.LENGTH_SHORT).show();
                                    }
                                }).setNegativeButton("아니요", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.cancel();
                                    }
                                });
                        AlertDialog alert = builder.create();
                        alert.show();
                        return true;
                    }
                });
                img.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(getApplicationContext(),PictureInfoActivity.class);
                        i.putExtra("picturePath",file.getAbsolutePath());
                        i.putExtra("pictureName",file.getName());
                        startActivity(i);
                    }
                });
            }
            return convertView;
        }
    }
}

