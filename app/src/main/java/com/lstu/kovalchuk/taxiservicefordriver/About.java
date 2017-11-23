package com.lstu.kovalchuk.taxiservicefordriver;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

public class About extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        TextView versionString = (TextView)findViewById(R.id.textView15);
        versionString.setText(BuildConfig.VERSION_NAME);

        TextView yandexMapsInfo = (TextView)findViewById(R.id.textView17);
        if (Build.VERSION.SDK_INT >= 24)
        {
            yandexMapsInfo.setText(Html.fromHtml(getString(R.string.termsOfUseYandexMapsInfo), 1));
        }
        else {
            yandexMapsInfo.setText(Html.fromHtml(getString(R.string.termsOfUseYandexMapsInfo)));
        }

        yandexMapsInfo.setMovementMethod(LinkMovementMethod.getInstance());
    }
}
