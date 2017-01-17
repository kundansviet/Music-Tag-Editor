package com.aw.musictagger;


import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class SongList extends Fragment implements RecyclerItemClickListener.OnItemClickListener {
    private RecyclerView rv_song_list;
    private List<SongData> song_data;
    private final Uri albumArtUri = Uri.parse("content://media/external/audio/albumart");
    private TextView tv_empty_list;
    private int current_pos=0;

    public SongList() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View converview = inflater.inflate(R.layout.fragment_song_list, container, false);
        song_data = new ArrayList<>();
        new FetchSongTask().execute();
        tv_empty_list= (TextView) converview.findViewById(R.id.tv_empty_list);
        rv_song_list = (RecyclerView) converview.findViewById(R.id.rv_song_list);
        rv_song_list.addOnItemTouchListener(new RecyclerItemClickListener(getActivity(), this));

        // Inflate the layout for this fragment
        return converview;
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
            Cursor audioCursor = getActivity().getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, selection, null, null);
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
        Intent editIntent = new Intent(getActivity(), TagEditor.class);
        editIntent.putExtra("path", song_data.get(position).song_path);
        editIntent.putExtra("from", 0);
        editIntent.putExtra("artwork", song_data.get(position).song_artwork);
        editIntent.putExtra("scroll_pos",position);
        startActivity(editIntent);
        getActivity().overridePendingTransition(R.anim.enter_from_right,R.anim.rest);
        getActivity().finish();
    }

    @Override
    public void onItemLongPress(View childView, int position) {

    }


    static SongList getNewInstance(int c_pos){


        SongList f = new SongList();
        Bundle args = new Bundle();
        args.putInt("c_pos", c_pos);
        f.setArguments(args);

        return f;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

       current_pos= getArguments().getInt("c_pos", 0);
    }

    class FetchSongTask extends AsyncTask{

        @Override
        protected Object doInBackground(Object[] params) {

            song_data.addAll(fetchAllSong());
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            if (song_data.size() == 0) {

                tv_empty_list.setVisibility(View.VISIBLE);
            }else {
                tv_empty_list.setVisibility(View.GONE);
            }

            rv_song_list.setLayoutManager(new GridLayoutManager(getActivity(), 2));
            rv_song_list.setAdapter(new SongListAdapter(getActivity(), song_data));
            rv_song_list.scrollToPosition(current_pos);

        }
    }
}
