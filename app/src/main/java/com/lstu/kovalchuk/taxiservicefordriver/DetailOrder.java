package com.lstu.kovalchuk.taxiservicefordriver;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.gson.Gson;
import com.lstu.kovalchuk.taxiservicefordriver.mapapi.Route;
import com.lstu.kovalchuk.taxiservicefordriver.mapapi.RouteResponse;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

public class DetailOrder extends AppCompatActivity {

    public static final String TAG = "DetailOrder";
    public static final String FINE_LOCATION = android.Manifest.permission.ACCESS_FINE_LOCATION;
    public static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    public static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;

    private FirebaseFirestore db;
    private Boolean mLocationPermissionGranted = false;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private Location currentLocation;
    private Order order;
    private TextView tvClientFullName;
    private TextView tvWhenceAddress;
    private TextView tvWhereAddress;
    private TextView tvApproxCost;
    private TextView tvCashlessPay;
    private TextView tvComment;
    private TextView tvApproxTimeToDest;
    private TextView tvApproxDistanceToDest;
    private TextView tvWaitingTime;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_order);

        db = FirebaseFirestore.getInstance();

        tvClientFullName = findViewById(R.id.detailOrderFullName);
        tvWhenceAddress = findViewById(R.id.detailOrderWhence);
        tvWhereAddress = findViewById(R.id.detailOrderWhere);
        tvApproxCost = findViewById(R.id.detailOrderApproxCost);
        tvCashlessPay = findViewById(R.id.detailOrderCashlessPay);
        tvComment = findViewById(R.id.detailOrderComment);
        tvApproxTimeToDest = findViewById(R.id.detailOrderApproxTimeToDest);
        tvApproxDistanceToDest = findViewById(R.id.detailOrderApproxDistanceToDest);
        tvWaitingTime = findViewById(R.id.detailOrderWaitingTime);

        Bundle arguments = getIntent().getExtras();
        if (arguments != null) {
            order = new Order();
            order.setID(arguments.getString("orderID"));
            order.setClientUID(arguments.getString("clientUID"));
            order.setWhenceAddress(arguments.getString("whenceAddress"));
            LatLng whenceLatLng = (LatLng) arguments.get("whenceLatLng");
            order.setWhereAddress(arguments.getString("whereAddress"));
            order.setApproxCost(arguments.getInt("approxCost"));
            order.setCashlessPay(arguments.getBoolean("cashlessPay"));
            order.setComment(arguments.getString("comment"));
            order.setApproxTimeToDest(arguments.getInt("approxTimeToDest"));
            order.setApproxDistanceToDest(arguments.getInt("approxDistanceToDest"));

            tvWaitingTime.setText("Вычисление...");

            setKonownData(order.getWhenceAddress(), order.getWhereAddress(),
                    order.getApproxCost(), order.isCashlessPay(),
                    order.getComment(), order.getApproxTimeToDest(),
                    order.getApproxDistanceToDest());

            if (order.getClientUID() != null)
                getClientFullName(order.getClientUID());

            if (whenceLatLng != null) {
                order.setWhenceGeoPoint(new GeoPoint(whenceLatLng.latitude, whenceLatLng.longitude));
            }

            getLocationPermission();
        }
    }

    private static class GetQuery {
        OkHttpClient client = new OkHttpClient();

        void run(String url, Callback callback) {
            Request request = new Request.Builder()
                    .url(url)
                    .build();

            client.newCall(request).enqueue(callback);
        }
    }

    private void getWaitingTime(GeoPoint whenceGeoPoint) {
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        try {
            if (mLocationPermissionGranted) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                    return;
                }
                Task location = mFusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "onComplete: местоположение определено!");
                        currentLocation = (Location) task.getResult();
                        GeoPoint currentGeoPoin = new GeoPoint(currentLocation.getLatitude(), currentLocation.getLongitude());

                        // Запрашиваем данные о маршруте до клиента
                        String position = currentLocation.getLatitude() + "," + currentLocation.getLongitude();
                        String destination = whenceGeoPoint.getLatitude() + "," + whenceGeoPoint.getLongitude();
                        GetRoute(position, destination, "true", "ru");
                    } else {
                        // Если НЕ удалось определить местоположение
                        Log.d(TAG, "onComplete: местоположение НЕ определено");
                        Toast.makeText(DetailOrder.this, "Не удалось определить текущее местоположение", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        } catch (Exception e) {
            Log.e(TAG, "getDeviceLocation: SecurityException: " + e.getMessage());
        }
    }

    public void GetRoute(String origin, String destination, String sensor, String language) {
        GetQuery query = new GetQuery();
        String url = "https://maps.googleapis.com/maps/api/directions/json?" +
                "origin=" + origin +
                "&destination=" + destination +
                "&sensor=" + sensor +
                "&language=" + language +
                "&key=" + getResources().getString(R.string.google_maps_webkey);

        query.run(url, new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                try {
                    runOnUiThread(() -> {
                        Toast.makeText(DetailOrder.this, "Проверьте соединение с сетью", Toast.LENGTH_SHORT).show();
                        tvWaitingTime.setText(R.string.errorGetting);
                    });
                    Log.e(TAG, "onFailure: не удалось получить маршруты в формате json" + e.getMessage());
                } catch (Exception ex) {
                    Log.e(TAG, "onFailure: " + e.getMessage());
                }
            }

            @Override
            public void onResponse(com.squareup.okhttp.Response response) {
                try {
                    String jsonString = response.body().string();
                    Gson g = new Gson();
                    RouteResponse routeResponse = g.fromJson(jsonString, RouteResponse.class);

                    if (routeResponse.getStatus().equals("OK")) {
                        DetailOrder.this.runOnUiThread(() -> {

                            Route minTimeRoute = routeResponse.getRoutes().get(0);
                            for (Route route : routeResponse.getRoutes()) {
                                if (route.getLegs().get(0).getDuration().getValue() < minTimeRoute.getLegs().get(0).getDuration().getValue()) {
                                    minTimeRoute = route;
                                }
                            }
                            order.setTimeWaiting(minTimeRoute.getLegs().get(0).getDuration().getValue());

                            Double hours = Math.floor(order.getTimeWaiting() / (double) 3600);
                            Double minutes = Math.floor(order.getTimeWaiting() / (double) 60) - hours.intValue() * 60;
                            String strApproxTime;
                            if (hours == 0) {
                                strApproxTime = MessageFormat.format("{0} мин.", minutes.intValue());
                            } else {
                                strApproxTime = MessageFormat.format("{0} ч. {1} мин.", hours.intValue(), minutes.intValue());
                            }
                            runOnUiThread(() -> tvWaitingTime.setText(strApproxTime));
                        });
                    }
                } catch (Exception ex) {
                    Log.d(TAG, "onResponse: ошибка при получении маршрута в формате json");
                    runOnUiThread(() -> {
                        Toast.makeText(DetailOrder.this, "Проверьте соединение с сетью", Toast.LENGTH_SHORT).show();
                        tvWaitingTime.setText(R.string.errorGetting);
                    });
                }
            }
        });
    }

    private void getClientFullName(String clientUID) {

        db.collection("clients").document(clientUID)
                .get()
                .addOnSuccessListener(result -> {
                    Client client = result.toObject(Client.class);
                    if (client != null) {
                        tvClientFullName.setText(client.getFullName());
                    } else {
                        Log.d(TAG, "getClientFullName: не удалось получить имя клиента");
                        tvClientFullName.setText(R.string.errorGetting);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "getClientFullName: " + e.getMessage());
                    tvClientFullName.setText(R.string.errorGetting);
                });
    }

    private void setKonownData(String whenceAddress, String whereAddress, Integer approxCost,
                               boolean cashlessPay, String comment, Integer approxTimeToDest,
                               Integer approxDistanceToDest) {
        if (whenceAddress != null) tvWhenceAddress.setText(whenceAddress);
        if (whereAddress != null) tvWhereAddress.setText(whereAddress);
        if (approxCost != null)
            tvApproxCost.setText(MessageFormat.format("{0} руб.", approxCost.toString()));
        if (!cashlessPay) {
            tvCashlessPay.setText("Наличными");
        } else {
            tvCashlessPay.setText("Безналичный расчет");
        }
        if (comment != null) tvComment.setText(comment);
        if (approxTimeToDest != null) {
            Double hours = Math.floor(approxTimeToDest / (double) 3600);
            Double minutes = Math.floor(approxTimeToDest / (double) 60) - hours.intValue() * 60;
            String strApproxTime;
            if (hours == 0) {
                strApproxTime = MessageFormat.format("{0} мин.", minutes.intValue());
            } else {
                strApproxTime = MessageFormat.format("{0} ч. {1} мин.", hours.intValue(), minutes.intValue());
            }
            tvApproxTimeToDest.setText(strApproxTime);
        }
        if (approxDistanceToDest != null) {
            tvApproxDistanceToDest.setText(MessageFormat.format("{0} км.",
                    Math.round(approxDistanceToDest / 100) / (double) 10));
        }
    }

    public void openNavigator(View view) {
        Map<String, Object> startOrder = new HashMap<>();
        startOrder.put("driverUID", FirebaseAuth.getInstance().getUid());
        startOrder.put("timeWaiting", order.getTimeWaiting());

        db.collection("orders").document(order.getID())
                .update(startOrder)
                .addOnCompleteListener(result -> {
                    Intent intent = new Intent(this, Navigator.class);
                    intent.putExtra("OrderID", order.getID());
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "openNavigator: " + e.getMessage());
                    Toast.makeText(DetailOrder.this, "Проверьте подключение к сети", Toast.LENGTH_SHORT).show();
                });
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

                if (order.getWhenceGeoPoint() != null)
                    getWaitingTime(order.getWhenceGeoPoint());
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
                    if (order.getWhenceGeoPoint() != null)
                        getWaitingTime(order.getWhenceGeoPoint());
                }
            }
        }
    }
}
