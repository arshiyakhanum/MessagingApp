package com.arshiya.messagingapp.services;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.arshiya.messagingapp.ConversationsList;
import com.arshiya.messagingapp.R;

/**
 * Created by arshiya on 15/5/16.
 */
public class MessageNotificationService extends IntentService {

    private static final String TAG = MessageNotificationService.class.getSimpleName();
    private static int mNotificationsCount;
    private NotificationCompat.Builder mBuilder;

    public MessageNotificationService(){
        super("super");
    }
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public MessageNotificationService(String name) {
        super(name);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mBuilder = new NotificationCompat.Builder(this);

    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "onHandleIntent()");
        int notifId = 1;

        mNotificationsCount++;

        //get intent data from IncomingSmsReceiver
        String title = intent.getStringExtra("address");
        String content = intent.getStringExtra("body");
        long date = intent.getLongExtra("date_sent", 0);
        String ticker = "New message from " + title;

        //build a notification
        mBuilder.setContentTitle(title)
                .setContentText(content)
                .setSmallIcon(R.drawable.app_icon)
                .setPriority(Notification.PRIORITY_DEFAULT)
                .setTicker(ticker);

        //explicit intent for activity
        Intent resultIntent = new Intent(this, ConversationsList.class);

        //create a task stack builder
        TaskStackBuilder  taskStackBuilder = TaskStackBuilder.create(this);

        //add activity and next intent to task stack builder
        taskStackBuilder.addParentStack(ConversationsList.class);
        taskStackBuilder.addNextIntent(resultIntent);

        PendingIntent  pendingIntent = taskStackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        mBuilder.setContentIntent(pendingIntent);
        mBuilder.setAutoCancel(true);
        mBuilder.setNumber(mNotificationsCount);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(notifId, mBuilder.build());
    }
}
