package com.lstu.kovalchuk.taxiservicefordriver;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class DetailOrder extends AppCompatActivity {

    private Order detailOrder;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_order);
    }

    public void openNavigator(View view) {
        Intent intent = new Intent(this, Navigator.class);
        startActivity(intent);
    }
}
