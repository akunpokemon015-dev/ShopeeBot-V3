package com.example.shopeewarbot;

import android.app.*;
import android.content.*;
import android.net.Uri;
import android.os.Build;

public class FlashSaleReceiver extends BroadcastReceiver {

private static final String CHANNEL_ID = "shopee_flash_sale";

@Override
public void onReceive(Context context, Intent intent) {

    createChannel(context);

    String link = context.getSharedPreferences(
        "flash_sale_v3", 0
    ).getString("link", "https://shopee.co.id");

    Intent open = new Intent(
        Intent.ACTION_VIEW,
        Uri.parse(link)
    );

    PendingIntent pending = PendingIntent.getActivity(
        context,
        3002,
        open,
        PendingIntent.FLAG_UPDATE_CURRENT |
        PendingIntent.FLAG_IMMUTABLE
    );

    Notification.Builder builder;

    if (Build.VERSION.SDK_INT >= 26) {
        builder = new Notification.Builder(context, CHANNEL_ID);
    } else {
        builder = new Notification.Builder(context);
    }

    Notification notification = builder
        .setSmallIcon(android.R.drawable.ic_dialog_info)
        .setContentTitle("SHOPEE FLASH SALE!")
        .setContentText("Promo dimulai! Ketuk untuk membuka produk.")
        .setContentIntent(pending)
        .setAutoCancel(true)
        .setPriority(Notification.PRIORITY_HIGH)
        .build();

    NotificationManager manager =
        (NotificationManager) context.getSystemService(
            Context.NOTIFICATION_SERVICE
        );

    manager.notify(3003, notification);
}

private void createChannel(Context context) {

    if (Build.VERSION.SDK_INT >= 26) {

        NotificationChannel channel =
            new NotificationChannel(
                CHANNEL_ID,
                "Shopee Flash Sale",
                NotificationManager.IMPORTANCE_HIGH
            );

        NotificationManager manager =
            (NotificationManager) context.getSystemService(
                Context.NOTIFICATION_SERVICE
            );

        manager.createNotificationChannel(channel);
    }
}

}
