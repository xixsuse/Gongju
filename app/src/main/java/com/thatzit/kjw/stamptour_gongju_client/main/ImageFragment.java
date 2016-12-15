package com.thatzit.kjw.stamptour_gongju_client.main;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.thatzit.kjw.stamptour_gongju_client.R;

import java.io.File;

/**
 * Created by kjw on 16. 9. 17..
 */
public class ImageFragment extends android.app.Fragment{
    private static final String KEY_CONTENT = "ImageFragment:Content";
    private String mContent = "";
    private View view;
    private ImageView item;
    private final String TAG = "ImageFragment";

    public static android.app.Fragment newInstance(String content) {
        ImageFragment fragment = new ImageFragment();
        fragment.mContent = content;
        return fragment;
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if ((savedInstanceState != null) && savedInstanceState.containsKey(KEY_CONTENT)) {
            mContent = savedInstanceState.getString(KEY_CONTENT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.image_fragment, container, false);
        setLayout();
        return view;
    }

    private void setLayout() {

        Log.e(TAG,"setLayout : "+mContent);
        item = (ImageView) view.findViewById(R.id.pager_img);
        File img = new File(mContent);
        Drawable place_img = getResources().getDrawable(R.drawable.img_img_load);
        Glide.with(item.getContext())
                .load(img)
                .centerCrop()
                .error(place_img)
                .into(item);

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_CONTENT, mContent);
    }
}
