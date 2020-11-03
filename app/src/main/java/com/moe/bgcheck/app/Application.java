package com.moe.bgcheck.app;
import android.os.StrictMode;

public class Application extends android.app.Application {

    @Override
    public void onCreate() {
        super.onCreate();
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().build());
    }
    
    
    
}
