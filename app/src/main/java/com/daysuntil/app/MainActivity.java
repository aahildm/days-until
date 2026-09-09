package com.daysuntil.app;

import android.app.DatePickerDialog;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private EditText etEventName;
    private TextView tvSelectedDate, tvDaysLeft;
    private Button btnPickDate, btnSave, btnAddWidget, btn24hr, btn12hr;
    private LinearLayout themeDefault, themeAmoled, themeNavy, themeGlass, themeTransparent;
    private LinearLayout textWhite, textYellow, textCyan, textPink;
    private ScrollView rootScrollView;
    private Calendar selectedDate;
    private SharedPreferences prefs;

    private EditText etEvent2Name;
    private TextView tvSelected2Date, tvDays2Left;
    private Button btnPick2Date;
    private Calendar selectedDate2;

    // Widget bg colors: Dark, Amoled, Navy, Glass, Transparent
    private static final int[] THEME_COLORS = {0xFF1A1A2E, 0xFF000000, 0xFF0D1B2A, 0xAA000000, 0x00000000};
    private static final String[] APP_BG_COLORS = {"#FF1A1A2E","#FF000000","#FF0D1B2A","#FF0A0A0A","#FF0A0A0A"};
    private static final int[] TEXT_COLORS = {0xFFFFFFFF, 0xFFFFE066, 0xFF66FFEE, 0xFFFF80AB};
    private static final String[] TEXT_COLOR_HEX = {"#FFFFFFFF","#FFFFE066","#FF66FFEE","#FFFF80AB"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs = getSharedPreferences("days_until_prefs", MODE_PRIVATE);
        selectedDate = Calendar.getInstance();

        rootScrollView = findViewById(R.id.rootScrollView);
        etEventName = findViewById(R.id.etEventName);
        tvSelectedDate = findViewById(R.id.tvSelectedDate);
        tvDaysLeft = findViewById(R.id.tvDaysLeft);
        btnPickDate = findViewById(R.id.btnPickDate);
        btnSave = findViewById(R.id.btnSave);
        btnAddWidget = findViewById(R.id.btnAddWidget);
        btn24hr = findViewById(R.id.btn24hr);
        btn12hr = findViewById(R.id.btn12hr);
        themeDefault = findViewById(R.id.themeDefault);
        themeAmoled = findViewById(R.id.themeAmoled);
        themeNavy = findViewById(R.id.themeNavy);
        themeGlass = findViewById(R.id.themeGlass);
        themeTransparent = findViewById(R.id.themeTransparent);
        textWhite = findViewById(R.id.textWhite);
        textYellow = findViewById(R.id.textYellow);
        textCyan = findViewById(R.id.textCyan);
        textPink = findViewById(R.id.textPink);

        selectedDate2 = Calendar.getInstance();
        etEvent2Name = findViewById(R.id.etEvent2Name);
        tvSelected2Date = findViewById(R.id.tvSelected2Date);
        tvDays2Left = findViewById(R.id.tvDays2Left);
        btnPick2Date = findViewById(R.id.btnPick2Date);

        btnPick2Date.setOnClickListener(v -> showDatePicker2());

        loadSavedData();
        updateClockFormatUI();
        applyThemeBackground();
        highlightTextColor();

        btnPickDate.setOnClickListener(v -> showDatePicker());
        btnSave.setOnClickListener(v -> saveData());
        btnAddWidget.setOnClickListener(v -> addWidget());
        btn24hr.setOnClickListener(v -> setClockFormat(false));
        btn12hr.setOnClickListener(v -> setClockFormat(true));
        themeDefault.setOnClickListener(v -> applyTheme(0));
        themeAmoled.setOnClickListener(v -> applyTheme(1));
        themeNavy.setOnClickListener(v -> applyTheme(2));
        themeGlass.setOnClickListener(v -> applyTheme(3));
        themeTransparent.setOnClickListener(v -> applyTheme(4));
        textWhite.setOnClickListener(v -> setTextColor(0));
        textYellow.setOnClickListener(v -> setTextColor(1));
        textCyan.setOnClickListener(v -> setTextColor(2));
        textPink.setOnClickListener(v -> setTextColor(3));
    }

    private void setClockFormat(boolean use12hr) {
        prefs.edit().putBoolean("use_12hr", use12hr).apply();
        updateClockFormatUI();
        updateAllWidgets();
    }

    private void updateClockFormatUI() {
        boolean use12hr = prefs.getBoolean("use_12hr", false);
        btn24hr.setAlpha(use12hr ? 0.4f : 1f);
        btn12hr.setAlpha(use12hr ? 1f : 0.4f);
    }

    private void applyTheme(int index) {
        prefs.edit().putInt("theme_index", index).apply();
        applyThemeBackground();
        updateAllWidgets();
    }

    private void applyThemeBackground() {
        int index = prefs.getInt("theme_index", 0);
        rootScrollView.setBackgroundColor(Color.parseColor(APP_BG_COLORS[index]));
        LinearLayout[] swatches = {themeDefault, themeAmoled, themeNavy, themeGlass, themeTransparent};
        for (int i = 0; i < swatches.length; i++) {
            swatches[i].setAlpha(i == index ? 1f : 0.45f);
        }
    }

    private void setTextColor(int index) {
        prefs.edit().putInt("text_color_index", index).apply();
        highlightTextColor();
        updateAllWidgets();
    }

    private void highlightTextColor() {
        int index = prefs.getInt("text_color_index", 0);
        LinearLayout[] swatches = {textWhite, textYellow, textCyan, textPink};
        for (int i = 0; i < swatches.length; i++) {
            swatches[i].setAlpha(i == index ? 1f : 0.45f);
        }
    }

    private void showDatePicker() {
        new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    selectedDate.set(year, month, dayOfMonth, 0, 0, 0);
                    updateDateDisplay();
                },
                selectedDate.get(Calendar.YEAR),
                selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void updateDateDisplay() {
        tvSelectedDate.setText(new SimpleDateFormat("EEE, MMM dd, yyyy", Locale.getDefault())
                .format(selectedDate.getTime()));
        updateDaysLeft();
    }

    private void updateDaysLeft() {
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);
        long days = java.util.concurrent.TimeUnit.MILLISECONDS
                .toDays(selectedDate.getTimeInMillis() - today.getTimeInMillis());
        if (days < 0) tvDaysLeft.setText(Math.abs(days) + " days ago");
        else if (days == 0) tvDaysLeft.setText("Today!");
        else tvDaysLeft.setText(days + " days left");
    }

    private void saveData() {
        String eventName = etEventName.getText().toString().trim();
        if (eventName.isEmpty()) {
            Toast.makeText(this, "Enter event name", Toast.LENGTH_SHORT).show();
            return;
        }
        prefs.edit()
                .putString("event_name", eventName)
                .putLong("target_date", selectedDate.getTimeInMillis())
                .putString("event2_name", etEvent2Name.getText().toString().trim())
                .putLong("event2_date", selectedDate2.getTimeInMillis())
                .apply();
        Toast.makeText(this, "Saved!", Toast.LENGTH_SHORT).show();
        updateAllWidgets();
    }

    private void loadSavedData() {
        etEventName.setText(prefs.getString("event_name", "My Event"));
        selectedDate.setTimeInMillis(prefs.getLong("target_date", System.currentTimeMillis()));
        updateDateDisplay();

        // Load event 2
        String e2name = prefs.getString("event2_name", "");
        long e2date = prefs.getLong("event2_date", 0);
        etEvent2Name.setText(e2name);
        if (e2date > 0) selectedDate2.setTimeInMillis(e2date);
        updateDate2Display();
    }

    private void showDatePicker2() {
        new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    selectedDate2.set(year, month, dayOfMonth, 0, 0, 0);
                    updateDate2Display();
                },
                selectedDate2.get(Calendar.YEAR),
                selectedDate2.get(Calendar.MONTH),
                selectedDate2.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void updateDate2Display() {
        tvSelected2Date.setText(new SimpleDateFormat("EEE, MMM dd, yyyy", Locale.getDefault())
                .format(selectedDate2.getTime()));
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0); today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0); today.set(Calendar.MILLISECOND, 0);
        long days = java.util.concurrent.TimeUnit.MILLISECONDS
                .toDays(selectedDate2.getTimeInMillis() - today.getTimeInMillis());
        if (days < 0) tvDays2Left.setText(Math.abs(days) + " days ago");
        else if (days == 0) tvDays2Left.setText("Today!");
        else tvDays2Left.setText(days + " days left");
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
        AppWidgetManager mgr = AppWidgetManager.getInstance(this);
        ComponentName provider = new ComponentName(this, WidgetProvider.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                && mgr.isRequestPinAppWidgetSupported()) {
            mgr.requestPinAppWidget(provider, null, null);
            Toast.makeText(this, "Select where to place widget", Toast.LENGTH_SHORT).show();
        }
    }
}
