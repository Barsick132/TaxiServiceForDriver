package com.lstu.kovalchuk.taxiservicefordriver;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.regex.Pattern;

public final class Registration extends AppCompatActivity {

    private TextView tvTextMessage;
    private EditText metFullName;
    private EditText metBrandCar;
    private EditText metColorCar;
    private EditText metNumberCar;
    private EditText metAccountNumber;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        tvTextMessage = findViewById(R.id.regTextMessage);
        metFullName = findViewById(R.id.regFullName);
        metBrandCar = findViewById(R.id.regBrand);
        metColorCar = findViewById(R.id.regColor);
        metNumberCar = findViewById(R.id.regNumber1);
        metAccountNumber = findViewById(R.id.regAccountNumber);

        //Ссылка для регистрации в Yandex.Money
        TextView yandexMapsInfo = findViewById(R.id.regLink);
        if (Build.VERSION.SDK_INT >= 24) {
            yandexMapsInfo.setText(Html.fromHtml(getString(R.string.notAccountNumber), 1));
        } else {
            yandexMapsInfo.setText(Html.fromHtml(getString(R.string.notAccountNumber)));
        }

        yandexMapsInfo.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private boolean isFullName(String str)
    {
        Pattern pattern = Pattern.compile("[A-ZА-Я][a-zA-Zа-яА-Я]* [A-ZА-Я][a-zA-Zа-яА-Я]*");
        return pattern.matcher(str).matches();
    }
    private boolean isColor(String str)
    {
        Pattern pattern = Pattern.compile("[a-zA-Zа-яА-Я][a-zа-я]*[-]?[a-zа-я]+");
        return pattern.matcher(str).matches();
    }
    private boolean isNumberCar(String str)
    {
        Pattern pattern = Pattern.compile("[АВЕКМНОРСТУХ][0-9]{3}[АВЕКМНОРСТУХ]{2}/[0-9]{2,3}");
        return pattern.matcher(str).matches();
    }

    public void toRegister(View view) {
        String sFullName = metFullName.getText().toString();
        String sBrandCar = metBrandCar.getText().toString();
        String sColorCar = metColorCar.getText().toString();
        String sNumberCar = metNumberCar.getText().toString();
        String sAccountNumber = metAccountNumber.getText().toString();
        if (!isFullName(sFullName)) {
            metFullName.setError("Поле должно содержать ваши Фамилию и Имя с заглавных букв через пробел");
        }
        if (sBrandCar.isEmpty()) {
            metBrandCar.setError("Поле не может быть пустым");
        }
        if (!isColor(sColorCar)) {
            metColorCar.setError("Поле должно содержать Цвет машины с большой или маленькой буквы. Допускается использование дефиса");
        }
        if (!isNumberCar(sNumberCar)) {
            metNumberCar.setError("Поле должно содержать Номер вашей машины в соответствии со стандартами РФ. Номер региона регистрации должен быть отделен слешем (например, С065МК/24)");
        }
    }
}
