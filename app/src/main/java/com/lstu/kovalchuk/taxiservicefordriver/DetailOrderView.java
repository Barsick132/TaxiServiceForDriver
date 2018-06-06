package com.lstu.kovalchuk.taxiservicefordriver;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import java.text.MessageFormat;

public class DetailOrderView extends AppCompatActivity {

    private TextView tvClientFullName;
    private TextView tvWhenceAddress;
    private TextView tvWhereAddress;
    private TextView tvApproxCost;
    private TextView tvCashlessPay;
    private TextView tvComment;
    private TextView tvApproxTimeToDest;
    private TextView tvApproxDistanceToDest;
    private TextView tvWaitingTime;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_order_view);

        tvClientFullName = findViewById(R.id.dovFullName);
        tvWhenceAddress = findViewById(R.id.dovWhence);
        tvWhereAddress = findViewById(R.id.dovWhere);
        tvApproxCost = findViewById(R.id.dovApproxCost);
        tvCashlessPay = findViewById(R.id.dovCashlessPay);
        tvComment = findViewById(R.id.dovComment);
        tvApproxTimeToDest = findViewById(R.id.dovApproxTimeToDest);
        tvApproxDistanceToDest = findViewById(R.id.dovApproxDistanceToDest);
        tvWaitingTime = findViewById(R.id.dovWaitingTime);

        Bundle arguments = getIntent().getExtras();
        // Если есть переданные аргументы
        if (arguments != null) {
            // Получаем эти аргументы как текущие координаты
            setData(arguments.getString("ClientFullName"),
                    arguments.getString("WhenceAddress"),
                    arguments.getString("WhereAddress"),
                    arguments.getInt("ApproxCost"),
                    arguments.getBoolean("CashlessPay"),
                    arguments.getString("Comment"),
                    arguments.getInt("ApproxTimeToDest"),
                    arguments.getInt("ApproxDistanceToDest"),
                    arguments.getInt("WaitingTime"));
        }
    }

    private void setData(String clientFullName, String whenceAddress, String whereAddress, Integer approxCost,
                         boolean cashlessPay, String comment, Integer approxTimeToDest,
                         Integer approxDistanceToDest, Integer approxTimeWait) {
        if (clientFullName != null) tvClientFullName.setText(clientFullName);
        if (whenceAddress != null) tvWhenceAddress.setText(whenceAddress);
        if (whereAddress != null) tvWhereAddress.setText(whereAddress);
        if (approxCost != null)
            tvApproxCost.setText(MessageFormat.format("{0} руб.", approxCost.toString()));
        if (!cashlessPay) {
            tvCashlessPay.setText("Наличными");
        } else {
            tvCashlessPay.setText("Безналичный расчет");
        }
        if (comment != null) tvComment.setText(comment);
        if (approxTimeToDest != null) {
            Double hours = Math.floor(approxTimeToDest / (double) 3600);
            Double minutes = Math.floor(approxTimeToDest / (double) 60) - hours.intValue() * 60;
            String strApproxTime;
            if (hours == 0) {
                strApproxTime = MessageFormat.format("{0} мин.", minutes.intValue());
            } else {
                strApproxTime = MessageFormat.format("{0} ч. {1} мин.", hours.intValue(), minutes.intValue());
            }
            tvApproxTimeToDest.setText(strApproxTime);
        }
        if (approxDistanceToDest != null) {
            tvApproxDistanceToDest.setText(MessageFormat.format("{0} км.",
                    Math.round(approxDistanceToDest / 100) / (double) 10));
        }
        if (approxTimeWait != null) {
            Double hours = Math.floor(approxTimeWait / (double) 3600);
            Double minutes = Math.floor(approxTimeWait / (double) 60) - hours.intValue() * 60;
            String strApproxTime;
            if (hours == 0) {
                strApproxTime = MessageFormat.format("{0} мин.", minutes.intValue());
            } else {
                strApproxTime = MessageFormat.format("{0} ч. {1} мин.", hours.intValue(), minutes.intValue());
            }
            tvWaitingTime.setText(strApproxTime);
        }
    }
}
