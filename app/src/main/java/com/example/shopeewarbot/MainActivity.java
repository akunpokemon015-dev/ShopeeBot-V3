package com.example.shopeewarbot;

import android.Manifest;
import android.app.*;
import android.content.*;
import android.net.Uri;
import android.os.*;
import android.provider.Settings;
import android.view.*;
import android.widget.*;
import androidx.core.app.ActivityCompat;
import java.text.SimpleDateFormat;
import java.util.*;

public class MainActivity extends Activity {

    EditText link, target, date, time;
    TextView status;
    LinearLayout box;

    static final String PREF = "flash_sale_v3";

    @Override
    public void onCreate(Bundle b) {
        super.onCreate(b);

        getWindow().setStatusBarColor(0xFFEE4D2D);

        box = new LinearLayout(this);
        box.setOrientation(1);
        box.setPadding(28, 28, 28, 28);
        box.setBackgroundColor(0xFFFFF7F4);

        ScrollView scroll = new ScrollView(this);
        scroll.addView(box);
        setContentView(scroll);

        TextView title = new TextView(this);
        title.setText("SHOPEE WAR BOT V3");
        title.setTextSize(25);
        title.setTextColor(0xFFEE4D2D);
        box.addView(title);

        link = field("Link produk Shopee");
        target = field("Target harga");
        date = field("Tanggal YYYY-MM-DD");
        time = field("Jam mulai HH:MM");

        SharedPreferences p = getSharedPreferences(PREF, 0);

        link.setText(p.getString("link", "https://id.shp.ee/ES93SBgp"));
        target.setText(p.getString("target", ""));
        date.setText(p.getString("date", ""));
        time.setText(p.getString("time", ""));

        Button save = button("SIMPAN PENGATURAN");
        Button arm = button("AKTIFKAN MODE SIAGA");
        Button open = button("BUKA PRODUK SHOPEE");

        box.addView(save);
        box.addView(arm);
        box.addView(open);

        status = new TextView(this);
        box.addView(status);

        save.setOnClickListener(v -> saveSettings());
        arm.setOnClickListener(v -> schedule());
        open.setOnClickListener(v -> openProduct());

        if (Build.VERSION.SDK_INT >= 33) {
            ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.POST_NOTIFICATIONS},
                7
            );
        }

        refreshStatus();
    }

    EditText field(String hint) {
        EditText e = new EditText(this);
        e.setSingleLine(true);
        e.setHint(hint);
        box.addView(e);
        return e;
    }

    Button button(String text) {
        Button b = new Button(this);
        b.setText(text);
        return b;
    }

    void saveSettings() {
        getSharedPreferences(PREF, 0).edit()
            .putString("link", link.getText().toString().trim())
            .putString("target", target.getText().toString().trim())
            .putString("date", date.getText().toString().trim())
            .putString("time", time.getText().toString().trim())
            .apply();

        Toast.makeText(this, "Pengaturan tersimpan",
            Toast.LENGTH_SHORT).show();
    }

    void schedule() {
        saveSettings();

        try {
            SimpleDateFormat f =
                new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US);

            f.setLenient(false);

            Date when = f.parse(
                date.getText().toString().trim() + " " +
                time.getText().toString().trim()
            );

            if (when == null ||
                when.getTime() <= System.currentTimeMillis()) {
                throw new Exception();
            }

            AlarmManager am = getSystemService(AlarmManager.class);

            if (Build.VERSION.SDK_INT >= 31 &&
                !am.canScheduleExactAlarms()) {

                Intent settings = new Intent(
                    Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                    Uri.parse("package:" + getPackageName())
                );

                startActivity(settings);
                return;
            }

            Intent i = new Intent(this, FlashSaleReceiver.class);

            PendingIntent pi = PendingIntent.getBroadcast(
                this, 3001, i,
                PendingIntent.FLAG_UPDATE_CURRENT |
                PendingIntent.FLAG_IMMUTABLE
            );

            am.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                when.getTime(),
                pi
            );

            status.setText("MODE SIAGA AKTIF\n" +
                date.getText() + " " + time.getText());

        } catch (Exception e) {
            Toast.makeText(this,
                "Periksa tanggal dan jam",
                Toast.LENGTH_LONG).show();
        }
    }

    void openProduct() {
        String url = link.getText().toString().trim();

        if (url.isEmpty()) return;

        startActivity(new Intent(
            Intent.ACTION_VIEW,
            Uri.parse(url)
        ));
    }

    void refreshStatus() {
        status.setText("Shopee War Bot siap digunakan.");
    }
}
