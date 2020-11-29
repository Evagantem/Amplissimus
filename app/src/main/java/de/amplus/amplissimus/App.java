package de.amplus.amplissimus;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

public class App extends Application {
    public static final String NEW_SUBS_CHANNEL_ID = "subs_channel";

    @Override
    public void onCreate() {
        super.onCreate();
        setupNotificationChannels();
    }

    private void setupNotificationChannels() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel subsChannel = new NotificationChannel(
                    NEW_SUBS_CHANNEL_ID,
                    "Neue Vertretungen",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            subsChannel.setDescription("Benachrichtigt dich für neue Vertretungen.");
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(subsChannel);
        }
    }
}
