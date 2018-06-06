package com.lstu.kovalchuk.taxiservicefordriver;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public final class Registration extends AppCompatActivity {

    private static final int RC_SIGN_IN = 123;
    private static final String TAG = "MyAppReg";

    private TextView tvTextMessage;
    private EditText metFullName;
    private EditText metBrandCar;
    private EditText metColorCar;
    private EditText metNumberCar;
    private EditText metAccountNumber;
    private TextView yandexMapsInfo;
    private Button btnReg;

    private boolean notCompleted;

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
        btnReg = findViewById(R.id.regBtnRegistration);

        //Ссылка для регистрации в Yandex.Money
        yandexMapsInfo = findViewById(R.id.regLink);
        if (Build.VERSION.SDK_INT >= 24) {
            yandexMapsInfo.setText(Html.fromHtml(getString(R.string.notAccountNumber), 1));
        } else {
            yandexMapsInfo.setText(Html.fromHtml(getString(R.string.notAccountNumber)));
        }

        yandexMapsInfo.setMovementMethod(LinkMovementMethod.getInstance());

        notCompleted = false;
        Bundle arguments = getIntent().getExtras();
        if (arguments != null) {
            String value = arguments.getString("Registration");
            if (value != null && value.equals("NotCompleted")) {
                notCompleted = true;
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        tvTextMessage.setText("");
    }

    private boolean isFullName(String str) {
        Pattern pattern = Pattern.compile("([a-zA-Z][a-z]* [a-zA-Z][a-z]*)|([а-яА-Я][а-я]* [а-яА-Я][а-я]*)");
        return pattern.matcher(str).matches();
    }

    private boolean isColor(String str) {
        Pattern pattern = Pattern.compile("([a-zA-Z][a-z]*[-]?[a-z]+)|([а-яА-Я][а-я]*[-]?[а-я]+)");
        return pattern.matcher(str).matches();
    }

    private boolean isNumberCar(String str) {
        Pattern pattern = Pattern.compile("[АВЕКМНОРСТУХ][0-9]{3}[АВЕКМНОРСТУХ]{2}/[0-9]{2,3}");
        return pattern.matcher(str).matches();
    }

    public void toRegister(View view) {
        String sFullName = metFullName.getText().toString();
        String sBrandCar = metBrandCar.getText().toString();
        String sColorCar = metColorCar.getText().toString();
        String sNumberCar = metNumberCar.getText().toString();
        String sAccountNumber = metAccountNumber.getText().toString();
        boolean errorFlag = false;
        if (!isFullName(sFullName)) {
            metFullName.setError("Поле должно содержать ваши Фамилию и Имя с заглавных букв через пробел");
            errorFlag = true;
        }
        if (sBrandCar.isEmpty()) {
            metBrandCar.setError("Поле не может быть пустым");
            errorFlag = true;
        }
        if (!isColor(sColorCar)) {
            metColorCar.setError("Поле должно содержать Цвет машины с большой или маленькой буквы. Допускается использование дефиса");
            errorFlag = true;
        }
        if (!isNumberCar(sNumberCar)) {
            metNumberCar.setError("Поле должно содержать Номер вашей машины в соответствии со стандартами РФ. Номер региона регистрации должен быть отделен слешем (например, С065МК/24)");
            errorFlag = true;
        }
        //
        //-- Проверка Номера счета Яндекс Денег --//
        //
        if (errorFlag) {
            return;
        }

        if (notCompleted) {
            tvTextMessage.setText("Вы не закончили регистрацию. Заполните поля ниже и зарегистрируйтесь");
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            updateUI(user);
        } else {
            List<AuthUI.IdpConfig> providers = Collections.singletonList(new AuthUI.IdpConfig.PhoneBuilder().build());

            startActivityForResult(
                    AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setAvailableProviders(providers)
                            .build(),
                    RC_SIGN_IN);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                // Successfully signed in
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                updateUI(user);
            } else {
                tvTextMessage.setText("Вы не завершили верификацию номера телефона, проверьте подключение к сети и попробуйте снова");
                updateUI(null);
            }
        }
    }

    private void updateUI(FirebaseUser user) {
        if(user!=null)
        {
            metFullName.setVisibility(View.GONE);
            metBrandCar.setVisibility(View.GONE);
            metColorCar.setVisibility(View.GONE);
            metNumberCar.setVisibility(View.GONE);
            metAccountNumber.setVisibility(View.GONE);
            btnReg.setVisibility(View.GONE);
            yandexMapsInfo.setVisibility(View.GONE);
            createAccountDb(user);
        }
        else
        {
            metFullName.setVisibility(View.VISIBLE);
            metBrandCar.setVisibility(View.VISIBLE);
            metColorCar.setVisibility(View.VISIBLE);
            metNumberCar.setVisibility(View.VISIBLE);
            metAccountNumber.setVisibility(View.VISIBLE);
            btnReg.setVisibility(View.VISIBLE);
            yandexMapsInfo.setVisibility(View.VISIBLE);
        }
    }

    private void createAccountDb(FirebaseUser user) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("drivers")
                .whereEqualTo(FieldPath.documentId(), user.getUid())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if(!task.getResult().isEmpty())
                        {
                            Log.d(TAG, "There is such a document");

                            Toast.makeText(this, "Авторизация завершена!", Toast.LENGTH_SHORT).show();

                            Intent intent = new Intent(this, Global.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
                        }
                        else {
                            Log.d(TAG, "No such document");

                            // Create a new user with a first and last name
                            Driver driver = new Driver(user.getPhoneNumber(),
                                    metFullName.getText().toString(),
                                    metBrandCar.getText().toString(),
                                    metColorCar.getText().toString(),
                                    metNumberCar.getText().toString(),
                                    metAccountNumber.getText().toString());

                            // Add a new document with a generated ID
                            db.collection("drivers").document(user.getUid())
                                    .set(driver)
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d(TAG, "DocumentSnapshot successfully written!");

                                        Toast.makeText(Registration.this, "Регистрация завершена!", Toast.LENGTH_SHORT).show();

                                        Intent intent = new Intent(Registration.this, Global.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                        finish();
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.w(TAG, "Error writing document", e);
                                        tvTextMessage.setText("Не удалось завершить регистрацию. Проверьте подключение к сети и попробуйте позднее");
                                    });
                        }
                    } else {
                        Log.d(TAG, "Error getting documents: ", task.getException());
                        updateUI(null);
                        tvTextMessage.setText("Не удалось соединение с сервером. Проверьте соединение с интернетом и повторите попытку");
                    }
                });
    }
}
