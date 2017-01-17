package com.aw.musictagger;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
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
import java.util.List;

/**
 * Created by kundan on 12/6/2016.
 */

public class AlbumListAdapter extends RecyclerView.Adapter<AlbumListAdapter.AlbumListHolder> {

    private Context context;
    private List<AlbumData> albumDataList;
    private LayoutInflater inflater;
    private ImageLoader imageLoader;
    private DisplayImageOptions options;
    int height;

    public AlbumListAdapter(Context context1, List<AlbumData> albumDataList1){

        this.context = context1;
        this.albumDataList = albumDataList1;
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
    public AlbumListHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View convertview = inflater.inflate(R.layout.album_single_row, parent, false);
        AlbumListHolder songHolder = new AlbumListHolder(convertview);
        return songHolder;
    }

    @Override
    public void onBindViewHolder(AlbumListHolder holder, int position) {
        holder.rl_card.getLayoutParams().height= (int) ((height/2)*1.2);
        holder.tv_album_name.setText(albumDataList.get(position).album_name);
        imageLoader.displayImage(albumDataList.get(position).album_artwork,holder.iv_album_logo,options);
    }

    @Override
    public int getItemCount() {
        return albumDataList.size();
    }

    public class AlbumListHolder extends RecyclerView.ViewHolder{
        ImageView iv_album_logo;
        TextView tv_album_name;
        RelativeLayout rl_card;
        public AlbumListHolder(View itemView) {
            super(itemView);
            iv_album_logo = (ImageView) itemView.findViewById(R.id.iv_album_logo);
            tv_album_name = (TextView) itemView.findViewById(R.id.tv_album_name);
            rl_card= (RelativeLayout) itemView.findViewById(R.id.rl_card);
        }
    }
}
