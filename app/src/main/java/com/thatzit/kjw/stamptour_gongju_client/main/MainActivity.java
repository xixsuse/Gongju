package com.thatzit.kjw.stamptour_gongju_client.main;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.thatzit.kjw.stamptour_gongju_client.R;
import com.thatzit.kjw.stamptour_gongju_client.checker.LocaleChecker;
import com.thatzit.kjw.stamptour_gongju_client.http.ResponseCode;
import com.thatzit.kjw.stamptour_gongju_client.http.ResponseKey;
import com.thatzit.kjw.stamptour_gongju_client.http.ResponseMsg;
import com.thatzit.kjw.stamptour_gongju_client.http.StampRestClient;
import com.thatzit.kjw.stamptour_gongju_client.main.adapter.MainPageAdapter;
import com.thatzit.kjw.stamptour_gongju_client.main.event.ListChangeEvent;
import com.thatzit.kjw.stamptour_gongju_client.main.fileReader.PreLoadAsyncTask;
import com.thatzit.kjw.stamptour_gongju_client.main.msgListener.ListChangeListener;
import com.thatzit.kjw.stamptour_gongju_client.main.msgListener.MapViewGpsStateListener;
import com.thatzit.kjw.stamptour_gongju_client.main.msgListener.MapViewLocationListener;
import com.thatzit.kjw.stamptour_gongju_client.main.msgListener.ParentGpsStateListener;
import com.thatzit.kjw.stamptour_gongju_client.main.msgListener.ParentLocationListener;
import com.thatzit.kjw.stamptour_gongju_client.preference.LoggedInInfo;
import com.thatzit.kjw.stamptour_gongju_client.preference.PreferenceManager;
import com.thatzit.kjw.stamptour_gongju_client.push.service.GpsService;
import com.thatzit.kjw.stamptour_gongju_client.push.service.event.GpsStateEvent;
import com.thatzit.kjw.stamptour_gongju_client.push.service.msgListener.GpsStateEventListener;
import com.thatzit.kjw.stamptour_gongju_client.push.service.event.LocationEvent;
import com.thatzit.kjw.stamptour_gongju_client.push.service.msgListener.LocationEventListener;
import com.thatzit.kjw.stamptour_gongju_client.push.service.msgListener.PushMessageChangeListener;
import com.thatzit.kjw.stamptour_gongju_client.push.service.msgListener.PushMessageEvent;
import com.thatzit.kjw.stamptour_gongju_client.push.service.GpsService.MyLocalBinder;
import com.thatzit.kjw.stamptour_gongju_client.util.MyApplication;
import com.thatzit.kjw.stamptour_gongju_client.util.ProgressWaitDaialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

public class MainActivity extends AppCompatActivity implements PushMessageChangeListener, LocationEventListener, GpsStateEventListener {
    private PreferenceManager preferenceManager;
    private boolean mBound;
    private AppCompatActivity self;
    private GpsService mService;
    private Context myapplication= MyApplication.getContext();
    private static final String TAG = "MainActivity";
    private ParentLocationListener parentLocationListener;
    private ParentGpsStateListener parentGpsStateListener;

    private MapViewGpsStateListener mapViewGpsStateListener;
    private MapViewLocationListener mapViewLocationListener;


    private LoggedInInfo user;
    private ProgressWaitDaialog progressWaitDaialog;
    public static ArrayList<TempTownDTO> UserTownInfo_arr;
    private boolean req_flag;
    private ViewPager viewPager;
    private MainPageAdapter adapter;
    private TabLayout tabLayout;
    private ListChangeListener listener;

    private LocaleChecker localeChecker;
    private boolean TopFlag;

    private final int USERINFOCHANGED = 1001;
    private static final int HIDELISTCHANGED = 7778;
    private static final int HIDELISTUNCHANGED = 7779;
    private boolean gpsDialogRender;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TopFlag = true;
        gpsDialogRender = true;
        localeChecker = new LocaleChecker(this);
        self = this;
        try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    "com.thatzit.kjw.stamptour_gongju_client",
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.d("KeyHash NameNotFound:",e.toString());

        } catch (NoSuchAlgorithmException e) {
            Log.d("KeyHash Nosuch :",e.toString());
        }
        tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        setTabIcon(tabLayout);
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        tabLayout.setSelectedTabIndicatorHeight(0);
        tabLayout.setLayoutDirection(TabLayout.LAYOUT_DIRECTION_INHERIT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            tabLayout.setTabTextColors(getColor(R.color.cardview_dark_background),getColor(R.color.com_facebook_blue));
        }else{
            tabLayout.setTabTextColors(getResources().getColor(R.color.cardview_dark_background),getResources().getColor(R.color.com_facebook_blue));
        }

        preferenceManager = new PreferenceManager(this);
        //Fragment에서 사용할 데이터 서버쪽에서 받을 데이터들 로딩후 페이지셋팅
        new PreLoadAsyncTask(this).execute();
        progressWaitDaialog = new ProgressWaitDaialog(this);
        request_TownUserInfo();
//        pushRequest();

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
                            default: array.add(new TempTownDTO(town.getString("TOWN_CODE"),town.getString("Nick"),town.getString("CheckTime"),town.getString("region_en"),town.getString("rank_no")));
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
                        viewPager = (ViewPager) findViewById(R.id.pager);
                        adapter = new MainPageAdapter
                                (getSupportFragmentManager(), tabLayout.getTabCount());
                        viewPager.setAdapter(adapter);
                        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
                        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                            @Override
                            public void onTabSelected(TabLayout.Tab tab) {
                                viewPager.setCurrentItem(tab.getPosition());
                            }

                            @Override
                            public void onTabUnselected(TabLayout.Tab tab) {

                            }

                            @Override
                            public void onTabReselected(TabLayout.Tab tab) {

                            }
                        });
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
        });
    }
    private void setTabIcon(TabLayout tabLayout) {
        View view1 = getLayoutInflater().inflate(R.layout.tabiconview, null);
        view1.findViewById(R.id.icon).setBackgroundResource(R.drawable.btn_tabs_stamp);
        tabLayout.addTab(tabLayout.newTab().setCustomView(view1));

        View view2 = getLayoutInflater().inflate(R.layout.tabiconview, null);
        view2.findViewById(R.id.icon).setBackgroundResource(R.drawable.btn_tabs_map);
        tabLayout.addTab(tabLayout.newTab().setCustomView(view2));

        View view3 = getLayoutInflater().inflate(R.layout.tabiconview, null);
        view3.findViewById(R.id.icon).setBackgroundResource(R.drawable.btn_tabs_ranking);
        tabLayout.addTab(tabLayout.newTab().setCustomView(view3));

        View view4 = getLayoutInflater().inflate(R.layout.tabiconview, null);
        view4.findViewById(R.id.icon).setBackgroundResource(R.drawable.btn_tabs_more);
        tabLayout.addTab(tabLayout.newTab().setCustomView(view4));
    }


    private void pushRequest() {
        LoggedInInfo loggedIn_Info = preferenceManager.getLoggedIn_Info();
        RequestParams params = new RequestParams();
        params.put(ResponseKey.TOKEN.getKey(),loggedIn_Info.getAccesstoken());
        params.put(ResponseKey.NICK.getKey(),loggedIn_Info.getNick());
        params.put(ResponseKey.DEVICETOKEN.getKey(),preferenceManager.getGCMaccesstoken());
        StampRestClient.post(getResources().getString(R.string.req_url_push_test), params, new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                Log.e("pushRequest",response+"");
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                Log.e("pushRequest",errorResponse+"");
            }
        });
    }



    @Override
    public void OnReceived(PushMessageEvent event) {

    }


    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            // TODO Auto-generated method stub
            mBound = false;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // TODO Auto-generated method stub
            Log.d("servicebinding", TAG);
            MyLocalBinder binder = (MyLocalBinder)service;
            mService = binder.getService();
            if(mService==null)Log.d(TAG, "null");
            else Log.d(TAG, "not null");
            mBound = true;
            mService.setOnTopActivity(self);
            mService.setOnLocationEventListener(MainActivity.this);
            mService.setOnGpsStateEventListener(MainActivity.this);
        }
    };

    @Override
    public void OnReceivedEvent(LocationEvent event) {
        Log.e(TAG+"Re",event.getLocation().getLatitude()+":"+event.getLocation().getLongitude());
        if(UserTownInfo_arr != null){

        }
        if(parentLocationListener != null){
            Log.e(TAG,"parentLocationListener is Notnull");
            parentLocationListener.OnReceivedLocation(event);
        }else{
            Log.e(TAG,"parentLocationListener is null");
        }
        if(mapViewLocationListener != null){
            Log.e(TAG,"mapViewLocationListener is Notnull");
            mapViewLocationListener.OnReceivedLocation(event);
        }else{
            Log.e(TAG,"mapViewLocationListener is null");
        }
    }

    @Override
    public void OnReceivedStateEvent(GpsStateEvent event) {
        Log.e(TAG+"Re",event.isState()+"");
        if(!event.isState()){
            if(gpsDialogRender){
                ShowGpsDialog();
                gpsDialogRender=false;
            }
        }
        if(UserTownInfo_arr != null){

        }
        if(parentGpsStateListener != null){
            Log.e(TAG,"parentGpsStateListener is Notnull");

            parentGpsStateListener.OnReceivedParentStateEvent(event);
        }else{
            Log.e(TAG,"parentGpsStateListener is null");
        }

        if(mapViewGpsStateListener != null){
            Log.e(TAG,"mapViewGpsStateListener is Notnull");

            mapViewGpsStateListener.OnReceivedParentStateEvent(event);
        }else{
            Log.e(TAG,"mapViewGpsStateListener is Notnull");
        }
    }
    @Override
    protected void onStart() {
        // TODO Auto-generated method stub
        super.onStart();
        bindService(new Intent(this, GpsService.class), mConnection, Context.BIND_AUTO_CREATE);
        TopFlag = true;

    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();

    }

    @Override
    protected void onStop() {
        // TODO Auto-generated method stub
        super.onStop();
        TopFlag = false;
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        TopFlag = false;
        if(mBound == true && mService != null)
        {
            mService.setOnGpsStateEventListener(null);
            mService.setOnLocationEventListener(null);
            mService.setOnTopActivity(null);
            Log.d(TAG, "unbindService");
            unbindService(mConnection);
        }
    }

    public boolean isTopFlag() {
        return TopFlag;
    }
    private void ShowGpsDialog() {
        AlertDialog.Builder gsDialog = new AlertDialog.Builder(this);
        gsDialog.setTitle("위치 서비스 설정");
        gsDialog.setMessage("무선 네트워크 사용, GPS 위성 사용을 모두 체크하셔야 정확한 위치 서비스가 가능합니다.\n위치 서비스 기능을 설정하시겠습니까?");
        gsDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // GPS설정 화면으로 이동
                Intent intent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                intent.addCategory(Intent.CATEGORY_DEFAULT);
                startActivity(intent);
            }
        }).setNegativeButton("NO", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                return;
            }
        }).create().show();
    }
    @Override
    public void onBackPressed() {
        this.finish();
    }

    public void setParentLocationListener(ParentLocationListener listener){
        this.parentLocationListener = listener;
    }
    public void setParentGpsStateListener(ParentGpsStateListener listener){
        this.parentGpsStateListener = listener;
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.e(TAG,"RequestCode : "+requestCode+"\nResultCode : "+resultCode);
        if(resultCode == USERINFOCHANGED)
        {
            Log.e(TAG,"USERINFOCHANGED");
            finish();
        }else if(resultCode == HIDELISTCHANGED){
            if(listener!=null)listener.OnRecivedChangeList(new ListChangeEvent(true));
        }else if(resultCode == HIDELISTUNCHANGED){
            if(listener!=null)listener.OnRecivedChangeList(new ListChangeEvent(false));
        }
    }
    public void setOnListChangeListener(ListChangeListener listener){
        this.listener = listener;
    }

    public void setMapViewGpsStateListener(MapViewGpsStateListener mapViewGpsStateListener) {
        this.mapViewGpsStateListener = mapViewGpsStateListener;
    }

    public void setMapViewLocationListener(MapViewLocationListener mapViewLocationListener) {
        this.mapViewLocationListener = mapViewLocationListener;
    }

}
