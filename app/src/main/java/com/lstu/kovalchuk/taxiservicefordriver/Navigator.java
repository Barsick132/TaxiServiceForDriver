package com.lstu.kovalchuk.taxiservicefordriver;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;

import com.yandex.mapkit.Animation;
import com.yandex.mapkit.MapKitFactory;
import com.yandex.mapkit.geometry.Point;
import com.yandex.mapkit.map.CameraPosition;
import com.yandex.mapkit.mapview.MapView;

public class Navigator extends AppCompatActivity{

    private MapView mapview;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MapKitFactory.setApiKey("ce22acd0-3f1a-4e4b-bb04-293ac4af654a");
        MapKitFactory.initialize(this);

        setContentView(R.layout.activity_navigator);
        mapview = (MapView)findViewById(R.id.navMap);
        mapview.getMap().move(
                new CameraPosition(new Point(55.751574, 37.573856), 11.0f, 0.0f, 0.0f),
                new Animation(Animation.Type.SMOOTH, 0),
                null);

        initToolbar();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapview.onStop();
        MapKitFactory.getInstance().onStop();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapview.onStart();
        MapKitFactory.getInstance().onStart();
    }

    private void initToolbar() {
        android.support.v7.widget.Toolbar toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.navigatorToolbar);
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

    public void openDetailOrder(View view) {
        Intent intent = new Intent(this, DetailOrder.class);
        startActivity(intent);
    }

    public void closeOrder(View view) {
        Intent intent = new Intent(this, Global.class);
        startActivity(intent);
    }

    public void openPhoneKeyboard(View view) {
    }

    private void updateNavigator(){

    }
}
