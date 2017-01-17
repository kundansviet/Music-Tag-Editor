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
import android.os.Looper;
import android.os.Message;
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
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.acrcloud.utils.ACRCloudRecognizer;
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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class TagEditor extends AppCompatActivity implements RecyclerItemClickListener.OnItemClickListener,
        ImageLoadingListener, SaveDialog.SaveChangeCallback {

    String path, artwork, album_id;
    private ImageView iv_artwork;
    private ImageView iv_artwork_edit;
    private RelativeLayout iv_image_chooser;
    private EditText et_title, et_artist, et_album, et_genre, et_year;
    private IMusicMetadata metadata;
    private ImageLoader imageLoader;
    private DisplayImageOptions options;
    private Toolbar toolbar;
    private CollapsingToolbarLayout toolbar_layout;
    private boolean editable, editable_1, saved = true;
    private ProgressBar progress;
    private List<Result> isong_data;
    private RecyclerView rv_itune_song;
    private HorizontalSongListAdapter songListAdapter;
    private TextView tv_loading_text;
    private int camefrom, scroll_pos = 0, ACTION_REQUEST_GALLERY = 1991;
    private byte[] imageInByte;
    private FloatingActionButton fab, play_pause, rl_deep_search;
    private MediaPlayer mediaPlayer;
    private ACRCloudRecognizer re;
    private Map<String, Object> config;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tag_editor);
        isong_data = new ArrayList<>();
        intiImageLoader();
        Intent dataIntent = getIntent();
        scroll_pos = dataIntent.getIntExtra("scroll_pos", 0);
        camefrom = dataIntent.getIntExtra("from", 0);
        album_id = dataIntent.getStringExtra("album_id");
        artwork = dataIntent.getStringExtra("artwork");
        path = dataIntent.getStringExtra("path");
        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(path);
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }

        config = new HashMap<String, Object>();
        config.put("access_key", "76f5462329a29f2891f3b19b063363e0");
        config.put("access_secret", "r6KJtRWiE5AF672rBhGnibW3RK9pANCOg8cZOYhL");
        config.put("host", "ap-southeast-1.api.acrcloud.com");
        config.put("debug", false);
        config.put("timeout", 5);

        re = new ACRCloudRecognizer(config);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        progress = (ProgressBar) findViewById(R.id.progress);
        toolbar_layout = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);
        iv_artwork = (ImageView) findViewById(R.id.iv_artwork);
        iv_image_chooser = (RelativeLayout) findViewById(R.id.iv_image_chooser);
        et_title = (EditText) findViewById(R.id.et_title);
        et_album = (EditText) findViewById(R.id.et_album);
        et_artist = (EditText) findViewById(R.id.et_artist);
        et_genre = (EditText) findViewById(R.id.et_genre);
        et_year = (EditText) findViewById(R.id.et_year);
        rl_deep_search = (FloatingActionButton) findViewById(R.id.rl_deep_search);
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
                    rl_deep_search.setImageDrawable(ContextCompat.getDrawable(TagEditor.this, R.drawable.deep_search));
                    if (isConnected()) {
                        callItunesApiUsingTitleName();

                    } else {
                        turnOnEditText();
                    }

                } else {
                    fab.setImageDrawable(ContextCompat.getDrawable(TagEditor.this, R.drawable.edittag));
                    rl_deep_search.setImageDrawable(ContextCompat.getDrawable(TagEditor.this, R.drawable.deep_search));
                    turnOffEditText();
                    updateMetadata();

                }

            }
        });

        rl_deep_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                editable_1 = !editable_1;

                if (editable_1) {
                    rl_deep_search.setImageDrawable(ContextCompat.getDrawable(TagEditor.this, R.drawable.done));
                    fab.setImageDrawable(ContextCompat.getDrawable(TagEditor.this, R.drawable.edittag));
                    if (isConnected()) {
                        tv_loading_text.setVisibility(View.VISIBLE);
                        progress.setVisibility(View.VISIBLE);
                        tv_loading_text.setText("Fetching from server....");
                        rec();

                    } else {
                        turnOnEditText();
                    }

                } else {
                    fab.setImageDrawable(ContextCompat.getDrawable(TagEditor.this, R.drawable.edittag));
                    rl_deep_search.setImageDrawable(ContextCompat.getDrawable(TagEditor.this, R.drawable.deep_search));
                    turnOffEditText();
                    updateMetadata();

                }


            }
        });


        iv_image_chooser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent gallryIntent = new Intent();
                gallryIntent.setType("image/*");
                gallryIntent.setAction(Intent.ACTION_PICK);
                startActivityForResult(gallryIntent, ACTION_REQUEST_GALLERY);
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

    /**
     * method to initiate imageloader
     */
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

    /**
     * method to check internet connectivity
     *
     * @return
     */
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
        saved = false;
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
        saved = true;
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

            if (src_set!=null&&meta!=null){
                new MyID3().update(cFile, src_set, meta);
            }

        } catch (Exception e) {
            // TODO Auto-generated catch block
        }

        sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(cFile)));
    }


    /**
     * calling itunes api using title name
     */
    void callItunesApiUsingTitleName() {
        if (metadata != null) {
            tv_loading_text.setVisibility(View.VISIBLE);
            progress.setVisibility(View.VISIBLE);
            Call<Model> call = null;
            Retrofit retrofit = new RetroFitMaker().instanceMaker("https://itunes.apple.com");
            RestApi restApis = retrofit.create(RestApi.class);
            call = restApis.searchItunes("application/x-www-form-urlencoded", removeUrl(et_title.getText().toString()));


            call.enqueue(new Callback<Model>() {
                @Override
                public void onResponse(Call<Model> call, Response<Model> response) {
                    tv_loading_text.setVisibility(View.GONE);
                    progress.setVisibility(View.GONE);

                    if (response.isSuccessful()&&response.body().getResultCount() > 0) {
                        setSuggetionListAdapter(response.body().getResults());
                    } else {
                        callItunesApiUsingAlbumName();
                    }
                }

                @Override
                public void onFailure(Call<Model> call, Throwable t) {
                    rl_deep_search.setVisibility(View.VISIBLE);
                    progress.setVisibility(View.GONE);
                    tv_loading_text.setText("No match found");
                    Log.d("error", t.getMessage());
                    turnOnEditText();
                }
            });

        } else {
            rl_deep_search.setVisibility(View.VISIBLE);
            turnOnEditText();
        }

    }

    /**
     * calling itunes api using album name
     */
    void callItunesApiUsingAlbumName() {

        tv_loading_text.setVisibility(View.VISIBLE);
        progress.setVisibility(View.VISIBLE);
        Call<Model> call = null;
        Retrofit retrofit = new RetroFitMaker().instanceMaker("https://itunes.apple.com");
        RestApi restApis = retrofit.create(RestApi.class);

        call = restApis.searchItunes("application/x-www-form-urlencoded", removeUrl(et_album.getText().toString()));


        call.enqueue(new Callback<Model>() {
            @Override
            public void onResponse(Call<Model> call, Response<Model> response) {
                tv_loading_text.setVisibility(View.GONE);
                progress.setVisibility(View.GONE);
                if (response.isSuccessful()&&response.body().getResultCount() > 0) {
                    setSuggetionListAdapter(response.body().getResults());
                } else {
                    callItunesApiUsingArtistName();
                }
            }

            @Override
            public void onFailure(Call<Model> call, Throwable t) {
                rl_deep_search.setVisibility(View.VISIBLE);
                progress.setVisibility(View.GONE);
                tv_loading_text.setText("No match found");
                Log.d("error", t.getMessage());
                turnOnEditText();
            }
        });


    }


    /**
     * calling itunes api using artist name
     */

    void callItunesApiUsingArtistName() {

        tv_loading_text.setVisibility(View.VISIBLE);
        progress.setVisibility(View.VISIBLE);
        Call<Model> call = null;
        Retrofit retrofit = new RetroFitMaker().instanceMaker("https://itunes.apple.com");
        RestApi restApis = retrofit.create(RestApi.class);
        call = restApis.searchItunes("application/x-www-form-urlencoded", removeUrl(et_artist.getText().toString()));


        call.enqueue(new Callback<Model>() {
            @Override
            public void onResponse(Call<Model> call, Response<Model> response) {
                tv_loading_text.setVisibility(View.GONE);
                progress.setVisibility(View.GONE);
                if (response.isSuccessful()&&response.body().getResultCount() > 0) {
                    setSuggetionListAdapter(response.body().getResults());
                } else {
                    rl_deep_search.setVisibility(View.VISIBLE);
                    turnOnEditText();
                }
            }

            @Override
            public void onFailure(Call<Model> call, Throwable t) {
                rl_deep_search.setVisibility(View.VISIBLE);
                progress.setVisibility(View.GONE);
                tv_loading_text.setText("No match found");
                Log.d("error", t.getMessage());
                turnOnEditText();
            }
        });


    }

    /**
     * method to get color from image
     */
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


    /**
     * method to modify tags
     *
     * @param commentstr
     * @return
     */
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

        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            finish();
        }

       /* if (saved == true) {

        } else {
            SaveDialog dialog = SaveDialog.getInstance();
            dialog.show(getSupportFragmentManager(), "MyDialogFragment");
        }*/
        if (camefrom == 5) {
            Intent intent = new Intent(this, AlbumSongList.class);
            intent.putExtra("album_id", album_id);
            startActivity(intent);
            overridePendingTransition(R.anim.enter_from_left, R.anim.exit_to_right);
            finish();
        } else {
            Intent intent = new Intent(TagEditor.this, MainActivity.class);
            intent.putExtra("scroll_pos", scroll_pos);

            startActivity(intent);
            overridePendingTransition(R.anim.enter_from_left, R.anim.exit_to_right);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ACTION_REQUEST_GALLERY && resultCode == RESULT_OK) {
            Uri imgUri = data.getData();
            iv_artwork.setImageURI(imgUri);
        }
    }


    /**
     * method to set list adapter when data searched from itunes
     */
    void setSuggetionListAdapter(List<Result> isong_data_searched) {

        tv_loading_text.setVisibility(View.GONE);
        progress.setVisibility(View.GONE);
        rv_itune_song.setVisibility(View.VISIBLE);
        isong_data = new ArrayList<>();
        isong_data.addAll(isong_data_searched);
        songListAdapter = new HorizontalSongListAdapter(TagEditor.this, isong_data);
        rv_itune_song.setAdapter(songListAdapter);
        turnOnEditText();
        rl_deep_search.setVisibility(View.VISIBLE);
    }


    /**
     * call acrcloud in new thread
     */
    public void rec() {
        new RecThread().start();
    }

    @Override
    public void saveStatus(int b) {
        if (b == 0) {
            saved = true;
            onBackPressed();
        } else {

            saved = true;
            updateMetadata();
            onBackPressed();
        }
    }


    /**
     * method to call AcrCloud in new thread
     */
    class RecThread extends Thread {

        public void run() {


            File file = new File(path);
            byte[] buffer = new byte[3 * 1024 * 1024];
            if (!file.exists()) {
                return;
            }
            FileInputStream fin = null;
            int bufferLen = 0;
            try {
                fin = new FileInputStream(file);
                bufferLen = fin.read(buffer, 0, buffer.length);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (fin != null) {
                        fin.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (bufferLen <= 0)
                return;

            String result = re.recognizeByFileBuffer(buffer, bufferLen, 10);
            try {
                Message msg = new Message();
                msg.obj = result;

                msg.what = 1;
                mHandler.sendMessage(msg);
            } catch (Exception e) {
                e.printStackTrace();
                tv_loading_text.setVisibility(View.GONE);
                progress.setVisibility(View.GONE);

            }
        }
    }


    /**
     * Handler to handle acrcloud response
     */
    private Handler mHandler = new Handler(Looper.getMainLooper()) {

        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case 1:
                    String res = (String) msg.obj;
                    try {
                        JSONObject jsonResult = new JSONObject(res);
                        if (jsonResult.has("metadata")) {
                            JSONObject metadata = jsonResult.getJSONObject("metadata");
                            JSONArray music = metadata.getJSONArray("music");

                            JSONObject albumobj = music.getJSONObject(0);
                            if (albumobj.has("album")) {
                                JSONObject album = albumobj.getJSONObject("album");
                                et_album.setText(album.optString("name"));
                            }
                            if (albumobj.has("title")) {

                                et_title.setText(albumobj.getString("title"));
                            }

                            if (albumobj.has("artists")) {
                                JSONArray artist_array = albumobj.getJSONArray("artists");
                                JSONObject artist_obj = artist_array.getJSONObject(0);
                                et_artist.setText(artist_obj.getString("name"));

                            }

                            tv_loading_text.setVisibility(View.GONE);
                            progress.setVisibility(View.GONE);
                            callItunesApiUsingTitleName();
                        } else {
                            tv_loading_text.setVisibility(View.GONE);
                            progress.setVisibility(View.GONE);
                        }


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    break;

                default:
                    break;
            }
        }

        ;
    };
}
