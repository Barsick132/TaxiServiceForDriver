package com.lstu.kovalchuk.taxiservicefordriver;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class Global extends AppCompatActivity {

    private android.support.v7.widget.Toolbar toolbar;
    private List<Order> listOrders = new ArrayList<Order>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_global);

        initToolbar();
    }

    private void initToolbar() {
        toolbar = (android.support.v7.widget.Toolbar)findViewById(R.id.toolbar);
        toolbar.setOnMenuItemClickListener(new android.support.v7.widget.Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                return false;
            }
        });

        toolbar.inflateMenu(R.menu.menu);
    }

    public void openMenu(MenuItem item) {
        Intent intent = new Intent(this, Menu.class);
        startActivity(intent);
    }

    public void clickOrder(View view) {
        Intent intent = new Intent(this, DetailOrder.class);
        startActivity(intent);
    }

    private void updeteListOrders() {

    }
}
