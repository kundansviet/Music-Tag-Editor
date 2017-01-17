package com.aw.musictagger;


import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
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
public class AlbumList extends Fragment implements RecyclerItemClickListener.OnItemClickListener {
    private List<AlbumData> albumDatas;
    private final Uri albumArtUri = Uri.parse("content://media/external/audio/albumart");
    private RecyclerView rv_album_list;
    private TextView tv_empty_album_list;
    private int current_pos=0;
    public AlbumList() {
        // Required empty public constructor
    }

    static AlbumList getNewInstance(int c_pos){


        AlbumList f = new AlbumList();
        Bundle args = new Bundle();
        args.putInt("c_pos", c_pos);
        f.setArguments(args);

        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View convertview = inflater.inflate(R.layout.fragment_album_list, container, false);
        albumDatas=new ArrayList<>();

        new FetchAlbum().execute();

        tv_empty_album_list= (TextView)convertview.findViewById(R.id.tv_empty_album_list);




        rv_album_list = (RecyclerView) convertview.findViewById(R.id.rv_album_list);

        rv_album_list.addOnItemTouchListener(new RecyclerItemClickListener(getActivity(), this));


        return convertview;
    }


    /**
     * method to fetch all song
     */
    private List<AlbumData> fetchAllAlbum() {
        String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";
        String[] projection = {
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.ALBUM,


        };
        List<AlbumData> album_list = new ArrayList<>();
        List<String> album_id_list = new ArrayList<>();
        Boolean isSDPresent = android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);

        if (isSDPresent) {
            Cursor audioCursor = getActivity().getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, selection, null, null);
            if (audioCursor != null) {
                while (audioCursor.moveToNext()) {
                    AlbumData albumData = new AlbumData();
                    albumData.album_name = audioCursor.getString(7);
                    albumData.album_artwork = String.valueOf(ContentUris.withAppendedId(albumArtUri, Integer.parseInt(audioCursor.getString(6))));
                    albumData.album_id = String.valueOf(Uri.parse(audioCursor.getString(6)));
                    if (!album_id_list.contains(String.valueOf(Uri.parse(audioCursor.getString(6))))){
                        album_id_list.add(String.valueOf(Uri.parse(audioCursor.getString(6))));
                        album_list.add(albumData);
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
        return album_list;
    }

    @Override
    public void onItemClick(View childView, int position) {
        Intent intent=new Intent(getActivity(),AlbumSongList.class);
        intent.putExtra("album_id",albumDatas.get(position).album_id);
        startActivity(intent);
        getActivity().overridePendingTransition(R.anim.enter_from_right,R.anim.rest);
    }

    @Override
    public void onItemLongPress(View childView, int position) {

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        current_pos= getArguments().getInt("c_pos", 0);
    }

    class FetchAlbum extends AsyncTask{

        @Override
        protected Object doInBackground(Object[] params) {
            albumDatas.addAll(fetchAllAlbum());
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);

            if (albumDatas.size() == 0) {

                tv_empty_album_list.setVisibility(View.VISIBLE);
            }else {
                tv_empty_album_list.setVisibility(View.GONE);
            }

            rv_album_list.setLayoutManager(new GridLayoutManager(getActivity(), 2));
            rv_album_list.setAdapter(new AlbumListAdapter(getActivity(),albumDatas));
            rv_album_list.scrollToPosition(current_pos);
        }
    }

}
