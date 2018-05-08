package com.lstu.kovalchuk.taxiservicefordriver;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 123;
    private static final String TAG = "MyAppMain";
    private FirebaseAuth mAuth;

    private TextView tvTextMessage;
    private TextView tvQuestion;
    private LinearLayout llButtons;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        tvTextMessage = findViewById(R.id.mainTextMessage);
        tvQuestion = findViewById(R.id.mainQuestion);
        llButtons = findViewById(R.id.mainButtons);
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser user = mAuth.getCurrentUser();
        updateUI(user);
    }

    private void updateUI(FirebaseUser user) {
        if(user!=null){
            llButtons.setVisibility(View.GONE);
            tvQuestion.setVisibility(View.GONE);
            isAccountDbCreated(user);
        }
        else{
            llButtons.setVisibility(View.VISIBLE);
            tvQuestion.setVisibility(View.VISIBLE);
        }
    }

    public void goRegistration(View view) {
        Intent intent = new Intent(this, Registration.class);
        startActivity(intent);
    }

    public void signIn(View view) {
        List<AuthUI.IdpConfig> providers = Collections.singletonList(new AuthUI.IdpConfig.PhoneBuilder().build());

        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .build(),
                RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                // Successfully signed in
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            } else {
                tvTextMessage.setText("Убедитесь в корректности введенных данных, проверьте подключение к сети и попробуйте снова");
                updateUI(null);
            }
        }
    }

    private void isAccountDbCreated(FirebaseUser user) {
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
                            startActivity(intent);
                            finish();
                        }
                        else {
                            Log.d(TAG, "No such document");

                            Intent intent = new Intent(this, Registration.class);
                            intent.putExtra("Registration", "NotCompleted");
                            startActivity(intent);
                        }
                    } else {
                        Log.d(TAG, "Error getting documents: ", task.getException());

                        tvTextMessage.setText("Не удалось завершить авторизацию. Проверьте соединение с интернетом и повторите попытку");
                    }
                });
    }

}
