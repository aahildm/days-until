package com.daysuntil.app;

import android.app.DatePickerDialog;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;

public class WidgetConfigActivity extends AppCompatActivity {

    private int appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    private EditText etConfigEventName;
    private Button btnConfigPickDate, btnConfigSave;
    private Calendar selectedDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.widget_config);

        setResult(RESULT_CANCELED);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
        }
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
            return;
        }

        selectedDate = Calendar.getInstance();
        etConfigEventName = findViewById(R.id.etConfigEventName);
        btnConfigPickDate = findViewById(R.id.btnConfigPickDate);
        btnConfigSave = findViewById(R.id.btnConfigSave);

        btnConfigPickDate.setOnClickListener(v -> {
            DatePickerDialog dialog = new DatePickerDialog(
                    this,
                    (view, year, month, dayOfMonth) -> {
                        selectedDate.set(year, month, dayOfMonth, 0, 0, 0);
                        btnConfigPickDate.setText(
                                new java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
                                        .format(selectedDate.getTime()));
                    },
                    selectedDate.get(Calendar.YEAR),
                    selectedDate.get(Calendar.MONTH),
                    selectedDate.get(Calendar.DAY_OF_MONTH)
            );
            dialog.show();
        });

        btnConfigSave.setOnClickListener(v -> saveAndFinish());
    }

    private void saveAndFinish() {
        String eventName = etConfigEventName.getText().toString().trim();
        if (eventName.isEmpty()) {
            Toast.makeText(this, "Enter event name", Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences prefs = getSharedPreferences("days_until_prefs", MODE_PRIVATE);
        prefs.edit()
                .putString("widget_" + appWidgetId + "_name", eventName)
                .putLong("widget_" + appWidgetId + "_date", selectedDate.getTimeInMillis())
                .apply();

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        WidgetProvider.updateWidget(this, appWidgetManager, appWidgetId);

        Intent resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        setResult(RESULT_OK, resultValue);
        finish();
    }
}
