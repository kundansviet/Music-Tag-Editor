package com.aw.musictagger;

import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class AlbumSongList extends AppCompatActivity implements RecyclerItemClickListener.OnItemClickListener {
    private RecyclerView rv_album_song_list;
    private List<SongData> song_data;
    private final Uri albumArtUri = Uri.parse("content://media/external/audio/albumart");
    private String album_id;
    private int current_pos=0;
    private Toolbar t_bar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_song_list);

        album_id=getIntent().getStringExtra("album_id");

        song_data = new ArrayList<>();
        song_data.addAll(fetchAllSong());


        t_bar = (Toolbar) findViewById(R.id.t_bar);
        setSupportActionBar(t_bar);
        t_bar.setTitle("Album");
        rv_album_song_list= (RecyclerView) findViewById(R.id.rv_album_song_list);
        rv_album_song_list.setLayoutManager(new GridLayoutManager(this, 2));
        rv_album_song_list.setAdapter(new SongListAdapter(this, song_data));
        rv_album_song_list.addOnItemTouchListener(new RecyclerItemClickListener(this, this));
        rv_album_song_list.scrollToPosition(current_pos);


        if (song_data.size()==0){
            Intent intent = new Intent(AlbumSongList.this, MainActivity.class);
            intent.putExtra("scroll_pos", 0);
            startActivity(intent);
            overridePendingTransition(R.anim.enter_from_left, R.anim.exit_to_right);
            finish();
        }
    }

    /**
     * method to fetch all song
     */
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

                    if (String.valueOf(Uri.parse(audioCursor.getString(6))).equals(album_id)){
                        song_list.add(songData);
                    }

                }

                audioCursor.close();
            }


        }

       /* Cursor mAudioCursor = getActivity().getContentResolver().query(MediaStore.Audio.Media.INTERNAL_CONTENT_URI, projection, selection, null, null);

        if (mAudioCursor != null) {
            while (mAudioCursor.moveToNext()) {
                SongData songData = new SongData();
                songData.song_name = mAudioCursor.getString(4);
                songData.song_artwork = String.valueOf(ContentUris.withAppendedId(malbumArtUri, Integer.parseInt(mAudioCursor.getString(6))));
                songData.song_path = String.valueOf(Uri.parse(mAudioCursor.getString(3)));
                song_list.add(songData);
            }
        }
*/
        return song_list;
    }

    @Override
    public void onItemClick(View childView, int position) {
        Intent editIntent = new Intent(this, TagEditor.class);
        editIntent.putExtra("path", song_data.get(position).song_path);
        editIntent.putExtra("from", 5);
        editIntent.putExtra("album_id", album_id);
        editIntent.putExtra("artwork", song_data.get(position).song_artwork);
        editIntent.putExtra("scroll_pos",position);
        startActivity(editIntent);
        overridePendingTransition(R.anim.enter_from_right,R.anim.rest);
        finish();
    }

    @Override
    public void onItemLongPress(View childView, int position) {

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
