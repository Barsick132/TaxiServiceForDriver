package com.lstu.kovalchuk.taxiservicefordriver;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void goRegistration(View view) {
        Intent intent = new Intent(this, Registration.class);
        startActivity(intent);
    }

    public void signIn(View view) {
        Intent intent = new Intent(this, Global.class);
        startActivity(intent);
    }
}