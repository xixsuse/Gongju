package com.thatzit.kjw.stamptour_gongju_client.main;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.ListPopupWindow;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.share.Sharer;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;
import com.kakao.util.KakaoParameterException;
import com.kakao.kakaolink.KakaoLink;
import com.kakao.kakaolink.KakaoTalkLinkMessageBuilder;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.thatzit.kjw.stamptour_gongju_client.R;
import com.thatzit.kjw.stamptour_gongju_client.checker.ExternalMemoryDTO;
import com.thatzit.kjw.stamptour_gongju_client.checker.LocaleChecker;
import com.thatzit.kjw.stamptour_gongju_client.checker.UsableStorageChecker;
import com.thatzit.kjw.stamptour_gongju_client.hide.HideListActivity;
import com.thatzit.kjw.stamptour_gongju_client.hide.HideStatus;
import com.thatzit.kjw.stamptour_gongju_client.http.ResponseCode;
import com.thatzit.kjw.stamptour_gongju_client.http.ResponseKey;
import com.thatzit.kjw.stamptour_gongju_client.http.ResponseMsg;
import com.thatzit.kjw.stamptour_gongju_client.http.StampRestClient;
import com.thatzit.kjw.stamptour_gongju_client.login.LoggedInCase;
import com.thatzit.kjw.stamptour_gongju_client.main.adapter.MainRecyclerAdapter;
import com.thatzit.kjw.stamptour_gongju_client.main.adapter.PopUpAdapter;
import com.thatzit.kjw.stamptour_gongju_client.main.event.ListChangeEvent;
import com.thatzit.kjw.stamptour_gongju_client.main.fileReader.LoadAsyncTask;
import com.thatzit.kjw.stamptour_gongju_client.main.msgListener.ListChangeListener;
import com.thatzit.kjw.stamptour_gongju_client.main.msgListener.ParentGpsStateListener;
import com.thatzit.kjw.stamptour_gongju_client.main.msgListener.ParentLocationListener;
import com.thatzit.kjw.stamptour_gongju_client.main.msgListener.StampSealListnenr;
import com.thatzit.kjw.stamptour_gongju_client.preference.LoggedInInfo;
import com.thatzit.kjw.stamptour_gongju_client.preference.PreferenceManager;
import com.thatzit.kjw.stamptour_gongju_client.push.service.event.GpsStateEvent;
import com.thatzit.kjw.stamptour_gongju_client.push.service.event.LocationEvent;
import com.thatzit.kjw.stamptour_gongju_client.util.MyApplication;
import com.thatzit.kjw.stamptour_gongju_client.util.ProgressWaitDaialog;
import com.thatzit.kjw.stamptour_gongju_client.util.StampAnimationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

import static com.facebook.FacebookSdk.getApplicationContext;

public class MainFragment extends Fragment implements MainRecyclerAdapter.OnItemClickListener, View.OnClickListener,
        MainRecyclerAdapter.OnItemLongClickListener, ParentLocationListener, ParentGpsStateListener,
        PopupMenu.OnMenuItemClickListener, ListChangeListener, StampSealListnenr {

    CollapsingToolbarLayout collapsingToolbar;
    RecyclerView recyclerView;
    int mutedColor = R.attr.colorPrimary;
    MainRecyclerAdapter mainRecyclerAdapter;
    private LinearLayoutManager linearLayoutManager;
    private View view;
    private LocationEvent currentLocation;
    private final String TAG = "MainFragment";
    private static boolean turnOff=true;
    private TextView sort_btn;
    private TextView hide_btn;
    private TextView sort_mode_textview;
    private TextView share_button;
    private TextView firstline_text_view;
    private TextView secondline_cnt_text_view;
    private TextView secondline_nextcnt_text_view;

    private int sort_mode;
    private int setting_flag = 0;
    private String current_req_url;
    private String accesstoken;
    private View progressbar;
    private PreferenceManager preferenceManager;
    private ProgressWaitDaialog progressWaitDaialog;
    private LoggedInInfo user;
    private boolean req_flag;
    private ArrayList<TempTownDTO> UserTownInfo_arr;
    private ImageView header;
    private String nick;
    private String grade;
    private String zosa;
    private String last_string;

    private static final int HIDEACTIVITYSTART = 7777;
    private static final int HIDELISTCHANGED = 7778;
    private static final int HIDELISTUNCHANGED = 7779;
    private MainActivity parent;
    private boolean turnOnGpsShow = true;
    private CallbackManager callbackManager;
    private ShareDialog shareDialog;
    private KakaoLink kakaoLink;
    private KakaoTalkLinkMessageBuilder kakaoTalkLinkMessageBuilder;

    private StampAnimationView stampAnimationView;
    private LocaleChecker localeChecker;

    private boolean stamp_on = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.main_fragment, container, false);
        preferenceManager = new PreferenceManager(getActivity());
        preferenceManager.setAgoIsStampOn("EMPTY");
        progressWaitDaialog = new ProgressWaitDaialog(view.getContext());
        stampAnimationView = new StampAnimationView(view.getContext());
        req_flag = false;
        UserTownInfo_arr = ((MainActivity)getActivity()).UserTownInfo_arr;
        setLayout();
        return view;
    }
    private void setLayout(){
        localeChecker = new LocaleChecker(getActivity());
        final Toolbar toolbar = (Toolbar) view.findViewById(R.id.anim_toolbar);
        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        collapsingToolbar = (CollapsingToolbarLayout) view.findViewById(R.id.collapsing_toolbar);
        collapsingToolbar.setTitle(" ");


//        header = (ImageView) view.findViewById(R.id.header);
//        header.setOnClickListener(this);

        firstline_text_view = (TextView)view.findViewById(R.id.firstline_text_view);
        secondline_cnt_text_view = (TextView)view.findViewById(R.id.secondline_cnt_text_view);
        secondline_nextcnt_text_view = (TextView)view.findViewById(R.id.secondline_nextcnt_text_view);

        sort_btn = (TextView) view.findViewById(R.id.sort_btn);
        hide_btn = (TextView) view.findViewById(R.id.hide_btn);
        share_button = (TextView) view.findViewById(R.id.share_button);
        sort_mode_textview = (TextView) view.findViewById(R.id.sort_mode_textview);
        sort_btn.setOnClickListener(this);
        hide_btn.setOnClickListener(this);
        share_button.setOnClickListener(this);
        recyclerView = (RecyclerView) view.findViewById(R.id.scrollableview);

        recyclerView.setHasFixedSize(true);
        linearLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(linearLayoutManager);
        mainRecyclerAdapter = new MainRecyclerAdapter(view.getContext());
        recyclerView.setAdapter(mainRecyclerAdapter);
        parent = (MainActivity) getActivity();
        parent.setOnListChangeListener(this);
        mainRecyclerAdapter.SetOnItemClickListener(this);
        mainRecyclerAdapter.SetOnItemLongClickListener(this);
        mainRecyclerAdapter.SetOnStampASealListener(this);
        ((MainActivity)getActivity()).setParentLocationListener(this);
        ((MainActivity)getActivity()).setParentGpsStateListener(this);
        progressbar = view.findViewById(R.id.list_progressbar);
        UsableStorageChecker usableStorageChecker = new UsableStorageChecker();
        ExternalMemoryDTO check_result = usableStorageChecker.check_ext_memory();
        Log.d(TAG,check_result.toString());

        current_request();
        sort_load_before_check();

    }
    private void current_request() {
        RequestParams requestParams = new RequestParams();
        current_req_url = MyApplication.getContext().getString(R.string.req_url_current_stamp);
        nick = preferenceManager.getLoggedIn_Info().getNick();
        accesstoken = preferenceManager.getLoggedIn_Info().getAccesstoken();
        zosa = getString(R.string.zosa);
        last_string = getString(R.string.isgrade);
        requestParams.put(ResponseKey.NICK.getKey(),nick);
        requestParams.put(ResponseKey.TOKEN.getKey(),accesstoken);
        StampRestClient.post(current_req_url,requestParams,new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray result) {

            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

                String code = null;
                String msg = null;
                Log.e("this request","come");
                try {
                    Log.e("this request","try");
                    code = response.getString(ResponseKey.CODE.getKey());
                    msg = response.getString(ResponseKey.MESSAGE.getKey());
                    if(code.equals(ResponseCode.SUCCESS.getCode())&&msg.equals(ResponseMsg.SUCCESS.getMessage())){
                        Log.e("this request","parse");
                        JSONObject resultData = response.getJSONObject(ResponseKey.RESULTDATA.getKey());
                        int res_next_stamp_count = resultData.getInt("next_stamp_count");
                        int res_stamp_count = resultData.getInt("stamp_count");
                        String res_grade;
                        String res_nick = resultData.getString("nick");
                        localeChecker.check();
                        String locale = localeChecker.check_return_locale();
                        switch (locale){
                            case "ko": res_grade = resultData.getString("grade");
                                break;
                            case "en": res_grade = resultData.getString("grade_en");
                                break;
                            default:  res_grade = resultData.getString("grade");
                                break;
                        }
//                        String res_grade = resultData.getString("grade");

                        nick = res_nick;
                        grade = res_grade;
                        String space = " ";
                        String firstline = nick+zosa+space+grade+space+last_string;
                        firstline_text_view.setText(firstline);
                        secondline_cnt_text_view.setText(res_stamp_count+"");
                        secondline_nextcnt_text_view.setText(res_next_stamp_count+"");
                        Log.e("this request","nick : "+res_nick+"\ngrade : "+res_grade+"\ncur_stamp : "+
                                res_stamp_count+"\nnext_stamp : "+res_next_stamp_count);
                        sort_load_before_check();
                    }else{
                        Log.e("this request else",code+":"+msg);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.e("this request","exception");
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
            }
        });
    }

    private void request_TownUserInfo() {
        user = preferenceManager.getLoggedIn_Info();
        progressWaitDaialog.show();
        String req_url = this.getString(R.string.req_url_town_list);
        RequestParams requestParams = new RequestParams();
        requestParams.put(ResponseKey.NICK.getKey(),user.getNick());
        requestParams.put(ResponseKey.TOKEN.getKey(),user.getAccesstoken());
        StampRestClient.post(req_url,requestParams,new JsonHttpResponseHandler(){
            private ArrayList<TempTownDTO> make_TownDataList(JSONArray resultData) {
                ArrayList<TempTownDTO> array = new ArrayList<TempTownDTO>();
                JSONObject town;
                for(int i = 0 ; i < resultData.length() ; i++){
                    try {
                        town = (JSONObject) resultData.get(i);
                        localeChecker.check();
                        String locale = localeChecker.check_return_locale();
                        switch (locale){
                            case "ko": array.add(new TempTownDTO(town.getString("TOWN_CODE"),town.getString("Nick"),town.getString("CheckTime"),town.getString("region"),town.getString("rank_no")));
                                break;
                            case "en": array.add(new TempTownDTO(town.getString("TOWN_CODE"),town.getString("Nick"),town.getString("CheckTime"),town.getString("region_en"),town.getString("rank_no")));
                                break;
                            default: array.add(new TempTownDTO(town.getString("TOWN_CODE"),town.getString("Nick"),town.getString("CheckTime"),town.getString("region"),town.getString("rank_no")));
                                break;
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
                return array;
            }
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                progressWaitDaialog.dismiss();
                String code = null;
                String msg = null;

                try {
                    code = response.getString(ResponseKey.CODE.getKey());
                    msg = response.getString(ResponseKey.MESSAGE.getKey());
                    if(code.equals(ResponseCode.SUCCESS.getCode())&&msg.equals(ResponseMsg.SUCCESS.getMessage())){
                        JSONArray resultData = response.getJSONArray(ResponseKey.RESULTDATA.getKey());
                        JSONObject data = (JSONObject) resultData.get(0);
                        UserTownInfo_arr = make_TownDataList(resultData);
                        Log.e(TAG,"town_code : "+data.getString("TOWN_CODE")+"\nCheckTime : "+data.getString("CheckTime")+"\nRange : "+data.getString("valid_range"));
                        req_flag = true;
                        current_request();

                    }else{
                        Log.e(TAG,code+":"+msg);
                        JSONObject resultData = null;
                        req_flag = false;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    req_flag = false;
                }
            }



            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                super.onSuccess(statusCode, headers, response);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                progressWaitDaialog.dismiss();
                req_flag = false;
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
            }
        });
    }

    private void popUpShow() {

        //normal popup
        PopupMenu popup = new PopupMenu(getActivity(), sort_btn);
        MenuInflater inflater = popup.getMenuInflater();
        popup.setOnMenuItemClickListener(this);
        inflater.inflate(R.menu.menu_main, popup.getMenu());
        popup.show();

    }

    private void custom_PopUpShow() {
        ListPopupWindow listPopupWindow = new ListPopupWindow(
                getActivity());
        ArrayList<String> arGeneral = new ArrayList<String>();
        arGeneral.add(getString(R.string.distance_string));
        arGeneral.add(getString(R.string.area_string));
        arGeneral.add(getString(R.string.name_string));

        PopUpAdapter popUpAdapter = new PopUpAdapter(arGeneral, R.layout.popup_item,getActivity());
        listPopupWindow.setAdapter(popUpAdapter);
        listPopupWindow.setAnchorView(sort_btn);
        listPopupWindow.setWidth((int) getResources().getDimension(R.dimen.popup_width));
        listPopupWindow.setHeight((int) getResources().getDimension(R.dimen.popup_height));

        listPopupWindow.setModal(true);
        //listPopupWindow.setOnItemClickListener(this);

        listPopupWindow.show();
    }

    @Override
    public void onItemClick(View view, int position) {
        Log.e("RecycleitemClick","position = "+position);
        Intent intent = new Intent(getActivity(),DetailActivity.class);
        TownDTO town = mainRecyclerAdapter.getmListData(position);
        intent.putExtra("town",Integer.parseInt(town.getNo()));
        getActivity().startActivity(intent);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.header:
//                linearLayoutManager.scrollToPositionWithOffset(7, collapsingToolbar.getBottom());
//                Toast.makeText(getContext(),"이미지클릭",Toast.LENGTH_LONG).show();
                break;
//                stampAnimationView.show();
//                stampAnimationView.startAnimation();
//                break;
            case R.id.hide_btn:
//                Toast.makeText(getContext(),"숨김관리버튼클릭",Toast.LENGTH_LONG).show();
                Intent intent = new Intent(getActivity(),HideListActivity.class);
                getActivity().startActivityForResult(intent,HIDEACTIVITYSTART);
                break;
            case R.id.sort_btn:
                popUpShow();
//                Toast.makeText(getContext(),"정렬버튼클릭",Toast.LENGTH_LONG).show();
                break;
            case R.id.share_button:
                LoggedInInfo loggedin_info = preferenceManager.getLoggedIn_Info();
                if(loggedin_info.getLoggedincase().equals("NORMAL")){
                    break;
                }else{
                    shareSocial(loggedin_info.getLoggedincase());
                }
                //Toast.makeText(getContext(),"공유클릭",Toast.LENGTH_LONG).show();
                break;
        }
    }

    private void shareSocial(String loggedincase) {
        if(loggedincase.equals(LoggedInCase.FBLogin.getLogin_case())){
            fbShare();
        }else if(loggedincase.equals(LoggedInCase.KAKAOLogin.getLogin_case())){
            kakaoShare();
        }
    }


    private void fbShare() {
        callbackManager = CallbackManager.Factory.create();
        shareDialog = new ShareDialog(getActivity());
        shareDialog.registerCallback(callbackManager, new FacebookCallback<Sharer.Result>() {
            @Override
            public void onSuccess(Sharer.Result result) {
                Log.e("Facebook share test", result.getPostId() + "");
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {
                Log.e("Facebook error", error.getLocalizedMessage());
            }
        });
        if (ShareDialog.canShow(ShareLinkContent.class)) {
            ShareLinkContent shareLinkContent = new ShareLinkContent.Builder()
                    .setContentTitle(getString(R.string.FB_Share_Title))
                    .setContentDescription(preferenceManager.getLoggedIn_Info().getNick()+getString(R.string.FB_Share_Description))
                    .setContentUrl(Uri.parse(getString(R.string.FB_Share_APPUrl)))
                    .build();
            shareDialog.show(shareLinkContent);
        }
    }

    private void kakaoShare() {
        try {
            kakaoLink = KakaoLink.getKakaoLink(getApplicationContext());
            kakaoTalkLinkMessageBuilder = kakaoLink.createKakaoTalkLinkMessageBuilder();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            kakaoTalkLinkMessageBuilder.addText(getString(R.string.FB_Share_Title));
//            kakaoTalkLinkMessageBuilder.addImage()
            kakaoTalkLinkMessageBuilder.addAppLink(getString(R.string.FB_Share_Title2));
            kakaoTalkLinkMessageBuilder.addWebButton(getString(R.string.FB_Share_Title2),getString(R.string.FB_Share_APPUrl));
            kakaoTalkLinkMessageBuilder.build();
            kakaoLink.sendMessage(kakaoTalkLinkMessageBuilder, getActivity());
        } catch (KakaoParameterException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void onItemLongClick(View view, int position) {
        Log.e("RecycleitemLongClick","position = "+position);
        TownDTO data = mainRecyclerAdapter.getmListData(position);
        if(!data.isStamp_on()){
            Toast.makeText(getActivity(),data.getName()+getResources().getString(R.string.hide_notfication_text),Toast.LENGTH_LONG).show();
            preferenceManager.setTownHideStatus(data.getNo(), HideStatus.HIDE.getStatus());
            sort_load_before_check();
        }
        if(currentLocation==null)return;

    }

    @Override
    public void OnReceivedLocation(LocationEvent locationEvent) {
        Log.e(TAG,locationEvent.getLocation().getLatitude()+":"+locationEvent.getLocation().getLongitude());
        currentLocation = locationEvent;
        new LoadAsyncTask(firstline_text_view, secondline_cnt_text_view, secondline_nextcnt_text_view, sort_mode_textview,UserTownInfo_arr,sort_mode,currentLocation,mainRecyclerAdapter, MyApplication.getContext(),stampAnimationView).execute();

    }

    @Override
    public void OnReceivedParentStateEvent(GpsStateEvent event) {
        Log.e(TAG,event.isState()+"");
        if(event.isState() == false){
            if(turnOff == false)
            {
                return;
            }else{
                turnOff = event.isState();
                currentLocation = null;
                new LoadAsyncTask(firstline_text_view, secondline_cnt_text_view, secondline_nextcnt_text_view, sort_mode_textview, UserTownInfo_arr, sort_mode,currentLocation,mainRecyclerAdapter,MyApplication.getContext(),stampAnimationView).execute();
            }

        }else{
            turnOff = event.isState();
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        LoadAsyncTask loadAsyncTask = null;
        switch (item.getItemId()) {

            case R.id.action_sort_distance:
                //Toast.makeText(getContext(),"거리클릭",Toast.LENGTH_LONG).show();
                sort_load_before_check();
                break;
            case R.id.action_sort_name:
                // Toast.makeText(getContext(),"이름클릭",Toast.LENGTH_LONG).show();
                sort_mode = 1;
                loadAsyncTask = new LoadAsyncTask(firstline_text_view, secondline_cnt_text_view,
                        secondline_nextcnt_text_view, sort_mode_textview, UserTownInfo_arr,
                        sort_mode,currentLocation,mainRecyclerAdapter,MyApplication.getContext(),stampAnimationView);
                loadAsyncTask.setOnStampSealListener(this);
                loadAsyncTask.execute();
                break;
            case R.id.action_sort_region:
                // Toast.makeText(getContext(),"권역클릭",Toast.LENGTH_LONG).show();
                sort_mode = 2;
                loadAsyncTask = new LoadAsyncTask(firstline_text_view, secondline_cnt_text_view,
                        secondline_nextcnt_text_view, sort_mode_textview, UserTownInfo_arr,
                        sort_mode,currentLocation,mainRecyclerAdapter,MyApplication.getContext(),stampAnimationView);
                loadAsyncTask.setOnStampSealListener(this);
                loadAsyncTask.execute();
                break;
            default:
                return false;
        }
        return false;
    }

    private void sort_load_before_check() {
        if(setting_flag == 0){
            //처음 화면 세팅작업중에는 토스트 안띄우고 일단은 데이터 띄워주기 위해서 setting_flag 값에 따라 분기시킴
            if(currentLocation == null) {
                //gps 안켜지거나 못잡으면 0번은 안됨 기본 이름으로
                sort_mode = 1;
                LoadAsyncTask loadAsyncTask = new LoadAsyncTask(firstline_text_view,secondline_cnt_text_view,
                        secondline_nextcnt_text_view,sort_mode_textview, UserTownInfo_arr,
                        sort_mode,currentLocation,mainRecyclerAdapter,MyApplication.getContext(),stampAnimationView);
                loadAsyncTask.setOnStampSealListener(this);
                loadAsyncTask.execute();
            }else{
                sort_mode = 0;
                LoadAsyncTask loadAsyncTask = new LoadAsyncTask(firstline_text_view, secondline_cnt_text_view,
                        secondline_nextcnt_text_view, sort_mode_textview,
                        UserTownInfo_arr, sort_mode,currentLocation,
                        mainRecyclerAdapter,MyApplication.getContext(),stampAnimationView);
                loadAsyncTask.setOnStampSealListener(this);
                loadAsyncTask.execute();
            }
            setting_flag=1;
        }else{
            if(currentLocation == null){
                LoadAsyncTask loadAsyncTask = new LoadAsyncTask(firstline_text_view, secondline_cnt_text_view,
                        secondline_nextcnt_text_view, sort_mode_textview, UserTownInfo_arr,
                        sort_mode,currentLocation,mainRecyclerAdapter,MyApplication.getContext(),stampAnimationView);
                loadAsyncTask.setOnStampSealListener(this);
                loadAsyncTask.execute();

            }else{
                sort_mode = 0;
                LoadAsyncTask loadAsyncTask = new LoadAsyncTask(firstline_text_view, secondline_cnt_text_view,
                        secondline_nextcnt_text_view, sort_mode_textview,
                        UserTownInfo_arr, sort_mode,currentLocation,
                        mainRecyclerAdapter,MyApplication.getContext(),stampAnimationView);
                loadAsyncTask.setOnStampSealListener(this);
                loadAsyncTask.execute();
            }
        }

    }




    @Override
    public void OnRecivedChangeList(ListChangeEvent event) {
        if(event.isChange_status()){
            Log.e(TAG,"LIST CHANGED");
            sort_load_before_check();
        }
        else {
            return;
        }
    }

    @Override
    public void OnStampASeal(TownDTO dto) {

        Log.e("OnStampASeal","position = "+dto.getNo());
        TownDTO data = dto;
        if(currentLocation==null)return;
        String req_stamp_check = getString(R.string.req_url_stamp_check);
        RequestParams params = new RequestParams();
        params.put(ResponseKey.NICK.getKey(),nick);
        params.put(ResponseKey.TOKEN.getKey(),accesstoken);
        params.put("town_code",data.getNo());
        params.put("latitude",currentLocation.getLocation().getLatitude());
        params.put("longitude",currentLocation.getLocation().getLongitude());
        StampRestClient.post(req_stamp_check,params,new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                String code = null;
                String msg = null;

                try {
                    code = response.getString(ResponseKey.CODE.getKey());
                    msg = response.getString(ResponseKey.MESSAGE.getKey());
                    if(code.equals(ResponseCode.SUCCESS.getCode())&&msg.equals(ResponseMsg.SUCCESS.getMessage())){
                        JSONObject resultData = response.getJSONObject(ResponseKey.RESULTDATA.getKey());
                        int res_town_code = resultData.getInt("TOWN_CODE");
                        if(res_town_code == -1){
                            Toast.makeText(getActivity(), R.string.already_taken_string,Toast.LENGTH_LONG).show();
                            return;
                        }
                        String res_nick = resultData.getString("Nick");
                        String res_time = resultData.getString("CheckTime");

                        Log.e("STAMP_CHECK_REQ","nick : "+res_nick+"|town : "+res_town_code+"|time : "+res_time);
                        request_TownUserInfo();
                        sort_load_before_check();

                    }else{
                        Log.e(TAG,code+":"+msg);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {

            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();
        Log.e(TAG,"onResume");
        ((MainActivity)getActivity()).setParentLocationListener(this);
        ((MainActivity)getActivity()).setParentGpsStateListener(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.e(TAG,"onStart");
        ((MainActivity)getActivity()).setParentLocationListener(this);
        ((MainActivity)getActivity()).setParentGpsStateListener(this);
    }

    @Override
    public void onDestroy() {
        if(stamp_on) {
            Log.e("stamp_checked","스탬프찍음");
        }else {
            Log.e("stamp_not_checked","스탬프안찍음");
            preferenceManager.setAgoIsStampOn("EMPTY");
        }
        super.onDestroy();
    }

    @Override
    public void onDestroyView() {
        if(stamp_on) {
            Log.e("stamp_checked","스탬프찍음");
        }else{
            Log.e("stamp_not_checked","스탬프안찍음");
            preferenceManager.setAgoIsStampOn("EMPTY");
        }
        super.onDestroyView();
    }
}