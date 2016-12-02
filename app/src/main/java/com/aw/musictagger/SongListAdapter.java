package com.aw.musictagger;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.media.MediaMetadataRetriever;
import android.support.v7.widget.RecyclerView;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

/**
 * Created by kundan on 10/28/2016.
 */

public class SongListAdapter extends RecyclerView.Adapter<SongListAdapter.SongHolder> {

    private Context context;
    private List<SongData> song_data;
    private LayoutInflater inflater;
    private ImageLoader imageLoader;
    private DisplayImageOptions options;
    int height;


    public SongListAdapter(Context context1, List<SongData> song_data) {
        this.context = context1;
        this.song_data = song_data;
        inflater = LayoutInflater.from(context);
        imageLoader = ImageLoader.getInstance();
        options = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.pictures)
                .showImageForEmptyUri(R.drawable.pictures)
                .showImageOnFail(R.drawable.pictures)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .considerExifParams(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .build();

        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        height = size.x;
    }

    @Override
    public SongHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View convertview = inflater.inflate(R.layout.song_single_item, parent, false);
        SongHolder songHolder = new SongHolder(convertview);
        return songHolder;
    }

    @Override
    public void onBindViewHolder(SongHolder holder, int position) {
        holder.rl_card.getLayoutParams().height= (int) ((height/2)*1.2);
        holder.tv_song_name.setText(song_data.get(position).song_name);
        imageLoader.displayImage(song_data.get(position).song_artwork,holder.iv_song_logo,options);

        //holder.iv_song_logo.setImageBitmap(getImageFromPath(song_data.get(position).song_path));
    }

    @Override
    public int getItemCount() {
        return song_data.size();
    }

    class SongHolder extends RecyclerView.ViewHolder {
        ImageView iv_song_logo;
        TextView tv_song_name;
        RelativeLayout rl_card;

        public SongHolder(View itemView) {
            super(itemView);

            iv_song_logo = (ImageView) itemView.findViewById(R.id.iv_song_logo);
            tv_song_name = (TextView) itemView.findViewById(R.id.tv_song_name);
            rl_card= (RelativeLayout) itemView.findViewById(R.id.rl_card);

        }
    }


}
