package com.lstu.kovalchuk.taxiservicefordriver;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class Menu extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
    }

    public void openAbout(View view) {
        Intent intent = new Intent(this, About.class);
        startActivity(intent);
    }

    public void openSupport(View view) {
        Intent intent = new Intent(this, Support.class);
        startActivity(intent);
    }

    public void openAssessments(View view) {
        Intent intent = new Intent(this, Assessments.class);
        startActivity(intent);
    }

    public void openProfile(View view) {
        Intent intent = new Intent(this, Profile.class);
        startActivity(intent);
    }
}
