package startsticky.example.com.stickyservice;

import android.app.ActivityManager;
import android.content.AsyncTaskLoader;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.widget.TextView;

import java.lang.ref.WeakReference;


public class MainActivity extends ActionBarActivity
{
    private TextView textView;
    // ----->
    private MyService mService;
    // ----->
    private boolean mBound;
    private boolean asyncTaskRunning;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        asyncTaskRunning = true;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = (TextView) findViewById(R.id.textView);
        checkServiceRunning();
        startTextViewUpdaterThread();
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        // Bind to Service using service connection
        Intent intent = new Intent(this, MyService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        // unbind Service
        if(mBound)
        {
            unbindService(mConnection);
            mBound = false;
        }
    }

    @Override
    protected void onDestroy()
    {
        asyncTaskRunning = false;
        super.onDestroy();
    }

    // ------------------------------------> Async Task Loader

    private void startTextViewUpdaterThread()
    {
        getLoaderManager().initLoader(0, new Bundle(), new android.app.LoaderManager.LoaderCallbacks<Void>()
        {
            @Override
            public Loader<Void> onCreateLoader(int i, Bundle bundle)
            {
                FooLoader<Void> voidFooLoader = new FooLoader<>(MainActivity.this);
                voidFooLoader.forceLoad();
                return voidFooLoader;
            }

            @Override
            public void onLoadFinished(Loader<Void> loader, Void o)
            {

            }

            @Override
            public void onLoaderReset(Loader<Void> loader)
            {

            }
        });
    }

    static class FooLoader<V> extends AsyncTaskLoader<Void>
    {
        static WeakReference<MainActivity> mActivity;

        public FooLoader(MainActivity mActivity)
        {
            super(mActivity);
            this.mActivity = new WeakReference<>(mActivity);
        }

        public Void loadInBackground()
        {
            final MainActivity mainActivity = mActivity.get();
            if(mainActivity != null)
            {
                while(mainActivity.asyncTaskRunning)
                {
                    Log.d("DEBUG", "Async Task In Activity Running");
                    mainActivity.runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            if(mainActivity.mService != null && mainActivity.textView != null)
                            {
                                mainActivity.textView.setText("Service Counter : " + mainActivity.mService.getCounter());
                            }
                        }
                    });
                }
            }
            return null;
        }

    }

    // ------------------------------------> Service Connection

    /**
     * Defines callbacks for service binding, passed to bindService()
     */
    private ServiceConnection mConnection = new ServiceConnection()
    {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service)
        {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            MyService.MyBinder binder = (MyService.MyBinder) service;
            mService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0)
        {
            mService = null;
            mBound = false;
        }
    };

    // ------------------------------------> Start the service if not running

    private void checkServiceRunning()
    {
        if(!isMyServiceRunning(MyService.class))
        {
            MyService.begin(this);
        }
    }

    private boolean isMyServiceRunning(Class<?> serviceClass)
    {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for(ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE))
        {
            if(serviceClass.getName().equals(service.service.getClassName()))
            {
                return true;
            }
        }
        return false;
    }

}
