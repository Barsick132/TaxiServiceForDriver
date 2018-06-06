package com.lstu.kovalchuk.taxiservicefordriver;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;

public class TotalOrder extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {
    public static final String TAG = "TotalOrder";

    private TextView tvClientFullName;
    private TextView tvWhenceAddress;
    private TextView tvWhereAddress;
    private TextView tvCost;
    private TextView tvCashlessPay;
    private TextView tvTimeComplete;
    private TextView tvDTbegin;
    private TextView tvDTend;
    private SwipeRefreshLayout srlRefresh;

    private FirebaseFirestore db;
    private String isTotalOrder;
    private String orderID;
    private Order order;
    private Client client;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_total_order);

        db = FirebaseFirestore.getInstance();
        isTotalOrder = "true";

        tvClientFullName = findViewById(R.id.totalOrderClientFullName);
        tvWhenceAddress = findViewById(R.id.totalOrderWhence);
        tvWhereAddress = findViewById(R.id.totalOrderWhere);
        tvCost = findViewById(R.id.totalOrderCost);
        tvCashlessPay = findViewById(R.id.totalOrderCashlessPay);
        tvTimeComplete = findViewById(R.id.totalOrderTimeComplete);
        tvDTbegin = findViewById(R.id.totalOrderDTstart);
        tvDTend = findViewById(R.id.totalOrderDTend);

        srlRefresh = findViewById(R.id.totalOrderRefresh);
        srlRefresh.setOnRefreshListener(this);
        srlRefresh.setColorSchemeColors(getResources().getColor(R.color.colorAccent));
    }

    private void setData(String clientFullName, String whenceAddress, String whereAddress,
                         Integer totalCost, boolean cashlessPay, Timestamp dTbegin, Timestamp dTend) {
        if(clientFullName!=null) tvClientFullName.setText(clientFullName);
        if(whenceAddress!=null) tvWhenceAddress.setText(whenceAddress);
        if(whereAddress!=null) tvWhereAddress.setText(whereAddress);
        if(totalCost!=null) tvCost.setText(MessageFormat.format("{0} руб.", totalCost.intValue()));

        if (!cashlessPay) {
            tvCashlessPay.setText("Наличными");
        } else {
            tvCashlessPay.setText("Безналичный расчет");
        }

        DateFormat df = new SimpleDateFormat("dd.MM.yyyy HH:mm");
        if(dTbegin!=null) {
            tvDTbegin.setText(df.format(dTbegin.toDate()));
        }
        if(dTend!=null) {
            tvDTend.setText(df.format(dTend.toDate()));
        }
        if(dTbegin!=null && dTend!=null){
            Long timeComplete = (dTend.toDate().getTime() - dTbegin.toDate().getTime())/1000;
            Double hours = Math.floor(timeComplete / (double) 3600);
            Double minutes = Math.floor(timeComplete / (double) 60) - hours.intValue() * 60;
            String strApproxTime;
            if (hours == 0) {
                strApproxTime = MessageFormat.format("{0} мин.", minutes.intValue());
            } else {
                strApproxTime = MessageFormat.format("{0} ч. {1} мин.", hours.intValue(), minutes.intValue());
            }
            tvTimeComplete.setText(strApproxTime);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        SharedPreferences sharedPref = getSharedPreferences("com.lstu.kovalchuk.taxiservicefordriver", Context.MODE_PRIVATE);
        orderID = sharedPref.getString("OrderID", null);
        if (orderID == null) {
            isTotalOrder = null;
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString("isTotalOrder", isTotalOrder);
            editor.putString("OrderID", orderID);
            editor.apply();

            Intent intent = new Intent(TotalOrder.this, Global.class);
            startActivity(intent);
            finish();
        }

        getOrder();
    }

    private void getOrder() {
        db.collection("orders").document(orderID)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    order = documentSnapshot.toObject(Order.class);
                    if(order!=null){
                        getClient(order.getClientUID());
                    }
                })
                .addOnFailureListener(aVoid -> {
                    Toast.makeText(TotalOrder.this,
                            "Не удалось получить заказ. Проверьте соединение с сетью и обновите страницу",
                            Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "getOrder: не удалось проверить статус заказа в БД");
                });
    }

    private void getClient(String clientUID) {
        if (clientUID != null) {
            db.collection("clients").document(clientUID)
                    .get()
                    .addOnSuccessListener(result -> {
                        client = result.toObject(Client.class);
                        String clientFullName;
                        if(client!=null) clientFullName = client.getFullName();
                        else clientFullName = null;

                        setData(clientFullName,order.getWhenceAddress(),
                                order.getWhereAddress(),order.getTotalCost(),
                                order.isCashlessPay(),order.getDTbegin(),
                                order.getDTend());
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "getClient: " + e.getMessage());
                        Toast.makeText(TotalOrder.this, "Не удалось получить сведения о клиенте. Проверьте соединение с сетью", Toast.LENGTH_SHORT).show();
                    });
        } else {
            Log.d(TAG, "getClient: не удалось получить сведения о клиенте");
            Toast.makeText(TotalOrder.this, "Не удалось получить сведения о клиенте. Проверьте соединение с сетью", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        SharedPreferences sharedPref = getSharedPreferences("com.lstu.kovalchuk.taxiservicefordriver", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("isTotalOrder", isTotalOrder);
        editor.apply();
    }

    public void goListOrders(View view) {
        isTotalOrder = null;
        orderID = null;
        SharedPreferences sharedPref = getSharedPreferences("com.lstu.kovalchuk.taxiservicefordriver", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("isTotalOrder", isTotalOrder);
        editor.putString("OrderID", orderID);
        editor.apply();

        Intent intent = new Intent(TotalOrder.this, Global.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void onRefresh() {
        srlRefresh.setRefreshing(true);
        srlRefresh.postDelayed(() -> {
            srlRefresh.setRefreshing(false);

            getOrder();
        }, 3000);
    }
}
