package com.lstu.kovalchuk.taxiservicefordriver;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class Registration extends AppCompatActivity {
    private EditText etPhone = (EditText)findViewById(R.id.regPhone);
    private EditText etLastName = (EditText)findViewById(R.id.regFamily);
    private EditText etFirstName = (EditText)findViewById(R.id.regName);
    private EditText etBrandCar = (EditText)findViewById(R.id.regBrand);
    private EditText etColorCar = (EditText)findViewById(R.id.regColor);
    private EditText etNumberCar = (EditText)findViewById(R.id.regNumber1);
    private EditText etAccountNumber = (EditText)findViewById(R.id.regAccountNumber);
    private EditText etPassword = (EditText)findViewById(R.id.regPass1);
    private EditText etConfirmPassword = (EditText)findViewById(R.id.regPass2);


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        TextView yandexMapsInfo = (TextView)findViewById(R.id.regLink);
        if (Build.VERSION.SDK_INT >= 24)
        {
            yandexMapsInfo.setText(Html.fromHtml(getString(R.string.notAccountNumber), 1));
        }
        else {
            yandexMapsInfo.setText(Html.fromHtml(getString(R.string.notAccountNumber)));
        }

        yandexMapsInfo.setMovementMethod(LinkMovementMethod.getInstance());
    }


    public void toRegister(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}
