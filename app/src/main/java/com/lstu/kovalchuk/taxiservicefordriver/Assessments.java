package com.lstu.kovalchuk.taxiservicefordriver;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class Assessments extends AppCompatActivity {

    private List<Estimate> listEstimate = new ArrayList<Estimate>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assessments);
    }

    private void getEstimates(){

    }
}
