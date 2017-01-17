package com.aw.musictagger;


import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.content.CursorLoader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

import static android.R.attr.path;
import static android.app.Activity.RESULT_OK;


/**
 * A simple {@link Fragment} subclass.
 */
public class FileChooser extends Fragment {

    private TextView et_path;
    private ImageView ib_browse;
    private LinearLayout ll_edit;
    private final int ACTIVITY_CHOOSE_FILE = 1;
    private String path = "",imgartwork;
    public FileChooser() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View convertview = inflater.inflate(R.layout.fragment_file_chooser, container, false);

        et_path= (TextView) convertview.findViewById(R.id.et_path);
        ib_browse= (ImageView) convertview.findViewById(R.id.ib_browse);

        ib_browse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent chooseFile;
                Intent intent;
                chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
                chooseFile.setType("audio/*");
                intent = Intent.createChooser(chooseFile, "Choose a file");
                startActivityForResult(intent, ACTIVITY_CHOOSE_FILE);
            }
        });



        ll_edit= (LinearLayout) convertview.findViewById(R.id.ll_edit);
        ll_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (et_path.getText()!=null&&et_path.getText().length()>0){
                    Intent editIntent = new Intent(getActivity(), TagEditor.class);
                    editIntent.putExtra("path", path);
                    editIntent.putExtra("from",1);
                    editIntent.putExtra("artwork",imgartwork  );
                    startActivity(editIntent);
                    getActivity().overridePendingTransition(R.anim.enter_from_right,R.anim.rest);
                    getActivity().finish();
                }else {
                   Toast.makeText(getActivity(),"Please select a file to edit",Toast.LENGTH_SHORT).show();
                }

            }
        });

        return convertview;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case ACTIVITY_CHOOSE_FILE: {
                if (resultCode == RESULT_OK) {
                    Uri uri = data.getData();
                    path = getRealPathFromURI(uri);
                    getMusicData(uri);
                    et_path.setText(path);
                }
            }
        }
    }

    private String getRealPathFromURI(Uri contentUri) {
        String[] proj = {MediaStore.Audio.Media.DATA};
        CursorLoader loader = new CursorLoader(getActivity(), contentUri, proj, null, null, null);
        Cursor cursor = loader.loadInBackground();
        int column_index = 0;
        try {
            column_index = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }


    void getMusicData(Uri contentUri){
        String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";
        String[] projection = {

                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.ALBUM_ID,
        };

        Cursor audioCursor = getActivity().getContentResolver().query(contentUri, projection, selection, null, null);


    }

}
