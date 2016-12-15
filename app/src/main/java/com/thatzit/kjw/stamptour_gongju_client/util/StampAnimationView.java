package com.thatzit.kjw.stamptour_gongju_client.util;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import com.thatzit.kjw.stamptour_gongju_client.R;
import com.thatzit.kjw.stamptour_gongju_client.main.TownDTO;
import com.thatzit.kjw.stamptour_gongju_client.main.msgListener.StampSealListnenr;


/**
 * Created by csc-pc on 2016. 10. 24..
 */

public class StampAnimationView extends Dialog implements View.OnClickListener{
    private Context context;
    private ImageView waitAnimation;
    private AnimationDrawable frameAnimation;
    private int position;
    private TownDTO dto;
    private StampSealListnenr listnenr;
    private final String TAG ="MainRecyclerAdapter";

    public int getPosition() {
        return position;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowManager.LayoutParams lpWindow = new WindowManager.LayoutParams();
        lpWindow.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        lpWindow.dimAmount = 0.5f;
        lpWindow.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lpWindow.width = WindowManager.LayoutParams.WRAP_CONTENT;
        getWindow().setAttributes(lpWindow);

        setContentView(R.layout.dialog_stamp_seal);
        setAnimationWait();
        Log.e("daaaaaaaaa","onCreate()");
    }

    public void goShow(TownDTO dto){
        this.dto = dto;
        setAnimationWait();
        this.show();
    }

    public void setAnimationWait() {
        Log.e("daaaaaaaaa","setAnimationWait()");
        waitAnimation = (ImageView)findViewById(R.id.dialog_stamp_seal_imageview);
        waitAnimation.setVisibility(View.VISIBLE);
        waitAnimation.setOnClickListener(this);
        waitAnimation.setBackgroundResource(R.drawable.animationlist);
        frameAnimation = (AnimationDrawable)waitAnimation.getBackground();
        waitAnimation.animate().alpha(1);
        startAnimation();
    }

    public void startAnimation(){
        if(frameAnimation.isRunning()){
            frameAnimation.stop();
        }

        frameAnimation.start();
    }


    public void stopAnimation(){
        if(frameAnimation.isRunning()){
            frameAnimation.stop();
        }

    }


    @Override
    public void onBackPressed() {
        // super.onBackPressed();
    }

    @Override
    public void dismiss() {
        waitAnimation.setVisibility(View.GONE);
        frameAnimation.stop();
        super.dismiss();
    }
    public StampAnimationView(Context context) {
        super(context,android.R.style.Theme_Translucent_NoTitleBar);
        this.context = context;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.dialog_stamp_seal_imageview:
                listnenr.OnStampASeal(dto);
                dismiss();
                break;

        }
    }


    public void SetOnStampASealListener(StampSealListnenr listnenr) {
        this.listnenr = listnenr;
    }
}


