package com.thatzit.kjw.stamptour_gongju_client.main.adapter;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.drawable.TransitionDrawable;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.thatzit.kjw.stamptour_gongju_client.R;
import com.thatzit.kjw.stamptour_gongju_client.main.TownDTO;
import com.thatzit.kjw.stamptour_gongju_client.main.msgListener.StampSealListnenr;
import com.thatzit.kjw.stamptour_gongju_client.preference.PreferenceManager;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Suleiman on 14-04-2015.
 */
public class MainRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private ArrayList<TownDTO> mListData = new ArrayList<TownDTO>();
    Context context;
    OnItemClickListener clickListener;
    OnItemLongClickListener longClickListener;
    private StampSealListnenr listnenr;
    private final String TAG ="MainRecyclerAdapter";
    private ObjectAnimator currentAnimation;
    public TransitionDrawable background;
    public Handler handler;
    private boolean stampFlag = true;
    private PreferenceManager preferenceManager;

    public MainRecyclerAdapter(Context context) {
        this.context = context;
        preferenceManager = new PreferenceManager(this.context);

    }
    public MainRecyclerAdapter(Context context, ArrayList<TownDTO> mListData) {
        this.context = context;
        this.mListData = mListData;
    }

    public TownDTO getmListData(int position) {
        return mListData.get(position);
    }

    public MainRecyclerAdapter(ArrayList<TownDTO> mtownlist) {
        this.mListData = mtownlist;
    }
    public void additem(TownDTO data){
        this.mListData.add(data);
    }
    public void removeitem(int position){
        this.mListData.remove(position);
    }
    public void removelist(){
        this.mListData.clear();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {

        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.townlistitem, viewGroup, false);
        return new NormalViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        if(viewHolder instanceof NormalViewHolder){
//            StampAnimationView stampAnimationView = new StampAnimationView(context,position);
//            stampAnimationView.SetOnStampASealListener(listnenr);

            String sdcard= Environment.getExternalStorageDirectory().getAbsolutePath();
            String no;
//            no = position + 1+"";
            no = mListData.get(position).getNo();
            Log.e(TAG,position+":"+mListData.get(position).getName());
            Log.e(TAG,position+":"+mListData.get(position).getRegion());
            Log.e(TAG,position+":"+mListData.get(position).getDistance());
            ((NormalViewHolder)viewHolder).name_text_view.setText("");
            ((NormalViewHolder)viewHolder).distance_text_view.setText("");
            ((NormalViewHolder)viewHolder).region_text_view.setText("");
            ((NormalViewHolder)viewHolder).town_checkedtime_view.setText("");
            ((NormalViewHolder)viewHolder).town_checkedcount_view.setText("");

            ((NormalViewHolder)viewHolder).name_text_view.setText(mListData.get(position).getName());
            ((NormalViewHolder)viewHolder).distance_text_view.setText(mListData.get(position).getDistance());
            ((NormalViewHolder)viewHolder).region_text_view.setText(mListData.get(position).getRegion());
            ((NormalViewHolder)viewHolder).town_checkedtime_view.setText(mListData.get(position).getStamp_checked());
            if(!mListData.get(position).getRank_no().equals("0"))
                ((NormalViewHolder)viewHolder).town_checkedcount_view.setText(mListData.get(position).getRank_no());

            //animation and imgview 잔상 초기화

            ((NormalViewHolder)viewHolder).stamp_checked_imgview.setVisibility(View.INVISIBLE);
            ((NormalViewHolder)viewHolder).footimg_view.setVisibility(View.INVISIBLE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                ((NormalViewHolder)viewHolder).item_container.setBackground(context.getDrawable(R.drawable.town_list_item_animatebg));
            }else{
                ((NormalViewHolder)viewHolder).item_container.setBackground(ContextCompat.getDrawable(context,R.drawable.town_list_item_animatebg));
            }

            String dirPath = sdcard+"/StampTour_gongju/contents/contents/town"+no+"_1.png";
            Log.e("ListAdapter",dirPath);
            File img = new File(dirPath);
            Glide.with(((NormalViewHolder)viewHolder).town_img_view.getContext())
                    .load(img)
                    .centerCrop()
                    .into(((NormalViewHolder)viewHolder).town_img_view);
            if(!mListData.get(position).getStamp_checked().equals("")){
                ((NormalViewHolder)viewHolder).stamp_checked_imgview.setVisibility(View.VISIBLE);
                ((NormalViewHolder)viewHolder).footimg_view.setVisibility(View.VISIBLE);
            }

            if(mListData.get(position).isStamp_on()){
                Log.e("isStamp_on","call");
                if(mListData.get(position).getStamp_checked().equals("")){

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        ((NormalViewHolder)viewHolder).region_text_view.setTextColor(context.getColor(R.color.stamp_list_item_text_color_alpha_on));
                    }else{
                        ((NormalViewHolder)viewHolder).region_text_view.setTextColor(ContextCompat.getColor(context,R.color.stamp_list_item_text_color_alpha_on));
                    }
                    background = (TransitionDrawable) ((NormalViewHolder)viewHolder).item_container.getBackground();
                    Handler hd = new Handler();
                    if(handler!=null)handler.removeMessages(0);
                    handler = new Handler()
                    {
                        public void handleMessage(Message msg)
                        {
                            super.handleMessage(msg);

                            // 할일들을 여기에 등록
                            background.startTransition(1000);
                            background.reverseTransition(1000);
                            this.sendEmptyMessageDelayed(0, 2000);        // REPEAT_DELAY 간격으로 계속해서 반복하게 만들어준다
                        }
                    };
                    handler.sendEmptyMessage(0);

                }
            }
        }

    }


    private class splashhandler implements Runnable{
        public void run() {

            background.startTransition(1000);
            background.reverseTransition(1000);
        }
    }
    @Override
    public int getItemCount() {
        return mListData == null ? 0 : mListData.size();
    }

    private TownDTO getItem(int position) {
        return mListData.get(position);
    }
    class NormalViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,View.OnLongClickListener {
        public ImageView town_img_view;
        public TextView name_text_view;
        public TextView region_text_view;
        public TextView distance_text_view;
        public TextView town_checkedtime_view;
        public TextView town_checkedcount_view;
        public RelativeLayout item_container;
        public ImageView stamp_checked_imgview;
        public ImageView footimg_view;
        public NormalViewHolder(View itemView) {
            super(itemView);

            town_img_view = (ImageView)itemView.findViewById(R.id.town_img_view);
            name_text_view = (TextView)itemView.findViewById(R.id.town_name_view);
            region_text_view = (TextView)itemView.findViewById(R.id.town_region_view);
            distance_text_view = (TextView)itemView.findViewById(R.id.town_distance_view);
            town_checkedtime_view = (TextView)itemView.findViewById(R.id.town_checkedtime_view);
            town_checkedcount_view = (TextView)itemView.findViewById(R.id.town_checkedcount_view);
            stamp_checked_imgview = (ImageView)itemView.findViewById(R.id.stamp_checked_imgview);
            footimg_view = (ImageView)itemView.findViewById(R.id.footimg_view);
            item_container = (RelativeLayout)itemView.findViewById(R.id.item_container);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }
        @Override
        public void onClick(View v) {
            clickListener.onItemClick(v, getPosition());
        }

        @Override
        public boolean onLongClick(View v) {
            longClickListener.onItemLongClick(v, getPosition());
            return true;
        }
    }
    public interface OnItemClickListener {
        public void onItemClick(View view, int position);
    }
    public interface OnItemLongClickListener {
        public void onItemLongClick(View view, int position);
    }
    public void SetOnItemClickListener(final OnItemClickListener itemClickListener) {
        this.clickListener = itemClickListener;
    }
    public void SetOnItemLongClickListener(final OnItemLongClickListener itemLongClickListener) {
        this.longClickListener = itemLongClickListener;
    }

    public void SetOnStampASealListener(StampSealListnenr listnenr) {
        this.listnenr = listnenr;
    }
}
