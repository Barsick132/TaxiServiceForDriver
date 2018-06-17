package com.lstu.kovalchuk.taxiservicefordriver;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.google.maps.android.PolyUtil;
import com.lstu.kovalchuk.taxiservicefordriver.mapapi.Route;
import com.lstu.kovalchuk.taxiservicefordriver.mapapi.RouteResponse;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Navigator extends AppCompatActivity implements OnMapReadyCallback {

    // Обработчик завершения инициализации карты
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "onMapReady: map is ready");
        mMap = googleMap;

        // Если есть права доступа к даным местоположения
        if (mLocationPermissionGranted) {
            // Получаем и отображаем текущее местоположение на карте
            getDeviceLocation();

            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                return;
            }

            mMap.setMyLocationEnabled(true); // Делаем доступной кнопку определения местоположения на карте

        }
    }

    public static final String TAG = "Navigator";
    public static final String FINE_LOCATION = android.Manifest.permission.ACCESS_FINE_LOCATION;
    public static final String COARSE_LOCATION = android.Manifest.permission.ACCESS_COARSE_LOCATION;
    public static final String CALL_PHONE = Manifest.permission.CALL_PHONE;
    public static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    public static final int CALL_PERMISSION_REQUEST_CODE = 4321;
    private static final float DEFAULT_ZOOM = 15f;

    private String orderID;
    private Order order = null;
    private DocumentReference docRef;
    private FirebaseFirestore db;
    private DriverGPS driverGPS;
    private Client client;
    private BitmapDescriptor iconWhence, iconWhere;
    private boolean isClientComeOut;
    private boolean routeDrawing;

    private LocationCallback mLocationCallback;
    private LocationRequest mLocationRequest;
    private Boolean mLocationPermissionGranted = false;
    private Boolean mCallingPermissionGranted = false;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private GoogleMap mMap;
    private Location currentLocation;
    private Button btnCancel;
    private Button btnCall;
    private Button btnArrived;
    private Button btnGo;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigator);

        db = FirebaseFirestore.getInstance();
        iconWhence = bitmapDescriptorFromVector(this, R.drawable.ic_place_accent_24dp);
        iconWhere = bitmapDescriptorFromVector(this, R.drawable.ic_place_blue_24dp);

        isClientComeOut = false;
        btnCancel = findViewById(R.id.navBtnCancel);
        btnCall = findViewById(R.id.navBtnCall);
        btnArrived = findViewById(R.id.navBtnDriverArrived);
        btnGo = findViewById(R.id.navBtnGo);
        btnGo.setVisibility(View.GONE);

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }

                currentLocation = locationResult.getLastLocation();
                moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), DEFAULT_ZOOM);

                try {
                    driverGPS = new DriverGPS(new Date(), currentLocation);
                    db.collection("driverGPS").document(FirebaseAuth.getInstance().getUid()).set(driverGPS)
                            .addOnSuccessListener(aVoid -> {
                                Log.d(TAG, "getOrder: координаты переданы");
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "getOrder: " + e.getMessage());
                            });
                } catch (Exception ex) {
                    Log.e(TAG, "getOrder: " + ex.getMessage());
                }
            }
        };

        getCallingPermissionGranted();
        // Получение прав доступа о местоположении, отрисовка карты
        // установка текущего местоположения на карте
        getLocationPermission();

        initToolbar();

        Bundle arguments = getIntent().getExtras();
        // Если есть переданные аргументы
        if (arguments != null) {
            // Получаем эти аргументы как текущие координаты
            orderID = arguments.getString("OrderID");
        }
        if (orderID != null) {
            getOrder();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        docRef.addSnapshotListener(((documentSnapshot, e) -> {
            if (e != null) {
                Toast.makeText(Navigator.this, "Что-то пошло не так :( Проверьте соединение с сетью", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "onEvent: Ошибка при прослушивании БД");
                return;
            }

            try {
                if (documentSnapshot.exists()) {
                    order = documentSnapshot.toObject(Order.class);
                    if (order != null) {
                        if (order.isCancel()) {
                            Toast.makeText(Navigator.this, "Клиент отменил заказ", Toast.LENGTH_SHORT).show();
                            openGlobal();
                        }

                        if (order.getDTend() != null && order.getTotalCost() != null) {
                            // Заказ завершен
                            Intent intent = new Intent(Navigator.this, TotalOrder.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
                        }

                        if (order.isClientCameOut()) {
                            if (!isClientComeOut) {
                                isClientComeOut = true;
                                android.app.Notification notification = new android.app.Notification.Builder(Navigator.this)
                                        .setContentTitle("Клиент выходит")
                                        .setContentText("Клиент сообщил о своем выходе")
                                        .setSmallIcon(R.drawable.ic_logo_tsfd)
                                        .build();
                                android.support.v4.app.NotificationManagerCompat notificationManager = android.support.v4.app.NotificationManagerCompat.from(this);
                                notificationManager.notify(0, notification);
                            }
                        }
                    }
                } else {
                    Log.d(TAG, "onStart: заказ не найден");
                    Toast.makeText(Navigator.this, "Заказ не найден", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception ex) {
                Log.e(TAG, "onStart: " + ex.getMessage());
            }
        }));

    }

    // Функция получения данных о заказе
    private void getOrder() {
        docRef = db.collection("orders").document(orderID);
        docRef.get()
                .addOnSuccessListener(documentSnapshot -> {
                    order = documentSnapshot.toObject(Order.class);
                    if (order != null) {
                        if (!order.isDriverArrived()) {
                            if(currentLocation != null) {
                                String position = currentLocation.getLatitude() + "," + currentLocation.getLongitude();
                                String destination = order.getWhenceGeoPoint().getLatitude() + "," + order.getWhenceGeoPoint().getLongitude();
                                routePrint(position, destination);
                                routeDrawing = true;
                            }
                        } else {
                            btnCancel.setVisibility(View.GONE);
                            btnArrived.setVisibility(View.GONE);
                            btnGo.setVisibility(View.VISIBLE);
                            btnCall.setVisibility(View.VISIBLE);
                            if (order.getDTbegin() != null) {
                                btnCall.setVisibility(View.GONE);
                                btnGo.setVisibility(View.GONE);

                                String position = order.getWhenceGeoPoint().getLatitude() + "," + order.getWhenceGeoPoint().getLongitude();
                                String destination = order.getWhereGeoPoint().getLatitude() + "," + order.getWhereGeoPoint().getLongitude();
                                routePrint(position, destination);
                            }
                        }
                        getClient(order.getClientUID());
                    } else {
                        btnCancel.performClick();
                    }
                })
                .addOnFailureListener(aVoid -> {
                    Toast.makeText(Navigator.this,
                            "Не удалось получить заказ. Проверьте соединение с сетью",
                            Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "getOrder: не удалось проверить статус заказа в БД");
                });
    }

    // Функция получения BitmapDescriptor по id векторного изображения в drawable
    private BitmapDescriptor bitmapDescriptorFromVector(Context context, @DrawableRes int vectorDrawableResourceId) {
        Drawable icon = ContextCompat.getDrawable(context, vectorDrawableResourceId);
        icon.setBounds(0, 0, icon.getIntrinsicWidth(), icon.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(icon.getIntrinsicWidth(), icon.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        icon.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    // Функция для совершения Http запросов
    private static class GetQuery {
        OkHttpClient client = new OkHttpClient();

        void run(String url, Callback callback) {
            Request request = new Request.Builder()
                    .url(url)
                    .build();

            client.newCall(request).enqueue(callback);
        }
    }

    // Функция отображения маршрута на карте
    private void routePrint(String origin, String destination) {
        GetQuery query = new GetQuery();
        String url = "https://maps.googleapis.com/maps/api/directions/json?" +
                "origin=" + origin +
                "&destination=" + destination +
                "&sensor=true" +
                "&language=ru" +
                "&key=" + getResources().getString(R.string.google_maps_webkey);

        query.run(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                try {
                    runOnUiThread(() -> Toast.makeText(Navigator.this,
                            "Не удалось отобразить маршрут", Toast.LENGTH_SHORT).show());
                    Log.d(TAG, "onFailure: не удалось получить маршруты в формате json");
                } catch (Exception ex) {
                    Log.e(TAG, "onFailure: " + ex.getMessage());
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    String jsonString = response.body().string();
                    Gson g = new Gson();
                    RouteResponse routeResponse = g.fromJson(jsonString, RouteResponse.class);

                    if (routeResponse.getStatus().equals("OK")) {
                        Navigator.this.runOnUiThread(() -> {
                            try {
                                Route minTimeRoute = routeResponse.getRoutes().get(0);
                                for (Route route : routeResponse.getRoutes()) {
                                    if (route.getLegs().get(0).getDuration().getValue() < minTimeRoute.getLegs().get(0).getDuration().getValue()) {
                                        minTimeRoute = route;
                                    }
                                }

                                List<LatLng> mPoints = PolyUtil.decode(minTimeRoute.getOverviewPolyline().getPoints());
                                mMap.clear();

                                PolylineOptions line = new PolylineOptions();
                                line.width(6f).color(R.color.colorAccent);
                                LatLngBounds.Builder latLngBuilder = new LatLngBounds.Builder();
                                for (int i = 0; i < mPoints.size(); i++) {
                                    if (i == 0) {
                                        MarkerOptions startMarkerOptions = new MarkerOptions()
                                                .position(mPoints.get(i))
                                                .icon(iconWhence)
                                                .title("A");
                                        mMap.addMarker(startMarkerOptions);
                                    } else if (i == mPoints.size() - 1) {
                                        MarkerOptions endMarkerOptions = new MarkerOptions()
                                                .position(mPoints.get(i))
                                                .icon(iconWhere)
                                                .title("B");
                                        mMap.addMarker(endMarkerOptions);
                                    }
                                    line.add(mPoints.get(i));
                                    latLngBuilder.include(mPoints.get(i));
                                }
                                mMap.addPolyline(line);
                                int size = getResources().getDisplayMetrics().widthPixels;
                                LatLngBounds latLngBounds = latLngBuilder.build();
                                CameraUpdate track = CameraUpdateFactory.newLatLngBounds(latLngBounds, size, size, 25);
                                mMap.moveCamera(track);
                            } catch (Exception ex) {
                                Log.e(TAG, "onResponse: " + ex.getMessage());
                            }
                        });
                    }
                } catch (Exception ex) {
                    Log.d(TAG, "onResponse: ошибка при получении маршрута в формате json");
                    runOnUiThread(() -> Toast.makeText(Navigator.this,
                            "Не удалось отобразить маршрут", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    private void getClient(String clientUID) {
        if (clientUID != null) {
            db.collection("clients").document(clientUID)
                    .get()
                    .addOnSuccessListener(result -> {
                        client = result.toObject(Client.class);
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "getClient: " + e.getMessage());
                        Toast.makeText(Navigator.this, "Не удалось получить сведения о клиенте. Проверьте соединение с сетью", Toast.LENGTH_SHORT).show();
                    });
        } else {
            Log.d(TAG, "getClient: не удалось получить сведения о клиенте");
            Toast.makeText(Navigator.this, "Не удалось получить сведения о клиенте. Проверьте соединение с сетью", Toast.LENGTH_SHORT).show();
        }
    }

    private void getCallingPermissionGranted() {
        Log.d(TAG, "getLocationPermission: получение прав доступа к данным местоположения");
        String[] permission = {CALL_PHONE};

        // Проверка предоставления прав доступа к данным местоположения
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
            // Если права предоставлены инициализируем карту
            mCallingPermissionGranted = true;
        } else {
            // Если права не были предоставлены
            // Вызов диалогового окна для предоставления прав доступа к данным местоположения
            ActivityCompat.requestPermissions(this,
                    permission,
                    CALL_PERMISSION_REQUEST_CODE);
        }
    }

    // Получение прав доступа к данным местоположения
    private void getLocationPermission() {
        Log.d(TAG, "getLocationPermission: получение прав доступа к данным местоположения");
        String[] permission = {FINE_LOCATION, COARSE_LOCATION};

        // Проверка предоставления прав доступа к данным местоположения
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                    COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                // Если права предоставлены инициализируем карту
                mLocationPermissionGranted = true;
                initMap();
            } else {
                // Если права не были предоставлены
                // Вызов диалогового окна для предоставления прав доступа к данным местоположения
                ActivityCompat.requestPermissions(this,
                        permission,
                        LOCATION_PERMISSION_REQUEST_CODE);
            }
        } else {
            // Если права не были предоставлены
            // Вызов диалогового окна для предоставления прав доступа к данным местоположения
            ActivityCompat.requestPermissions(this,
                    permission,
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    // Обработчик окончания вызова диалогового окна предоставления прав доступа
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;

        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    for (int grantResult : grantResults) {
                        if (grantResult != PackageManager.PERMISSION_GRANTED) {
                            // Если хоть какое-то право отсутствует
                            mLocationPermissionGranted = false;
                            return;
                        }
                    }
                    // Если все права были предоставлены
                    mLocationPermissionGranted = true;
                    initMap();
                }
            }
            case CALL_PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    for (int grantResult : grantResults) {
                        if (grantResult != PackageManager.PERMISSION_GRANTED) {
                            // Если хоть какое-то право отсутствует
                            mCallingPermissionGranted = false;
                            return;
                        }
                    }
                    // Если все права были предоставлены
                    mCallingPermissionGranted = true;
                }
            }
        }
    }

    // Инициализация карты
    private void initMap() {
        Log.d(TAG, "initMap: инициализация карты");
        // Отображение фрагмента с картой
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.navMap);
        mapFragment.getMapAsync(Navigator.this);
    }

    // Метод получения данных местоположения для отображения конкретного места на карте при первом запуске
    private void getDeviceLocation() {
        Log.d(TAG, "getDeviceLocation: получение координат текущего местоположения");

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(2000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setSmallestDisplacement(1);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        try {
            // Если есть право на получение данных местоположения
            if (mLocationPermissionGranted) {
                // Пытаемся их получить
                Task location = mFusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Если удалось определить местоположение
                        Log.d(TAG, "onComplete: местоположение определено!");
                        currentLocation = (Location) task.getResult();

                        if(currentLocation==null){
                            Log.d(TAG, "getDeviceLocation: местоположение НЕ определено");
                            Toast.makeText(Navigator.this, "Не удалось определить текущее местоположение", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if(!routeDrawing && order!=null && !order.isDriverArrived())
                        {
                            String position = currentLocation.getLatitude() + "," + currentLocation.getLongitude();
                            String destination = order.getWhenceGeoPoint().getLatitude() + "," + order.getWhenceGeoPoint().getLongitude();
                            routePrint(position, destination);
                            routeDrawing = true;
                        }

                        // Меняем местоположение камеры
                        moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), DEFAULT_ZOOM);
                    } else {
                        // Если НЕ удалось определить местоположение
                        Log.d(TAG, "onComplete: местоположение НЕ определено");
                        Toast.makeText(Navigator.this, "Не удалось определить текущее местоположение", Toast.LENGTH_SHORT).show();
                    }
                });

                mFusedLocationProviderClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null);
            }
        } catch (SecurityException e) {
            Log.e(TAG, "getDeviceLocation: SecurityException: " + e.getMessage());
        }
    }

    // Перемещаем камеру по заданным координатам с заданным зумом
    private void moveCamera(LatLng latLng, float zoom) {
        Log.d(TAG, "moveCamera: перемещаем камеру на: широту: " + latLng.latitude + ", долготу: " + latLng.longitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
    }

    @Override
    protected void onStop() {
        super.onStop();

        SharedPreferences sharedPref = getSharedPreferences("com.lstu.kovalchuk.taxiservicefordriver", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("OrderID", orderID);
        editor.apply();
    }

    private void initToolbar() {
        android.support.v7.widget.Toolbar toolbar = findViewById(R.id.navigatorToolbar);
        toolbar.setOnMenuItemClickListener(menuItem -> false);

        toolbar.inflateMenu(R.menu.menu);
    }

    public void openMenu(MenuItem item) {
        Intent intent = new Intent(this, Menu.class);
        startActivity(intent);
    }

    public void openDetailOrder(View view) {
        Intent intent = new Intent(Navigator.this, DetailOrderView.class);
        if (client != null) intent.putExtra("ClientFullName", client.getFullName());
        intent.putExtra("WhenceAddress", order.getWhenceAddress());
        intent.putExtra("WhereAddress", order.getWhereAddress());
        intent.putExtra("ApproxCost", order.getApproxCost());
        intent.putExtra("CashlessPay", order.isCashlessPay());
        intent.putExtra("Comment", order.getComment());
        intent.putExtra("ApproxTimeToDest", order.getApproxTimeToDest());
        intent.putExtra("ApproxDistanceToDest", order.getApproxDistanceToDest());
        intent.putExtra("WaitingTime", order.getTimeWaiting());
        startActivity(intent);
    }

    public void closeOrder(View view) {
        if (order == null) openGlobal();

        Map<String, Object> cancelOrder = new HashMap<>();
        cancelOrder.put("driverUID", null);
        cancelOrder.put("timeWaiting", null);

        db = FirebaseFirestore.getInstance();
        db.collection("orders").document(orderID)
                .update(cancelOrder)
                .addOnSuccessListener(result -> {
                    openGlobal();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "closeOrder: " + e.getMessage());
                    Toast.makeText(Navigator.this, "Не удалось отменить заказ. Проверьте соединение с сетью", Toast.LENGTH_SHORT).show();
                });
    }

    private void openGlobal() {
        orderID = null;
        SharedPreferences sharedPref = getSharedPreferences("com.lstu.kovalchuk.taxiservicefordriver", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("OrderID", orderID);
        editor.apply();

        Intent intent = new Intent(Navigator.this, Global.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    public void openPhoneKeyboard(View view) {
        if (mCallingPermissionGranted) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(Navigator.this, "Нет права для совершения вызова", Toast.LENGTH_SHORT).show();
                return;
            }
            if (client == null) return;

            Intent intent = new Intent(Intent.ACTION_CALL);
            intent.setData(Uri.parse("tel:" + client.getPhone()));
            startActivity(intent);
        }
    }

    public void startWaitingClient(View view) {
        db.collection("orders").document(order.getID())
                .update("driverArrived", true)
                .addOnSuccessListener(result -> {
                    order.setDriverArrived(true);
                    Log.d(TAG, "startWaitingClient: клиент оповещен");
                    btnCancel.setVisibility(View.GONE);
                    btnArrived.setVisibility(View.GONE);
                    btnGo.setVisibility(View.VISIBLE);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "startWaitingClient: " + e.getMessage());
                });
    }

    public void startOrder(View view) {
        Timestamp dtbegin = new Timestamp(new Date());
        db.collection("orders").document(order.getID())
                .update("dtbegin", dtbegin)
                .addOnSuccessListener(result -> {
                    order.setDTbegin(dtbegin);
                    Log.d(TAG, "startOrder: поездка началась");
                    btnCall.setVisibility(View.GONE);
                    btnGo.setVisibility(View.GONE);

                    String position = order.getWhenceGeoPoint().getLatitude() + "," + order.getWhenceGeoPoint().getLongitude();
                    String destination = order.getWhereGeoPoint().getLatitude() + "," + order.getWhereGeoPoint().getLongitude();
                    routePrint(position, destination);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "startOrder: " + e.getMessage());
                });
    }
}
