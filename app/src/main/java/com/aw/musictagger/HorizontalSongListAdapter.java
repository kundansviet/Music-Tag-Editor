package com.aw.musictagger;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

/**
 * Created by kundan on 11/11/2016.
 */

public class HorizontalSongListAdapter extends RecyclerView.Adapter<HorizontalSongListAdapter.SongHolder>{
    private Context context;
    private List<Result> song_data;
    private LayoutInflater inflater;
    private ImageLoader imageLoader;
    private DisplayImageOptions options;
  //  int height;
    public HorizontalSongListAdapter(Context context1, List<Result> song_data){
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

      /*  WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        height = size.x;*/
    }

    @Override
    public SongHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View convertview = inflater.inflate(R.layout.single_horizontal_row, parent, false);
        SongHolder songHolder = new SongHolder(convertview);
        return songHolder;
    }

    @Override
    public void onBindViewHolder(SongHolder holder, int position) {
       // holder.rl_card.getLayoutParams().height= (int) ((height/2.5)*1.2);
        holder.tv_song_name.setText(song_data.get(position).getTrackName());
        imageLoader.displayImage(song_data.get(position).getArtworkUrl100(),holder.iv_song_logo,options);
    }

    @Override
    public int getItemCount() {
        return song_data.size();
    }

    class SongHolder extends RecyclerView.ViewHolder{
        ImageView iv_song_logo;
        TextView tv_song_name;
        RelativeLayout rl_card;
        public SongHolder(View itemView) {
            super(itemView);

            iv_song_logo = (ImageView) itemView.findViewById(R.id.iv_h_song_logo);
            tv_song_name = (TextView) itemView.findViewById(R.id.tv_h_song_name);
            rl_card= (RelativeLayout) itemView.findViewById(R.id.rl_h_card);
        }
    }
}
