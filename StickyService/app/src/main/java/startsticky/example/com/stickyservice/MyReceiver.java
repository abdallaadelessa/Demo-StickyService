package startsticky.example.com.stickyservice;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class MyReceiver extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent)
    {
        String action = intent.getAction();
        if(action.equals("android.intent.action.BOOT_COMPLETED"))
        {
            MyService.begin(context);
        }
    }


}
