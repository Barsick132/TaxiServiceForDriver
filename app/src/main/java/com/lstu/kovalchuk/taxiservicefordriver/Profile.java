package com.lstu.kovalchuk.taxiservicefordriver;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;

public class Profile extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        DataProfile dataProfile = new DataProfile();
        EditText etFirstName = (EditText)findViewById(R.id.profileName);
        EditText etLastName = (EditText)findViewById(R.id.profileFamily);
        EditText etPatronymic = (EditText)findViewById(R.id.profilePatronymic);
        EditText etPhone = (EditText)findViewById(R.id.profilePhone);
        EditText etBrandCar = (EditText)findViewById(R.id.profileBrand);
        EditText etColorCar = (EditText)findViewById(R.id.profileColor);
        EditText etNumberCar = (EditText)findViewById(R.id.profileNumber1);
        EditText etNumberAccount = (EditText)findViewById(R.id.profileAccountNumber);
        EditText etPassword = (EditText)findViewById(R.id.profilePass1);
        EditText etConfirmPassword = (EditText)findViewById(R.id.profilePass2);
    }

    public void saveDataProfile(View view) {

    }
}
