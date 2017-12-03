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
    private EditText etPhone = (EditText)findViewById(R.id.editText3);
    private EditText etLastName = (EditText)findViewById(R.id.editText4);
    private EditText etFirstName = (EditText)findViewById(R.id.editText5);
    private EditText etBrandCar = (EditText)findViewById(R.id.editText8);
    private EditText etColorCar = (EditText)findViewById(R.id.editText9);
    private EditText etNumberCar = (EditText)findViewById(R.id.editText10);
    private EditText etAccountNumber = (EditText)findViewById(R.id.editText11);
    private EditText etPassword = (EditText)findViewById(R.id.editText6);
    private EditText etConfirmPassword = (EditText)findViewById(R.id.editText7);


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        TextView yandexMapsInfo = (TextView)findViewById(R.id.textView1);
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
