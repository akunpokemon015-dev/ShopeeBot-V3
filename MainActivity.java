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
    static final String PREF = "flash_sale_v3";
    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        getWindow().setStatusBarColor(0xFFEE4D2D);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(1); root.setPadding(28,28,28,28);
        root.setBackgroundColor(0xFFFFF7F4);
        ScrollView scroll = new ScrollView(this);
        LinearLayout box = root;
        TextView title = new TextView(this); title.setText("Shopee War Bot V3"); title.setTextSize(25); title.setTextColor(0xFFEE4D2D); title.setPadding(0,0,0,18); box.addView(title);
        TextView desc = new TextView(this); desc.setText("Mode Siaga Flash Sale • Alarm waktu + notifikasi cepat"); desc.setTextSize(14); box.addView(desc);
        link = field("Link produk Shopee"); target = field("Target harga (opsional)");
        date = field("Tanggal (YYYY-MM-DD)"); time = field("Jam mulai (HH:MM)");
        SharedPreferences p = getSharedPreferences(PREF,0);
        link.setText(p.getString("link","https://id.shp.ee/ES93SBgp"));
        target.setText(p.getString("target",""));
        date.setText(p.getString("date",""));
        time.setText(p.getString("time",""));
        Button save = button("SIMPAN PENGATURAN"); box.addView(save);
        Button arm = button("AKTIFKAN MODE SIAGA"); box.addView(arm);
        Button open = button("BUKA PRODUK SHOPEE SEKARANG"); box.addView(open);
        status = new TextView(this); status.setTextSize(15); status.setPadding(0,20,0,0); box.addView(status);
        TextView note = new TextView(this);
        note.setPadding(0,20,0,0); note.setTextSize(13);
        note.setText("Catatan: Android 15 membatasi pembukaan aplikasi otomatis dari latar belakang. Pada waktu flash sale, V3 mengirim notifikasi; ketuk notifikasi untuk membuka produk. Pembelian dan pembayaran tetap manual.");
        box.addView(note);
        scroll.addView(root); setContentView(scroll);
        save.setOnClickListener(v -> saveSettings());
        arm.setOnClickListener(v -> schedule());
        open.setOnClickListener(v -> openProduct());
        if (Build.VERSION.SDK_INT >= 33) ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.POST_NOTIFICATIONS},7);
        refreshStatus();
    }
    EditText field(String hint) { EditText e = new EditText(this); e.setSingleLine(true); e.setHint(hint); e.setTextSize(15); rootAdd(e); return e; }
    void rootAdd(View v) { ((LinearLayout)((ScrollView)findViewById(android.R.id.content).getChildAt(0)).getChildAt(0)).addView(v); }
    Button button(String s) { Button b = new Button(this); b.setText(s); return b; }
    void saveSettings() {
        getSharedPreferences(PREF,0).edit().putString("link",link.getText().toString().trim())
        .putString("target",target.getText().toString().trim()).putString("date",date.getText().toString().trim())
        .putString("time",time.getText().toString().trim()).apply();
        Toast.makeText(this,"Pengaturan tersimpan",Toast.LENGTH_SHORT).show();
    }
    void schedule() {
        saveSettings();
        try {
            String d=date.getText().toString().trim(), t=time.getText().toString().trim();
            java.text.SimpleDateFormat f=new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm",Locale.US);
            f.setLenient(false);
            Date when=f.parse(d+" "+t);
            if(when==null || when.getTime()<=System.currentTimeMillis()) throw new Exception();
            if(Build.VERSION.SDK_INT>=31 && !getSystemService(AlarmManager.class).canScheduleExactAlarms()) {
                new AlertDialog.Builder(this).setTitle("Izinkan alarm tepat waktu")
                .setMessage("Aktifkan izin Alarm & pengingat untuk jadwal flash sale yang lebih tepat.")
                .setPositiveButton("BUKA PENGATURAN",(a,b)->startActivity(new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:"+getPackageName()))))
                .setNegativeButton("Batal",null).show(); return;
            }
            Intent i=new Intent(this,FlashSaleReceiver.class);
            PendingIntent pi=PendingIntent.getBroadcast(this,3001,i,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
            AlarmManager am=getSystemService(AlarmManager.class);
            if(Build.VERSION.SDK_INT>=23) am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,when.getTime(),pi);
            else am.setExact(AlarmManager.RTC_WAKEUP,when.getTime(),pi);
            status.setText("MODE SIAGA AKTIF\nJadwal: "+d+" "+t+"\nNotifikasi akan dikirim pada waktu tersebut.");
            Toast.makeText(this,"Mode Siaga aktif",Toast.LENGTH_LONG).show();
        } catch(Exception e) {
            new AlertDialog.Builder(this).setMessage("Periksa tanggal dan jam. Gunakan format YYYY-MM-DD dan HH:MM, serta jadwal harus di masa depan.").setPositiveButton("OK",null).show();
        }
    }
    void openProduct() {
        String u=link.getText().toString().trim();
        if(u.isEmpty()) { Toast.makeText(this,"Masukkan link produk",Toast.LENGTH_SHORT).show(); return; }
        try { startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(u))); }
        catch(Exception e) { Toast.makeText(this,"Tidak bisa membuka link",Toast.LENGTH_SHORT).show(); }
    }
    void refreshStatus() {
        SharedPreferences p=getSharedPreferences(PREF,0);
        if(!p.getString("date","").isEmpty()) status.setText("Jadwal tersimpan: "+p.getString("date","")+" "+p.getString("time","")+"\nTekan AKTIFKAN MODE SIAGA untuk menjadwalkan alarm.");
    }
}
