package com.example.mask_2;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PointF;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.CameraPosition;
import com.naver.maps.map.MapFragment;

import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.UiSettings;
import com.naver.maps.map.overlay.InfoWindow;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.overlay.MultipartPathOverlay;
import com.naver.maps.map.overlay.Overlay;
import com.naver.maps.map.overlay.OverlayImage;
import com.naver.maps.map.overlay.PathOverlay;
import com.naver.maps.map.util.FusedLocationSource;
import com.naver.maps.map.util.MarkerIcons;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.Inflater;


public class NaverMapActivity extends AppCompatActivity implements NaverMap.OnMapClickListener, Overlay.OnClickListener, OnMapReadyCallback, NaverMap.OnCameraChangeListener, NaverMap.OnCameraIdleListener {

    private static final int ACCESS_LOCATION_PERMISSION_REQUEST_CODE = 100;
    private FusedLocationSource locationSource;
    private NaverMap naverMap;
    private InfoWindow infoWindow;
    TextView name;
    TextView stock;
    TextView time;

    Button root;
    FrameLayout frame;
    View view;


    private List<Marker> markerList = new ArrayList<Marker>();
    private boolean isCameraAnimated = false;
    public CommonFunction fc;



    private LinearLayout linear_root;
    private View mainLayout;

    private List<LatLng> lstLatLng;
    private static PathOverlay path;
    private MultipartPathOverlay multipartPath;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_naver_map);

        mainLayout = findViewById(R.id.main_layout);
        MapFragment mapFragment = (MapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        mapFragment.getMapAsync(this); //비동기적으로 지도 데이터를 불러오기

        frame = (FrameLayout)findViewById(R.id.frame);

        //xml 파일 객체화 준비
        LayoutInflater in = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
        view = in.inflate(R.layout.view_go, null);




    }





    @Override
    public void onMapReady(@NonNull final NaverMap naverMap) {
        this.naverMap = naverMap;

        locationSource = new FusedLocationSource(this, ACCESS_LOCATION_PERMISSION_REQUEST_CODE);
        naverMap.setLocationSource(locationSource);
        UiSettings uiSettings = naverMap.getUiSettings();
        uiSettings.setLocationButtonEnabled(true);

        naverMap.addOnCameraChangeListener(this);
        naverMap.addOnCameraIdleListener(this); //지도를 움직이고 멈췄을때 알려줌
        naverMap.setOnMapClickListener(this); //지도 어딘가 클릭했을때 호출

        LatLng mapCenter = naverMap.getCameraPosition().target; //중심점에 대한 위치
        fetchStoreSale(mapCenter.latitude, mapCenter.longitude, 5000); //위도 경도 중심으로 반경 5000미터 이내에 있는 판매처 조회


        infoWindow = new InfoWindow(); //객체 만들기, 마커를 클릭했을 때 보여주기-listener만들기
        infoWindow.setAdapter(new InfoWindow.DefaultViewAdapter(this) {
            @NonNull
            @Override    //textAdapter(간단한 텍스트), viewadapter -약국 이름이 담긴 store 객체를 가져온다.(복잡한 형태의 뷰)
            protected View getContentView(@NonNull InfoWindow infoWindow) {

                Marker marker = infoWindow.getMarker(); //마커를 태깅하고 나서 getMarker()를 하면 연결된 마커를 가지고 올 수 있다.
                Store store = (Store) marker.getTag(); //필요한 store 정보 가져오기
                View view = View.inflate(NaverMapActivity.this, R.layout.view_info_window, null); //xml파일 만들기


                ((TextView) view.findViewById(R.id.name)).setText(store.name); //store이름을 바인딩

                if ("plenty".equalsIgnoreCase(store.remain_stat)) { //재고현황 보여주기

                    ((TextView) view.findViewById(R.id.stock)).setText("100개 이상");

                } else if ("some".equalsIgnoreCase(store.remain_stat)) {

                    ((TextView) view.findViewById(R.id.stock)).setText("30개 이상 100개 미만");

                } else if ("fiew".equalsIgnoreCase(store.remain_stat)) {

                    ((TextView) view.findViewById(R.id.stock)).setText("2개 이상 30개 미만");

                } else if ("empty".equalsIgnoreCase(store.remain_stat)) {

                    ((TextView) view.findViewById(R.id.stock)).setText("1개 이하");

                } else if ("break".equalsIgnoreCase(store.remain_stat)) {

                    ((TextView) view.findViewById(R.id.stock)).setText("판매중지");

                } else {

                    ((TextView) view.findViewById(R.id.stock)).setText(null);

                }

                ((TextView) view.findViewById(R.id.time)).setText("입고 " + store.stock_at);



                return view;



            }
        });


    }





    //현재 위치 접근
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case ACCESS_LOCATION_PERMISSION_REQUEST_CODE:
                locationSource.onRequestPermissionsResult(requestCode, permissions, grantResults);
                return;
        }
    }


    //카메라의 이동 방지
    @Override
    public void onCameraChange(int reason, boolean animated) {
        isCameraAnimated = animated;
    }

    @Override
    public void onCameraIdle() { //지도를 움직이고 멈췄을때 다시 화면에 업데이트
        if(isCameraAnimated){
            LatLng mapCenter = naverMap.getCameraPosition().target;
            fetchStoreSale(mapCenter.latitude, mapCenter.longitude, 5000);
        }
    }

    private void fetchStoreSale(double lat, double lng, int m) {
        //해당 api를 제공하고 있는 도메인, 전달된 json형태를 자바객체로 변화시킴
        Retrofit retrofit = new Retrofit.Builder().baseUrl("https://8oi9s0nnth.apigw.ntruss.com").addConverterFactory(GsonConverterFactory.create()).build();

        MaskApi maskApi = retrofit.create(MaskApi.class);
        maskApi.getStoresByGeo(lat, lng, m).enqueue(new Callback<StoreSaleResult>() { //callback으로 전달받는다.
            @Override
            public void onResponse(Call<StoreSaleResult> call, Response<StoreSaleResult> response) { //호출에 성공했을때
                if (response.code() == 200) {
                    StoreSaleResult result = response.body(); //response.body는 자바 객체
                    updateMapMarkers(result); //지도상에 표시
                }
            }

            @Override
            public void onFailure(Call<StoreSaleResult> call, Throwable t) {

            }
        });
    }


    //지도의 빈 영역을 클릭했을 때 닫는다.
    @Override
    public void onMapClick(@NonNull PointF pointF, @NonNull LatLng latLng) {
        if(infoWindow.getMarker() != null){ //인포윈도우에 마커가 연결되어있으면 닫는다.
            infoWindow.close();
        }
    }


//마커를 클릭했을 때
    @Override
    public boolean onClick(@NonNull Overlay overlay) { //부모클래스로부터 상속받았다(overlay)
        if(overlay instanceof Marker){
            Marker marker = (Marker) overlay;
            final Store store = (Store) marker.getTag(); //필요한 store 정보 가져오기
            LatLng mapCenter = naverMap.getCameraPosition().target; //중심점에 대한 위치
            PathOverlay path = new PathOverlay();

            /*
            //경로 그리기
            path.setCoords(Arrays.asList(
                    new LatLng(37.5392, 127.1461),
                    new LatLng(store.lat, store.lng)
            ));
            path.setMap(naverMap);
*/

            if (view.getParent() != null) {
                ((ViewGroup) view.getParent()).removeView(view);
                //builder.setView(view);
            } else{
                frame.addView(view);
                root = findViewById(R.id.root);

                root.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Marker marker = new Marker();
                        fc.getInstance();

                        Intent i = new Intent(Intent.ACTION_VIEW,  Uri.parse(fc.getPath(37.5392, 127.1461, store.lat, store.lng).toString()));
                        startActivity(i);

                    }
                });
            }

            if(marker.getInfoWindow() != null){
                infoWindow.close(); //인포윈도우가 표시되었을 때 클릭시 없애기


            } else{
                infoWindow.open(marker); // 마커위에 인포윈도우 표시시


           }


            return true;
        }


        return false;
    }


    private void updateMapMarkers(StoreSaleResult result) {
        resetMarkerList();
        if (result.stores != null && result.stores.size() > 0) { //판매처가 한개 이상이면
            for (Store store : result.stores) { //판매처 개수만큼 돌면서 루트 생성
                Marker marker = new Marker();

                marker.setTag(store); //store 정보를 마커에 객체로 붙인다.
                marker.setPosition(new LatLng(store.lat, store.lng)); //마커 좌표 설정
                if ("plenty".equalsIgnoreCase(store.remain_stat)) {
                    marker.setIcon(OverlayImage.fromResource(R.drawable.marker_green));
                } else if ("some".equalsIgnoreCase(store.remain_stat)) {
                    marker.setIcon(OverlayImage.fromResource(R.drawable.marker_yellow));
                } else if ("flew".equalsIgnoreCase(store.remain_stat)) {
                    marker.setIcon(OverlayImage.fromResource(R.drawable.marker_red));
                } else {
                    marker.setIcon(OverlayImage.fromResource(R.drawable.marker_gray));
                }
                marker.setAnchor(new PointF(0.5f, 1.0f)); //마커의 위치(setPosition에서 설정한 것)와 아이콘 상의 위치를 어디에 둘건지 x축으로 50%, y축으로 100%
                marker.setMap(naverMap); //생성된 마커를 어떤 지도에 표시할 것인지.

                marker.setOnClickListener(this); //마커를 클릭했을 때 인포윈도우의 정보를 보여주기
                markerList.add(marker); //list에 마커 저장
            }
        }
    }

    private void resetMarkerList() { //나중에 마커 삭제 시
        if (markerList != null && markerList.size() > 0) {
            for (Marker marker : markerList) {
                marker.setMap(null); //마커가 표시되어 있는 지도를 null로 표시

            }
            markerList.clear(); // 지도상에 표시했던 마커를 지도상에서 제거
        }
    }


}
