package startsticky.example.com.stickyservice;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class MyService extends Service
{
    private static final int NOTIFICATION_ID = 12;
    public static final String TAG = "DEBUG";
    // ----->
    private final IBinder mBinder = new MyBinder();
    private Thread thread;
    private int threadNum;
    private int counter;

    public MyService()
    {
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return mBinder;
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
        addNotification();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        checkComingData();
        return START_STICKY;
    }

    // ---------------------------------->

    private void checkComingData()
    {
        if(thread == null)
        {
            threadNum++;
            thread = new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    while(true)
                    {
                        SystemClock.sleep(2000);
                        counter++;
                        Log.d(TAG, "Service Running " + threadNum);
                    }
                }
            });
            thread.start();
        }
    }

    // ---------------------------------->

    public static void begin(Context context)
    {
        Intent service = new Intent(context, MyService.class);
        context.startService(service);
    }

    private void addNotification()
    {
        Log.d(TAG, "Service Started");
        // create the notification
        CharSequence title = "Title";
        CharSequence content = "Description";
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setContentTitle(title);
        builder.setContentText(content);
        builder.setOngoing(true);

        // create the pending intent and add to the notification
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        builder.setContentIntent(pendingIntent);

        // send the notification
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    public int getCounter()
    {
        return counter;
    }

    // ---------------------------------->

    public class MyBinder extends Binder
    {
        public MyService getService()
        {
            return MyService.this;
        }
    }
}
