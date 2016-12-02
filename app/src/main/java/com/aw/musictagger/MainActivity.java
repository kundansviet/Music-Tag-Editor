package com.aw.musictagger;


import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {
    private List<SongData> song_data;
    private final Uri albumArtUri = Uri.parse("content://media/external/audio/albumart");
    private final Uri malbumArtUri = Uri.parse("content://media/internal/audio/albumart");
    private ViewPager pager;
    private FloatingActionButton fb_scan;
    private PagerAdapter pagerAdapter;
    private TabLayout tab;
    private Toolbar main_toolbar;
    private int selected_page = 0,scroll_pos=0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ImageLoaderConfiguration configuration = new ImageLoaderConfiguration.Builder(this).build();
        ImageLoader.getInstance().init(configuration);
        scroll_pos=getIntent().getIntExtra("scroll_pos",0);
        song_data = new ArrayList<>();
        song_data.addAll(fetchAllSong());
        main_toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(main_toolbar);
        fb_scan = (FloatingActionButton) findViewById(R.id.fb_scan);
        pager = (ViewPager) findViewById(R.id.pager);
        tab = (TabLayout) findViewById(R.id.tab);
        pagerAdapter = new PagerAdapter(getSupportFragmentManager(),scroll_pos);
        pager.setAdapter(pagerAdapter);
        tab.setupWithViewPager(pager);
        fb_scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (song_data.size() == 0) {
                    Toast.makeText(MainActivity.this,"No song found",Toast.LENGTH_SHORT).show();

                }else {
                    Intent editIntent = new Intent(MainActivity.this, TagEditor.class);
                    editIntent.putExtra("path", song_data.get(0).song_path);
                    editIntent.putExtra("from",0);
                    editIntent.putExtra("artwork", song_data.get(0).song_artwork);
                    startActivity(editIntent);
                    overridePendingTransition(R.anim.enter_from_right,R.anim.rest);
                    finish();
                }

            }
        });

        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                selected_page=position;
                if (position == 1) {
                    fb_scan.setVisibility(View.GONE);
                } else {
                    fb_scan.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private List<SongData> fetchAllSong() {
        String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";
        String[] projection = {
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.ALBUM_ID,


        };
        List<SongData> song_list = new ArrayList<>();

        Boolean isSDPresent = android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);

        if (isSDPresent) {
            Cursor audioCursor = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, selection, null, null);
            if (audioCursor != null) {
                while (audioCursor.moveToNext()) {
                    SongData songData = new SongData();
                    songData.song_name = audioCursor.getString(2);
                    songData.song_artwork = String.valueOf(ContentUris.withAppendedId(albumArtUri, Integer.parseInt(audioCursor.getString(6))));
                    songData.song_path = String.valueOf(Uri.parse(audioCursor.getString(3)));
                    song_list.add(songData);
                }

                audioCursor.close();
            }


        }

        /*Cursor mAudioCursor = getContentResolver().query(MediaStore.Audio.Media.INTERNAL_CONTENT_URI, projection, selection, null, null);

        if (mAudioCursor != null) {
            while (mAudioCursor.moveToNext()) {
                SongData songData = new SongData();
                songData.song_name = mAudioCursor.getString(4);
                songData.song_artwork = String.valueOf(ContentUris.withAppendedId(malbumArtUri, Integer.parseInt(mAudioCursor.getString(6))));
                songData.song_path = String.valueOf(Uri.parse(mAudioCursor.getString(3)));
                song_list.add(songData);
            }
        }*/

        return song_list;
    }




    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }
}
