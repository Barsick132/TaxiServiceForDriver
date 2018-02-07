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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        EditText etPhone = (EditText)findViewById(R.id.regPhone);
        EditText etLastName = (EditText)findViewById(R.id.regFamily);
        EditText etFirstName = (EditText)findViewById(R.id.regName);
        EditText etBrandCar = (EditText)findViewById(R.id.regBrand);
        EditText etColorCar = (EditText)findViewById(R.id.regColor);
        EditText etNumberCar = (EditText)findViewById(R.id.regNumber1);
        EditText etAccountNumber = (EditText)findViewById(R.id.regAccountNumber);
        EditText etPassword = (EditText)findViewById(R.id.regPass1);
        EditText etConfirmPassword = (EditText)findViewById(R.id.regPass2);

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
