package com.daysuntil.app;

import android.app.DatePickerDialog;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private EditText etEventName;
    private TextView tvSelectedDate, tvDaysLeft;
    private Button btnPickDate, btnSave, btnAddWidget;
    private Calendar selectedDate;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs = getSharedPreferences("days_until_prefs", MODE_PRIVATE);
        selectedDate = Calendar.getInstance();

        etEventName = findViewById(R.id.etEventName);
        tvSelectedDate = findViewById(R.id.tvSelectedDate);
        tvDaysLeft = findViewById(R.id.tvDaysLeft);
        btnPickDate = findViewById(R.id.btnPickDate);
        btnSave = findViewById(R.id.btnSave);
        btnAddWidget = findViewById(R.id.btnAddWidget);

        loadSavedData();

        btnPickDate.setOnClickListener(v -> showDatePicker());
        btnSave.setOnClickListener(v -> saveData());
        btnAddWidget.setOnClickListener(v -> addWidget());
    }

    private void showDatePicker() {
        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    selectedDate.set(year, month, dayOfMonth, 0, 0, 0);
                    updateDateDisplay();
                },
                selectedDate.get(Calendar.YEAR),
                selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH)
        );
        dialog.show();
    }

    private void updateDateDisplay() {
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, MMM dd, yyyy", Locale.getDefault());
        tvSelectedDate.setText(sdf.format(selectedDate.getTime()));
        updateDaysLeft();
    }

    private void updateDaysLeft() {
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);

        long diff = selectedDate.getTimeInMillis() - today.getTimeInMillis();
        long days = diff / (1000 * 60 * 60 * 24);

        if (days < 0) {
            tvDaysLeft.setText(Math.abs(days) + " days ago");
        } else if (days == 0) {
            tvDaysLeft.setText("Today!");
        } else {
            tvDaysLeft.setText(days + " days left");
        }
    }

    private void saveData() {
        String eventName = etEventName.getText().toString().trim();
        if (eventName.isEmpty()) {
            Toast.makeText(this, "Enter event name", Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("event_name", eventName);
        editor.putLong("target_date", selectedDate.getTimeInMillis());
        editor.apply();

        Toast.makeText(this, "Saved!", Toast.LENGTH_SHORT).show();
        updateAllWidgets();
    }

    private void loadSavedData() {
        String eventName = prefs.getString("event_name", "My Event");
        long targetMillis = prefs.getLong("target_date", System.currentTimeMillis());

        etEventName.setText(eventName);
        selectedDate.setTimeInMillis(targetMillis);
        updateDateDisplay();
    }

    private void updateAllWidgets() {
        Intent intent = new Intent(this, WidgetProvider.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        int[] ids = AppWidgetManager.getInstance(this)
                .getAppWidgetIds(new ComponentName(this, WidgetProvider.class));
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        sendBroadcast(intent);
    }

    private void addWidget() {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        ComponentName provider = new ComponentName(this, WidgetProvider.class);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (appWidgetManager.isRequestPinAppWidgetSupported()) {
                appWidgetManager.requestPinAppWidget(provider, null, null);
                Toast.makeText(this, "Select where to place widget", Toast.LENGTH_SHORT).show();
            } else {
                openWidgetPicker();
            }
        } else {
            openWidgetPicker();
        }
    }

    private void openWidgetPicker() {
        Intent intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_PICK);
        startActivity(intent);
    }
}
