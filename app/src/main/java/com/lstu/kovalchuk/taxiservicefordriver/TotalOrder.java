package com.lstu.kovalchuk.taxiservicefordriver;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class TotalOrder extends AppCompatActivity {
    private Order totalOrder;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_total_order);
    }

    public void goListOrders(View view) {
        Intent intent = new Intent(this, Global.class);
        startActivity(intent);
    }
}
