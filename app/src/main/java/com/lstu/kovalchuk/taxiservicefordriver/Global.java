package com.lstu.kovalchuk.taxiservicefordriver;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.MessageFormat;

public class Global extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    public static final String TAG = "Global";

    private FirebaseFirestore db;

    private ScrollView svListOrders;
    private LinearLayout llLoader;
    private LinearLayout llListOrders;
    private SwipeRefreshLayout srlRefresh;
    private TextView tvNotOrders;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_global);

        db = FirebaseFirestore.getInstance();

        svListOrders = findViewById(R.id.globalScrollViewListOrders);
        llLoader = findViewById(R.id.globalProgressBar);
        llListOrders = findViewById(R.id.globalListOrders);
        tvNotOrders = findViewById(R.id.globalNotOrders);
        tvNotOrders.setVisibility(View.GONE);

        srlRefresh = findViewById(R.id.globalRefresh);
        srlRefresh.setOnRefreshListener(this);
        srlRefresh.setColorSchemeColors(getResources().getColor(R.color.colorAccent));

        startLoader(false);

        getListOrders();

        initToolbar();
    }

    @Override
    protected void onStart() {
        super.onStart();

        SharedPreferences sharedPref = getSharedPreferences("com.lstu.kovalchuk.taxiservicefordriver", Context.MODE_PRIVATE);
        String orderID = sharedPref.getString("OrderID", null);
        String isTotalOrder = sharedPref.getString("isTotalOrder", null);
        if (orderID != null) {
            if (isTotalOrder == null) {
                Intent intent = new Intent(Global.this, Navigator.class);
                intent.putExtra("OrderID", orderID);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            } else {
                Intent intent = new Intent(Global.this, TotalOrder.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        }
    }

    private void startLoader(boolean start) {
        if (start) {
            svListOrders.setVisibility(View.GONE);
            llLoader.setVisibility(View.VISIBLE);
        } else {
            svListOrders.setVisibility(View.VISIBLE);
            llLoader.setVisibility(View.GONE);
        }
    }

    private void getListOrders() {
        startLoader(true);
        db.collection("orders")
                .whereEqualTo("cancel", false)
                .whereEqualTo("driverUID", null)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "getListOrders: данные заказа получены");
                        showListOrders(task.getResult());
                        startLoader(false);
                    } else {
                        Log.e(TAG, "onComplete: не удалось загрузить данные о заказах");
                        Toast.makeText(Global.this, "Не удалось загрузить список заказов", Toast.LENGTH_SHORT).show();
                        startLoader(false);
                    }
                });
    }

    private void showListOrders(QuerySnapshot result) {
        Order tmpOrder;
        llListOrders.removeAllViews();
        if (result.isEmpty())
            tvNotOrders.setVisibility(View.VISIBLE);
        else
            tvNotOrders.setVisibility(View.GONE);
        for (QueryDocumentSnapshot document : result) {
            tmpOrder = document.toObject(Order.class);
            getOrderView(tmpOrder);
        }
    }

    private void getOrderView(Order tmpOrder) {
        LinearLayout llOrder = new LinearLayout(this);
        LinearLayout llFirstRow = new LinearLayout(this);
        LinearLayout llSecondRow = new LinearLayout(this);
        LinearLayout llThirdRow = new LinearLayout(this);

        TextView tvTitle = new TextView(this);
        TextView tvDescription = new TextView(this);

        View viewLine = new View(this);

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);

        llFirstRow.setLayoutParams(layoutParams);
        llFirstRow.setOrientation(LinearLayout.HORIZONTAL);
        llFirstRow.setPadding(0, 5, 0, 0);

        tvTitle.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        tvTitle.setText(getResources().getText(R.string.whence));
        tvTitle.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));

        tvDescription.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 4f));
        tvDescription.setText(tmpOrder.getWhenceAddress());

        llFirstRow.addView(tvTitle);
        llFirstRow.addView(tvDescription);

        llSecondRow.setLayoutParams(layoutParams);
        llSecondRow.setOrientation(LinearLayout.HORIZONTAL);
        llSecondRow.setPadding(0, 5, 0, 0);

        tvTitle = new TextView(this);
        tvTitle.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        tvTitle.setText(getResources().getText(R.string.where));
        tvTitle.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));

        tvDescription = new TextView(this);
        tvDescription.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 4f));
        tvDescription.setText(tmpOrder.getWhereAddress());

        llSecondRow.addView(tvTitle);
        llSecondRow.addView(tvDescription);

        llThirdRow.setLayoutParams(layoutParams);
        llThirdRow.setOrientation(LinearLayout.HORIZONTAL);
        llThirdRow.setPadding(0, 5, 0, 5);

        tvTitle = new TextView(this);
        tvTitle.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        tvTitle.setText(getResources().getText(R.string.approximateCost));
        tvTitle.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));

        tvDescription = new TextView(this);
        LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 4f);
        layoutParams2.gravity = Gravity.CENTER;
        tvDescription.setLayoutParams(layoutParams2);
        tvDescription.setText(MessageFormat.format("{0} руб.", tmpOrder.getApproxCost().toString()));
        tvDescription.setTextSize(28);

        llThirdRow.addView(tvTitle);
        llThirdRow.addView(tvDescription);

        llOrder.setLayoutParams(layoutParams);
        llOrder.setOrientation(LinearLayout.VERTICAL);
        llOrder.setPadding(16, 0, 16, 0);
        llOrder.setOnClickListener(view -> {
            // Заказ выбран
            Intent intent = new Intent(Global.this, DetailOrder.class);
            intent.putExtra("orderID", tmpOrder.getID());
            intent.putExtra("clientUID", tmpOrder.getClientUID());
            intent.putExtra("whenceAddress", tmpOrder.getWhenceAddress());
            intent.putExtra("whenceLatLng", new LatLng(tmpOrder.getWhenceGeoPoint().getLatitude(), tmpOrder.getWhenceGeoPoint().getLongitude()));
            intent.putExtra("whereAddress", tmpOrder.getWhereAddress());
            intent.putExtra("approxCost", tmpOrder.getApproxCost());
            intent.putExtra("cashlessPay", tmpOrder.isCashlessPay());
            intent.putExtra("comment", tmpOrder.getComment());
            intent.putExtra("approxTimeToDest", tmpOrder.getApproxTimeToDest());
            intent.putExtra("approxDistanceToDest", tmpOrder.getApproxDistanceToDest());
            startActivity(intent);
        });

        llOrder.addView(llFirstRow);
        llOrder.addView(llSecondRow);
        llOrder.addView(llThirdRow);

        layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1);
        layoutParams.setMarginEnd(16);
        layoutParams.setMarginStart(16);
        viewLine.setLayoutParams(layoutParams);
        viewLine.setBackground(getDrawable(R.color.colorBlack));

        llListOrders.addView(llOrder);
        llListOrders.addView(viewLine);
    }

    private void initToolbar() {
        android.support.v7.widget.Toolbar toolbar = findViewById(R.id.globalToolbar);
        toolbar.setOnMenuItemClickListener(menuItem -> false);

        toolbar.inflateMenu(R.menu.menu);
    }

    public void openMenu(MenuItem item) {
        Intent intent = new Intent(this, Menu.class);
        startActivity(intent);
    }

    @Override
    public void onRefresh() {
        srlRefresh.setRefreshing(true);
        srlRefresh.postDelayed(() -> {
            srlRefresh.setRefreshing(false);

            getListOrders();
        }, 3000);
    }
}
