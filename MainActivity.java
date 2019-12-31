package com.app.map.demo;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.amap.api.maps.AMap;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.GeocodeAddress;
import com.amap.api.services.geocoder.GeocodeQuery;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.amap.api.services.route.BusPath;
import com.amap.api.services.route.BusRouteResult;
import com.amap.api.services.route.DrivePath;
import com.amap.api.services.route.DriveRouteResult;
import com.amap.api.services.route.RideRouteResult;
import com.amap.api.services.route.RouteSearch;
import com.amap.api.services.route.WalkPath;
import com.amap.api.services.route.WalkRouteResult;
import com.app.map.demo.overlay.BusRouteOverlay;
import com.app.map.demo.overlay.DrivingRouteOverlay;
import com.app.map.demo.overlay.WalkRouteOverlay;
import com.app.map.demo.utils.AMapUtil;
import com.app.map.demo.utils.ToastUtil;

import butterknife.ButterKnife;


public class MainActivity extends Activity implements RouteSearch.OnRouteSearchListener, GeocodeSearch.OnGeocodeSearchListener, View.OnClickListener {


    MapView mMapView;
    TextView tv_walk, tv_drive, tv_bus, tvMessage;
    EditText edtStart, edtEnd;
    Button btSearch;

    AMap aMap = null;
    RouteSearch mRouteSearch;
    GeocodeSearch geocodeSearch;


    private ProgressDialog progDialog = null;// 搜索时进度条

    private LatLonPoint mStartPoint;//起点，116.335891,39.942295
    private LatLonPoint mEndPoint;//终点，116.481288,39.995576

    private int ROUTE_TYPE = 0;

    private final int ROUTE_TYPE_WALK = 0; //步行
    private final int ROUTE_TYPE_DRIVING = 1; //驾车
    private final int ROUTE_TYPE_BUS = 2;//公交

    private String adcode;
    private WalkRouteResult mWalkRouteResult;
    private DriveRouteResult mDriveRouteResult;
    private BusRouteResult mBusRouteResult;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        //获取地图控件引用
        mMapView = findViewById(R.id.map);
        //在activity执行onCreate时执行mMapView.onCreate(savedInstanceState)，创建地图
        mMapView.onCreate(savedInstanceState);

        init();

    }

    /**
     * 初始化AMap对象
     */
    private void init() {

        mMapView = findViewById(R.id.map);
        tv_walk = findViewById(R.id.tv_walk);
        tv_drive = findViewById(R.id.tv_drive);
        tv_bus = findViewById(R.id.tv_bus);
        tvMessage = findViewById(R.id.tv_message);
        edtStart = findViewById(R.id.edt_start);
        edtEnd = findViewById(R.id.edt_end);
        btSearch = findViewById(R.id.bt_search);

        if (aMap == null) {
            aMap = mMapView.getMap();
        }
        mRouteSearch = new RouteSearch(this);
        mRouteSearch.setRouteSearchListener(this);
        tv_walk.setOnClickListener(this);
        tv_drive.setOnClickListener(this);
        tv_bus.setOnClickListener(this);
        btSearch.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_walk:
                if (ROUTE_TYPE != ROUTE_TYPE_WALK) {
                    ROUTE_TYPE = ROUTE_TYPE_WALK;
                    selectPosition(0);
                    Search();
                }
                break;

            case R.id.tv_drive:
                if (ROUTE_TYPE != ROUTE_TYPE_DRIVING) {
                    ROUTE_TYPE = ROUTE_TYPE_DRIVING;
                    selectPosition(1);
                    Search();
                }
                break;

            case R.id.tv_bus:
                if (ROUTE_TYPE != ROUTE_TYPE_BUS) {
                    ROUTE_TYPE = ROUTE_TYPE_BUS;
                    selectPosition(2);
                    Search();
                }
                break;

            case R.id.bt_search:
                //搜索
                Search();
                break;

        }
    }

    private void selectPosition(int position) {
        if (position == 0) {
            tv_walk.setTextColor(getResources().getColor(R.color.colorWhite));
            tv_walk.setBackgroundColor(getResources().getColor(R.color.colorBlue));
            tv_drive.setTextColor(getResources().getColor(R.color.colorBlack));
            tv_drive.setBackgroundColor(getResources().getColor(R.color.colorWhite));
            tv_bus.setTextColor(getResources().getColor(R.color.colorBlack));
            tv_bus.setBackgroundColor(getResources().getColor(R.color.colorWhite));
        } else if (position == 1) {
            tv_walk.setTextColor(getResources().getColor(R.color.colorBlack));
            tv_walk.setBackgroundColor(getResources().getColor(R.color.colorWhite));
            tv_drive.setTextColor(getResources().getColor(R.color.colorWhite));
            tv_drive.setBackgroundColor(getResources().getColor(R.color.colorBlue));
            tv_bus.setTextColor(getResources().getColor(R.color.colorBlack));
            tv_bus.setBackgroundColor(getResources().getColor(R.color.colorWhite));
        } else if (position == 2) {
            tv_walk.setTextColor(getResources().getColor(R.color.colorBlack));
            tv_walk.setBackgroundColor(getResources().getColor(R.color.colorWhite));
            tv_drive.setTextColor(getResources().getColor(R.color.colorBlack));
            tv_drive.setBackgroundColor(getResources().getColor(R.color.colorWhite));
            tv_bus.setTextColor(getResources().getColor(R.color.colorWhite));
            tv_bus.setBackgroundColor(getResources().getColor(R.color.colorBlue));
        }

    }

    //搜索路线
    private void Search() {
        if (TextUtils.isEmpty(edtStart.getEditableText().toString())) {
            ToastUtil.showToast(MainActivity.this, "请输入起点");
            return;
        }

        if (TextUtils.isEmpty(edtEnd.getEditableText().toString())) {
            ToastUtil.showToast(MainActivity.this, "请输入终点");
            return;
        }

        GeocodeSearchStart(edtStart.getEditableText().toString(), edtEnd.getEditableText().toString());
    }


    //起点经纬度
    public void GeocodeSearchStart(String city_start, String city_end) {
        //构造 GeocodeSearch 对象，并设置监听。
        geocodeSearch = new GeocodeSearch(this);
        geocodeSearch.setOnGeocodeSearchListener(new GeocodeSearch.OnGeocodeSearchListener() {
            @Override
            public void onRegeocodeSearched(RegeocodeResult regeocodeResult, int i) {

            }

            @Override
            public void onGeocodeSearched(GeocodeResult geocodeResult, int i) {
                if (i == AMapException.CODE_AMAP_SUCCESS) {
                    if (geocodeResult != null && geocodeResult.getGeocodeAddressList() != null
                            && geocodeResult.getGeocodeAddressList().size() > 0) {
                        GeocodeAddress address = geocodeResult.getGeocodeAddressList().get(0);
                        String addressName = "经纬度值:" + address.getLatLonPoint() + "  位置描述:" + address.getFormatAddress();
                        //获取到的经纬度
                        LatLonPoint latLongPoint = address.getLatLonPoint();
                        float Lat = (float) latLongPoint.getLatitude();
                        float Lon = (float) latLongPoint.getLongitude();
                        Log.d("-------起addressName----", addressName);
                        Log.d("-------Lat-----", Lat + "");
                        Log.d("-------Lon-----", Lon + "");

                        adcode = address.getAdcode(); //区号

                        mStartPoint = new LatLonPoint(Lat, Lon);//起点，116.335891,39.942295

                        GeocodeSearchEnd(city_end);
                    }
                }
            }
        });
        //address表示地址，第二个参数表示查询城市，中文或者中文全拼，citycode、adcode都ok
        GeocodeQuery query = new GeocodeQuery(city_start, city_start);
        geocodeSearch.getFromLocationNameAsyn(query);
    }


    //终点经纬度
    public void GeocodeSearchEnd(String city_end) {
        //构造 GeocodeSearch 对象，并设置监听。
        geocodeSearch = new GeocodeSearch(this);
        geocodeSearch.setOnGeocodeSearchListener(new GeocodeSearch.OnGeocodeSearchListener() {
            @Override
            public void onRegeocodeSearched(RegeocodeResult regeocodeResult, int i) {

            }

            @Override
            public void onGeocodeSearched(GeocodeResult geocodeResult, int i) {
                if (i == AMapException.CODE_AMAP_SUCCESS) {
                    if (geocodeResult != null && geocodeResult.getGeocodeAddressList() != null
                            && geocodeResult.getGeocodeAddressList().size() > 0) {
                        GeocodeAddress address = geocodeResult.getGeocodeAddressList().get(0);
                        String addressName = "经纬度值:" + address.getLatLonPoint() + "  位置描述:" + address.getFormatAddress();
                        //获取到的经纬度
                        LatLonPoint latLongPoint = address.getLatLonPoint();
                        float Lat = (float) latLongPoint.getLatitude();
                        float Lon = (float) latLongPoint.getLongitude();

                        Log.d("-------Lat-----", Lat + "");
                        Log.d("-------Lon-----", Lon + "");
//                        adcode = address.getAdcode(); //区号
                        Log.d("-------终addressName----", addressName);
                        mEndPoint = new LatLonPoint(Lat, Lon);//终点，116.481288,39.995576

                        searchRouteResult(ROUTE_TYPE);
                        setfromandtoMarker();
                    }
                }
            }
        });
        //address表示地址，第二个参数表示查询城市，中文或者中文全拼，citycode、adcode都ok
        GeocodeQuery query = new GeocodeQuery(city_end, city_end);
        geocodeSearch.getFromLocationNameAsyn(query);
    }


    private void setfromandtoMarker() {
        aMap.addMarker(new MarkerOptions()
                .position(AMapUtil.convertToLatLng(mStartPoint))
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.start)));
        aMap.addMarker(new MarkerOptions()
                .position(AMapUtil.convertToLatLng(mEndPoint))
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.end)));
    }

    /**
     * 开始搜索路径规划方案
     */
    public void searchRouteResult(int routeType) {
        if (mStartPoint == null) {
            ToastUtil.showToast(this, "定位中，稍后再试...");
            return;
        }
        if (mEndPoint == null) {
            ToastUtil.showToast(this, "终点未设置");
        }
        showProgressDialog();
        RouteSearch.FromAndTo fromAndTo = new RouteSearch.FromAndTo(mStartPoint, mEndPoint);
        if (routeType == ROUTE_TYPE_WALK) {// 步行路径规划
            RouteSearch.WalkRouteQuery query = new RouteSearch.WalkRouteQuery(fromAndTo, RouteSearch.WalkDefault);
            mRouteSearch.calculateWalkRouteAsyn(query);// 异步路径规划步行模式查询
        } else if (routeType == ROUTE_TYPE_DRIVING) {// 驾车
            RouteSearch.DriveRouteQuery query = new RouteSearch.DriveRouteQuery(fromAndTo, RouteSearch.DRIVEING_PLAN_DEFAULT, null, null, "");
            mRouteSearch.calculateDriveRouteAsyn(query);// 异步路径规划步行模式查询
        } else if (routeType == ROUTE_TYPE_BUS) {// 公交
            RouteSearch.BusRouteQuery query = new RouteSearch.BusRouteQuery(fromAndTo, RouteSearch.BUS_DEFAULT, adcode, 1);
            mRouteSearch.calculateBusRouteAsyn(query);// 异步路径规划步行模式查询
        }
    }


    /**
     * 显示进度框
     */
    private void showProgressDialog() {
        if (progDialog == null)
            progDialog = new ProgressDialog(this);
        progDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progDialog.setIndeterminate(false);
        progDialog.setCancelable(true);
        progDialog.setMessage("正在搜索");
        progDialog.show();
    }

    /**
     * 隐藏进度框
     */
    private void dissmissProgressDialog() {
        if (progDialog != null) {
            progDialog.dismiss();
        }
    }


    //公交的路线规划
    @Override
    public void onBusRouteSearched(BusRouteResult result, int errorCode) {
        dissmissProgressDialog();
        aMap.clear();// 清理地图上的所有覆盖物
        if (errorCode == AMapException.CODE_AMAP_SUCCESS) {
            if (result != null && result.getPaths() != null) {
                if (result.getPaths().size() > 0) {
                    mBusRouteResult = result;
                    final BusPath walkPath = mBusRouteResult.getPaths()
                            .get(0);
                    BusRouteOverlay busRouteOverlay = new BusRouteOverlay(
                            this, aMap, walkPath,
                            mBusRouteResult.getStartPos(),
                            mBusRouteResult.getTargetPos());
                    busRouteOverlay.removeFromMap();
                    busRouteOverlay.addToMap();
                    busRouteOverlay.zoomToSpan();
                    int dis = (int) walkPath.getDistance();
                    int dur = (int) walkPath.getDuration();
                    String des = AMapUtil.getFriendlyTime(dur) + "(" + AMapUtil.getFriendlyLength(dis) + ")";
                    tvMessage.setText(des);

                } else if (result != null && result.getPaths() == null) {
                    ToastUtil.showToast(this, "没有结果");
                }
            } else {
                ToastUtil.showToast(this, "没有结果");
            }
        } else if (errorCode == AMapException.CODE_AMAP_OVER_DIRECTION_RANGE) {
            ToastUtil.showToast(this.getApplicationContext(), "距离太远啦");
        } else {
            ToastUtil.showToast(this.getApplicationContext(), errorCode + "");
        }

    }

    //自驾的路线
    @Override
    public void onDriveRouteSearched(DriveRouteResult result, int errorCode) {
        dissmissProgressDialog();
        aMap.clear();// 清理地图上的所有覆盖物
        if (errorCode == AMapException.CODE_AMAP_SUCCESS) {
            if (result != null && result.getPaths() != null) {
                if (result.getPaths().size() > 0) {
                    mDriveRouteResult = result;
                    final DrivePath drivePath = mDriveRouteResult.getPaths()
                            .get(0);
                    DrivingRouteOverlay drivingRouteOverlay = new DrivingRouteOverlay(
                            this, aMap, drivePath,
                            mDriveRouteResult.getStartPos(),
                            mDriveRouteResult.getTargetPos(), null);
                    drivingRouteOverlay.removeFromMap();
                    drivingRouteOverlay.addToMap();
                    drivingRouteOverlay.zoomToSpan();
                    int dis = (int) drivePath.getDistance();
                    int dur = (int) drivePath.getDuration();
                    String des = AMapUtil.getFriendlyTime(dur) + "(" + AMapUtil.getFriendlyLength(dis) + ")";
                    tvMessage.setText(des);

                } else if (result != null && result.getPaths() == null) {
                    ToastUtil.showToast(this, "没有结果");
                }
            } else {
                ToastUtil.showToast(this, "没有结果");
            }
        } else if (errorCode == AMapException.CODE_AMAP_OVER_DIRECTION_RANGE) {
            ToastUtil.showToast(this.getApplicationContext(), "距离太远啦");
        } else {
            ToastUtil.showToast(this.getApplicationContext(), errorCode + "");
        }

    }

    //步行路线规划
    @Override
    public void onWalkRouteSearched(WalkRouteResult result, int errorCode) {

        dissmissProgressDialog();
        aMap.clear();// 清理地图上的所有覆盖物
        if (errorCode == AMapException.CODE_AMAP_SUCCESS) {
            if (result != null && result.getPaths() != null) {
                if (result.getPaths().size() > 0) {
                    mWalkRouteResult = result;
                    final WalkPath walkPath = mWalkRouteResult.getPaths()
                            .get(0);
                    WalkRouteOverlay walkRouteOverlay = new WalkRouteOverlay(
                            this, aMap, walkPath,
                            mWalkRouteResult.getStartPos(),
                            mWalkRouteResult.getTargetPos());
                    walkRouteOverlay.removeFromMap();
                    walkRouteOverlay.addToMap();
                    walkRouteOverlay.zoomToSpan();
                    int dis = (int) walkPath.getDistance();
                    int dur = (int) walkPath.getDuration();
                    String des = AMapUtil.getFriendlyTime(dur) + "(" + AMapUtil.getFriendlyLength(dis) + ")";
                    tvMessage.setText(des);

                } else if (result != null && result.getPaths() == null) {
                    ToastUtil.showToast(this, "没有结果");
                }
            } else {
                ToastUtil.showToast(this, "没有结果");
            }
        } else if (errorCode == AMapException.CODE_AMAP_OVER_DIRECTION_RANGE) {
            ToastUtil.showToast(this.getApplicationContext(), "距离太远啦");
        } else {
            ToastUtil.showToast(this.getApplicationContext(), errorCode + "");
        }

    }


    @Override
    public void onRideRouteSearched(RideRouteResult rideRouteResult, int i) {

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，销毁地图
        mMapView.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView.onResume ()，重新绘制加载地图
        mMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView.onPause ()，暂停地图的绘制
        mMapView.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //在activity执行onSaveInstanceState时执行mMapView.onSaveInstanceState (outState)，保存地图当前的状态
        mMapView.onSaveInstanceState(outState);
    }


    @Override
    public void onRegeocodeSearched(RegeocodeResult regeocodeResult, int i) {

    }

    @Override
    public void onGeocodeSearched(GeocodeResult geocodeResult, int i) {

    }

}
