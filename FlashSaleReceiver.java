package com.example.shopeewarbot;

import android.app.*;
import android.content.*;
import android.net.Uri;
import android.os.Build;

public class FlashSaleReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context c, Intent in) {
        android.content.SharedPreferences p=c.getSharedPreferences("flash_sale_v3",0);
        String url=p.getString("link","https://id.shp.ee/ES93SBgp");
        String target=p.getString("target","");
        String channel="flash_sale_alerts";
        NotificationManager nm=(NotificationManager)c.getSystemService(Context.NOTIFICATION_SERVICE);
        if(Build.VERSION.SDK_INT>=26) nm.createNotificationChannel(new NotificationChannel(channel,"Pengingat Flash Sale",NotificationManager.IMPORTANCE_HIGH));
        Intent view=new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        PendingIntent pi=PendingIntent.getActivity(c,3002,view,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        Notification.Builder b=Build.VERSION.SDK_INT>=26?new Notification.Builder(c,channel):new Notification.Builder(c);
        b.setSmallIcon(android.R.drawable.ic_dialog_info).setContentTitle("Flash Sale dimulai!")
         .setContentText(target.isEmpty()?"Ketuk untuk membuka produk Shopee":"Cek harga promo (target: "+target+")")
         .setAutoCancel(true).setContentIntent(pi).setPriority(Notification.PRIORITY_MAX);
        nm.notify(3003,b.build());
    }
}
