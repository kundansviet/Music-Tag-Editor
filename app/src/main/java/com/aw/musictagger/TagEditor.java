package com.aw.musictagger;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import org.cmc.music.common.ID3WriteException;
import org.cmc.music.metadata.IMusicMetadata;
import org.cmc.music.metadata.ImageData;
import org.cmc.music.metadata.MusicMetadata;
import org.cmc.music.metadata.MusicMetadataSet;
import org.cmc.music.myid3.MyID3;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class TagEditor extends AppCompatActivity implements RecyclerItemClickListener.OnItemClickListener, ImageLoadingListener {

    String path, artwork;
    private ImageView iv_artwork, iv_artwork_edit;
    private EditText et_title, et_artist, et_album, et_genre, et_year;
    private IMusicMetadata metadata;
    private ImageLoader imageLoader;
    private DisplayImageOptions options;
    private Toolbar toolbar;
    private CollapsingToolbarLayout toolbar_layout;
    private boolean editable;
    private ProgressBar progress;
    private List<Result> isong_data;
    private RecyclerView rv_itune_song;
    private HorizontalSongListAdapter songListAdapter;
    private TextView tv_loading_text;
    private int camefrom, scroll_pos = 0;
    private byte[] imageInByte;
    private FloatingActionButton fab, play_pause;
    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tag_editor);
        isong_data = new ArrayList<>();
        intiImageLoader();
        Intent dataIntent = getIntent();
        scroll_pos = dataIntent.getIntExtra("scroll_pos", 0);
        camefrom = dataIntent.getIntExtra("from", 0);
        artwork = dataIntent.getStringExtra("artwork");
        path = dataIntent.getStringExtra("path");
        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(path);
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }


        toolbar = (Toolbar) findViewById(R.id.toolbar);
        progress = (ProgressBar) findViewById(R.id.progress);
        toolbar_layout = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);
        iv_artwork = (ImageView) findViewById(R.id.iv_artwork);
        et_title = (EditText) findViewById(R.id.et_title);
        et_album = (EditText) findViewById(R.id.et_album);
        et_artist = (EditText) findViewById(R.id.et_artist);
        et_genre = (EditText) findViewById(R.id.et_genre);
        et_year = (EditText) findViewById(R.id.et_year);

        tv_loading_text = (TextView) findViewById(R.id.tv_loading_text);
        rv_itune_song = (RecyclerView) findViewById(R.id.rv_itune_song);
        rv_itune_song.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        songListAdapter = new HorizontalSongListAdapter(this, isong_data);
        rv_itune_song.setAdapter(songListAdapter);
        rv_itune_song.addOnItemTouchListener(new RecyclerItemClickListener(this, this));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        fab = (FloatingActionButton) findViewById(R.id.fab);
        play_pause = (FloatingActionButton) findViewById(R.id.play_pause);
        play_pause.setBackgroundResource(R.drawable.play);

        play_pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaPlayer.isPlaying()) {
                    play_pause.setImageDrawable(ContextCompat.getDrawable(TagEditor.this, R.drawable.play));
                    mediaPlayer.stop();

                } else {
                    play_pause.setImageDrawable(ContextCompat.getDrawable(TagEditor.this, R.drawable.pause));
                    mediaPlayer.reset();
                    try {
                        mediaPlayer.setDataSource(path);
                        mediaPlayer.prepare();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    mediaPlayer.start();
                }
            }
        });
        fab.setBackgroundResource(R.drawable.edittag);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editable = !editable;

                if (editable) {
                    fab.setImageDrawable(ContextCompat.getDrawable(TagEditor.this, R.drawable.done));
                    if (isConnected()) {
                        callItunesApi();
                    } else {
                        turnOnEditText();
                    }

                } else {
                    fab.setImageDrawable(ContextCompat.getDrawable(TagEditor.this, R.drawable.edittag));
                    turnOffEditText();
                    updateMetadata();

                }

            }
        });
        if (camefrom == 0) {
            imageLoader.displayImage(artwork, iv_artwork, options);
        } else {
            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
            File file = new File(path);
            FileInputStream inputStream = null;
            try {
                inputStream = new FileInputStream(file.getAbsolutePath());
                mmr.setDataSource(inputStream.getFD());
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (RuntimeException e) {
                e.printStackTrace();
            }

            byte[] data = mmr.getEmbeddedPicture();
            if (data != null && data.length > 0) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                iv_artwork.setImageBitmap(bitmap);
            }

        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                generateColorFrom();
            }
        }, 300);
        setOldData();

    }

    /**
     * Method to set device data
     */
    void setOldData() {
        MusicMetadataSet src_set = null;
        File cFile = new File(path);

        try {
            src_set = new MyID3().read(cFile);
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } // read metadata

        if (src_set == null) // perhaps no metadata
        {
            Log.i("NULL", "NULL");
        } else {
            try {
                metadata = src_set.merged;
                et_title.setText(metadata.getSongTitle());
                et_artist.setText(metadata.getArtist());
                et_album.setText(metadata.getAlbum());
                et_genre.setText(metadata.getGenre());
                et_year.setText(metadata.getYear());
                toolbar.setTitle(cFile.getName());
                toolbar_layout.setTitle(cFile.getName());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    void intiImageLoader() {
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

        imageLoader.setDefaultLoadingListener(this);
    }

    public boolean isConnected() {
        ConnectivityManager
                cm = (ConnectivityManager) getApplicationContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null
                && activeNetwork.isConnectedOrConnecting();
    }

    @Override
    public void onItemClick(View childView, int position) {
        et_title.setText(isong_data.get(position).getTrackName());
        et_artist.setText(isong_data.get(position).getArtistName());
        et_album.setText(isong_data.get(position).getCollectionName());
        et_genre.setText(isong_data.get(position).getPrimaryGenreName());
        String artwork_url = isong_data.get(position).getArtworkUrl100().replaceAll("100x100", "350x350");
        imageLoader.displayImage(artwork_url, iv_artwork, options);


    }

    @Override
    public void onItemLongPress(View childView, int position) {

    }


    /**
     * method to turn on edit text
     */
    void turnOnEditText() {
        et_title.setEnabled(true);
        et_artist.setEnabled(true);
        et_album.setEnabled(true);
        et_genre.setEnabled(true);
        et_year.setEnabled(true);
    }

    /**
     * method to turn off edit text
     */
    void turnOffEditText() {
        et_title.setEnabled(false);
        et_artist.setEnabled(false);
        et_album.setEnabled(false);
        et_genre.setEnabled(false);
        et_year.setEnabled(false);
    }


    /**
     * method to update metadata of file
     */
    void updateMetadata() {
        MusicMetadataSet src_set = null;
        File cFile = new File(path);

        try {
            src_set = new MyID3().read(cFile);
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } // read metadata
        MusicMetadata meta = new MusicMetadata("name");
        meta.setAlbum(et_album.getText().toString());
        meta.setSongTitle(et_title.getText().toString());
        meta.setArtist(et_artist.getText().toString());
        meta.setGenre(et_genre.getText().toString());
        meta.setYear(et_year.getText().toString());

        Bitmap bitmap = ((BitmapDrawable) iv_artwork.getDrawable()).getBitmap();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] imageInByte = stream.toByteArray();
        meta.addPicture(new ImageData(imageInByte, "image/jpeg", "gracenote image", 1));

        try {
            new MyID3().update(cFile, src_set, meta);
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ID3WriteException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(cFile)));
    }


    void callItunesApi() {

        MusicMetadataSet src_set = null;
        File cFile = new File(path);
        try {
            src_set = new MyID3().read(cFile);
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } // read metadata

        if (src_set == null) // perhaps no metadata
        {
            Log.i("NULL", "NULL");
        } else {
            try {
                metadata = src_set.getSimplified();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (metadata != null) {
            tv_loading_text.setVisibility(View.VISIBLE);
            progress.setVisibility(View.VISIBLE);
            Call<Model> call = null;
            Retrofit retrofit = new RetroFitMaker().instanceMaker("https://itunes.apple.com");
            RestApi restApis = retrofit.create(RestApi.class);


            call = restApis.searchItunes("application/x-www-form-urlencoded", removeUrl(metadata.getSongTitle()));


            call.enqueue(new Callback<Model>() {
                @Override
                public void onResponse(Call<Model> call, Response<Model> response) {
                    tv_loading_text.setVisibility(View.GONE);
                    progress.setVisibility(View.GONE);
                    if (response.body().getResultCount() > 0) {
                        rv_itune_song.setVisibility(View.VISIBLE);
                        isong_data = response.body().getResults();
                        songListAdapter = new HorizontalSongListAdapter(TagEditor.this, isong_data);
                        rv_itune_song.setAdapter(songListAdapter);
                        turnOnEditText();
                    } else {
                        callItunesApiUsingSecondary();
                    }


                }

                @Override
                public void onFailure(Call<Model> call, Throwable t) {
                    progress.setVisibility(View.GONE);
                    tv_loading_text.setText("No match found");
                    Log.d("error", t.getMessage());
                    turnOnEditText();
                }
            });

        } else {
            callItunesApiUsingFilename();
        }

    }

    void callItunesApiUsingSecondary() {

        MusicMetadataSet src_set = null;
        File cFile = new File(path);
        try {
            src_set = new MyID3().read(cFile);
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } // read metadata

        if (src_set == null) // perhaps no metadata
        {
            Log.i("NULL", "NULL");
        } else {
            try {
                metadata = src_set.id3v1Clean;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (metadata != null) {
            tv_loading_text.setVisibility(View.VISIBLE);
            progress.setVisibility(View.VISIBLE);
            Call<Model> call = null;
            Retrofit retrofit = new RetroFitMaker().instanceMaker("https://itunes.apple.com");
            RestApi restApis = retrofit.create(RestApi.class);

            call = restApis.searchItunes("application/x-www-form-urlencoded", removeUrl(metadata.getAlbum()));


            call.enqueue(new Callback<Model>() {
                @Override
                public void onResponse(Call<Model> call, Response<Model> response) {
                    tv_loading_text.setVisibility(View.GONE);
                    progress.setVisibility(View.GONE);
                    if (response.body().getResultCount() > 0) {
                        rv_itune_song.setVisibility(View.VISIBLE);
                        isong_data = response.body().getResults();
                        songListAdapter = new HorizontalSongListAdapter(TagEditor.this, isong_data);
                        rv_itune_song.setAdapter(songListAdapter);
                        turnOnEditText();
                    } else {
                        callItunesApiUsingThird();
                    }
                }

                @Override
                public void onFailure(Call<Model> call, Throwable t) {
                    progress.setVisibility(View.GONE);
                    tv_loading_text.setText("No match found");
                    Log.d("error", t.getMessage());
                    turnOnEditText();
                }
            });

        } else {
            callItunesApiUsingFilename();
        }

    }

    void callItunesApiUsingThird() {

        MusicMetadataSet src_set = null;
        File cFile = new File(path);
        try {
            src_set = new MyID3().read(cFile);
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } // read metadata

        if (src_set == null) // perhaps no metadata
        {
            Log.i("NULL", "NULL");
        } else {
            try {
                metadata = src_set.id3v1Clean;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (metadata != null) {
            tv_loading_text.setVisibility(View.VISIBLE);
            progress.setVisibility(View.VISIBLE);
            Call<Model> call = null;
            Retrofit retrofit = new RetroFitMaker().instanceMaker("https://itunes.apple.com");
            RestApi restApis = retrofit.create(RestApi.class);


            call = restApis.searchItunes("application/x-www-form-urlencoded", removeUrl(metadata.getArtist()));


            call.enqueue(new Callback<Model>() {
                @Override
                public void onResponse(Call<Model> call, Response<Model> response) {
                    tv_loading_text.setVisibility(View.GONE);
                    progress.setVisibility(View.GONE);
                    if (response.body().getResultCount() > 0) {
                        rv_itune_song.setVisibility(View.VISIBLE);
                        isong_data = response.body().getResults();
                        songListAdapter = new HorizontalSongListAdapter(TagEditor.this, isong_data);
                        rv_itune_song.setAdapter(songListAdapter);
                        turnOnEditText();
                    } else {
                        callItunesApiUsingFilename();
                    }


                }

                @Override
                public void onFailure(Call<Model> call, Throwable t) {
                    progress.setVisibility(View.GONE);
                    tv_loading_text.setText("No match found");
                    Log.d("error", t.getMessage());
                    turnOnEditText();
                }
            });

        } else {
            callItunesApiUsingFilename();
        }

    }


    void callItunesApiUsingFilename() {


        File cFile = new File(path);

        if (cFile.getName() != null) {
            tv_loading_text.setVisibility(View.VISIBLE);
            progress.setVisibility(View.VISIBLE);
            Call<Model> call = null;
            Retrofit retrofit = new RetroFitMaker().instanceMaker("https://itunes.apple.com");
            RestApi restApis = retrofit.create(RestApi.class);

            call = restApis.searchItunes("application/x-www-form-urlencoded", removeUrl(getTitleFromFileName(cFile.getName())));

            call.enqueue(new Callback<Model>() {
                @Override
                public void onResponse(Call<Model> call, Response<Model> response) {
                    tv_loading_text.setVisibility(View.GONE);
                    progress.setVisibility(View.GONE);
                    if (response.body().getResultCount() > 0) {
                        rv_itune_song.setVisibility(View.VISIBLE);
                        isong_data = response.body().getResults();
                        songListAdapter = new HorizontalSongListAdapter(TagEditor.this, isong_data);
                        rv_itune_song.setAdapter(songListAdapter);

                    }
                    turnOnEditText();

                }

                @Override
                public void onFailure(Call<Model> call, Throwable t) {
                    progress.setVisibility(View.GONE);
                    tv_loading_text.setText("No match found");
                    Log.d("error", t.getMessage());
                    turnOnEditText();
                }
            });

        } else {
            turnOnEditText();
        }

    }

    void generateColorFrom() {

        Bitmap b = ((BitmapDrawable) iv_artwork.getDrawable()).getBitmap();


        Palette.from(b).generate();
        Palette.from(b).maximumColorCount(5).generate(new Palette.PaletteAsyncListener() {
            @Override
            public void onGenerated(Palette palette) {
                toolbar_layout.setContentScrimColor(palette.getVibrantColor(getTitleColor()));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    getWindow().setStatusBarColor(palette.getVibrantColor(getTitleColor()));
                }

            }
        });
    }


    private String removeUrl(String commentstr) {
        String str = null;
        if (commentstr != null && !commentstr.trim().isEmpty()) {
            String temp = commentstr.replaceAll("\\(.*?\\) ?", "");
            String temp_1 = temp.replaceAll("\\[.*?\\] ?", "");
            str = temp_1.replaceAll("\\d", "");

            String urlPattern = "((https?|ftp|gopher|telnet|file|Unsure|http):((//)|(\\\\))+[\\w\\d:#@%/;$()~_?\\+-=\\\\\\.&]*)";
            Pattern p = Pattern.compile(urlPattern, Pattern.CASE_INSENSITIVE);
            Matcher m = p.matcher(str);
            int i = 0;
            while (m.find()) {
                str = commentstr.replaceAll(m.group(i), "").trim();
                i++;
            }
        } else {
            str = "";
        }
        return str;
    }


    public String getTitleFromFileName(String str) {
        if (str != null && str.length() > 0) {
            str = str.substring(0, str.length() - 4);
        }
        return str;
    }


    public byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int len = 0;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }

    @Override
    public void onLoadingStarted(String imageUri, View view) {
        updateToolbarColor();
    }

    @Override
    public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
        updateToolbarColor();
    }

    @Override
    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
        updateToolbarColor();
    }

    @Override
    public void onLoadingCancelled(String imageUri, View view) {
        updateToolbarColor();
    }

    void updateToolbarColor() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                generateColorFrom();
            }
        }, 100);


    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        mediaPlayer.stop();
        Intent intent = new Intent(TagEditor.this, MainActivity.class);
        intent.putExtra("scroll_pos", scroll_pos);

        startActivity(intent);
        overridePendingTransition(R.anim.enter_from_left, R.anim.exit_to_right);
    }
}
