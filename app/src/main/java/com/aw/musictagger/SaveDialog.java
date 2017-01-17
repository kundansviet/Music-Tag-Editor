package com.aw.musictagger;


import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;


/**
 * A simple {@link Fragment} subclass.
 */
public class SaveDialog extends DialogFragment {
    private TextView tv_yes, tv_no;
    private Context context;
    SaveChangeCallback callback;

    public SaveDialog() {
        // Required empty public constructor
    }

    public static SaveDialog getInstance() {
        return new SaveDialog();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        View v = inflater.inflate(R.layout.fragment_save_dialog, container, false);
        tv_no = (TextView) v.findViewById(R.id.tv_no);
        tv_yes = (TextView) v.findViewById(R.id.tv_yes);

        tv_no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.saveStatus(0);
            }
        });
        tv_yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.saveStatus(1);
            }
        });
        return v;
    }

    public interface SaveChangeCallback {
        public void saveStatus(int b);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        callback = (SaveChangeCallback) activity;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();

        Dialog dialog = getDialog();
        if (dialog != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }


}
