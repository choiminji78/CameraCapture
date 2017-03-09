package com.camera.hslee.mycameraproject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Environment;
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
import android.widget.ListAdapter;
import android.widget.Toast;

import java.io.File;
import java.util.List;
import java.util.Objects;

public class AlbumActivity extends AppCompatActivity {
    Activity act = this;
    GridView gridView;
    gridAdapter gA;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album);
        gridView = (GridView) findViewById(R.id.albGridView);
        gA = new gridAdapter();
        gridView.setAdapter(gA);
    }

    private class gridAdapter extends BaseAdapter {
        LayoutInflater inflater;
        File dir;
        File[] childFiles;

        public gridAdapter() {
//            dir = new File(getApplicationContext().getFilesDir(), "MyCameraApp");
//            dir = new File(getApplicationContext().getExternalCacheDir(),"MyCamerApp");
//            File dir = new File(getApplicationContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES),"MyCamerApp");
            dir = new File("sdcard/Android/data/"+getPackageName(),"MyCameraApp");
            childFiles = dir.listFiles();
            inflater = (LayoutInflater) act.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            if(childFiles!=null) {
                return childFiles.length;
            }else{
                return 0;
            }
        }

        @Override
        public Object getItem(int position) {
            if(childFiles!=null) {
                return childFiles[position];
            }else{
                return null;
            }
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.item, parent, false);
            }
            final File file = (File) getItem(position);
            if(file!=null) {
                final Drawable picture = Drawable.createFromPath(file.getAbsolutePath());
                ImageView img = (ImageView) convertView.findViewById(R.id.itemImg);
                img.setImageDrawable(picture);
                registerForContextMenu(img);
                img.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
//                        Toast.makeText(AlbumActivity.this, file.getName(), Toast.LENGTH_SHORT).show();
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
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0,1,100,"삭제");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        super.onContextItemSelected(item);
        AdapterView.AdapterContextMenuInfo menuinfo;
        menuinfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int index = menuinfo.position;
        File f = (File)gA.getItem(index);
        return f.delete();
    }
}

