package com.thatzit.kjw.stamptour_kyj_client.main;

/**
 * Created by kjw on 16. 8. 22..
 */

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.thatzit.kjw.stamptour_kyj_client.R;
import com.thatzit.kjw.stamptour_kyj_client.main.fileReader.ReadJson;
import com.thatzit.kjw.stamptour_kyj_client.main.msgListener.MapViewGpsStateListener;
import com.thatzit.kjw.stamptour_kyj_client.main.msgListener.MapViewLocationListener;
import com.thatzit.kjw.stamptour_kyj_client.main.msgListener.ParentGpsStateListener;
import com.thatzit.kjw.stamptour_kyj_client.main.msgListener.ParentLocationListener;
import com.thatzit.kjw.stamptour_kyj_client.push.service.event.GpsStateEvent;
import com.thatzit.kjw.stamptour_kyj_client.push.service.event.LocationEvent;
import com.thatzit.kjw.stamptour_kyj_client.util.ProgressWaitDaialog;

public class MapFragment extends Fragment implements OnMapReadyCallback,MapViewLocationListener,MapViewGpsStateListener{
    MapView mMapView;
    private GoogleMap googleMap;

    private View view;
    private static final LatLng SYDNEY = new LatLng(-33.88,151.21);
    private static final LatLng MOUNTAIN_VIEW = new LatLng(37.4, -122.1);
    private Marker marker;
    private static final String KEY_MAP_SAVED_STATE = "mapState";
    private Toolbar toolbar;
    private Bundle savedInstanceState;
    private LocationEvent locationEvent;
    private boolean turnOff;
    private boolean turnOnGpsShow = true;
    private boolean mapRender = true;
    private ProgressWaitDaialog progressWaitDaialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.map_fragment, container, false);
        this.savedInstanceState = savedInstanceState;
        ((MainActivity)getActivity()).setMapViewGpsStateListener(this);

        return view;
    }

    private void MapSetting(Bundle savedInstanceState) {
        mMapView = (MapView) view.findViewById(R.id.mapView);

        Bundle mapState = (savedInstanceState != null)
                ? savedInstanceState.getBundle(KEY_MAP_SAVED_STATE): null;
        mMapView.onCreate(mapState);
        //mMapView.onCreate(savedInstanceState);
        mMapView.onResume();
        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        mMapView.getMapAsync(this);
    }
    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity)getActivity()).setMapViewGpsStateListener(this);
        if(mMapView!=null)
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        if(mMapView!=null)
        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mMapView!=null)
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if(mMapView!=null)
        mMapView.onLowMemory();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        this.googleMap = googleMap;
        if(getActivity()!=null){
            // For showing a move to my location button
            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                return;
            }
            this.googleMap.setMyLocationEnabled(true);

            // For dropping a marker at a point on the Map
            // 포천 : 위도 - 37.8949148, 경도 - 127.20035510000002
//        LatLng center = new LatLng(37.8949148, 127.20035510000002);
            LatLng center = new LatLng(locationEvent.getLocation().getLatitude(), locationEvent.getLocation().getLongitude());
            Log.e("center",center.longitude+":"+center.latitude);
            for(int i =0 ; i < ReadJson.memCashList.size(); ++i){
                marker= this.googleMap
                        .addMarker(new MarkerOptions()
                                .position(new LatLng(Double.parseDouble(ReadJson.memCashList.get(i).getLat()), Double.parseDouble(ReadJson.memCashList.get(i).getLon())))
                                .title(ReadJson.memCashList.get(i).getName()));
            }
            this.googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(center, 10));

            mapRender = false;
        }else{
            return;
        }
    }

    @Override
    public void OnReceivedLocation(LocationEvent locationEvent) {
        this.locationEvent = locationEvent;
        if(mapRender)
        MapSetting(savedInstanceState);

    }

    @Override
    public void OnReceivedParentStateEvent(GpsStateEvent event) {
        if(event.isState() == false){
            //gps turn off
            if(turnOnGpsShow){
                turnOff = event.isState();
                return;
            }else{
                turnOff = event.isState();
            }

        }else{
            //gps turn on
            turnOff = event.isState();
            if(getActivity()==null){
                Log.e("ERROR","NOT_BIND");
            }else{
                ((MainActivity)getActivity()).setMapViewLocationListener(this);
            }
        }
    }
}