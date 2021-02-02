package com.moe.bgcheck;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import java.io.PrintWriter;
import android.app.NotificationManager;
import android.app.NotificationChannel;
import android.app.Notification;
import android.content.IntentFilter;

public class BootBroadcast extends BroadcastReceiver {

    @Override
    public void onReceive(Context p1, Intent p2) {
        new Thread(){
            public void run() {
                try {
                    java.lang.Process p=Runtime.getRuntime().exec("su");
                    PrintWriter pw=new PrintWriter(p.getOutputStream());
                    pw.println("sh /data/data/com.moe.bgcheck/files/exe.sh");
                    //pw.println("sleep 1s");
                    pw.println("exit");
                    pw.flush();
                    try {
                        p.waitFor();
                    } catch (InterruptedException e) {}
                    pw.close();
					p.destroy();
                } catch (Exception e) {}
            }}.start();
           NotificationManager nm= (NotificationManager)p1.getSystemService(p1.NOTIFICATION_SERVICE);
           NotificationChannel nc=nm.getNotificationChannel("boot");
           if(nc==null){
               nc=new NotificationChannel("boot","boot",NotificationManager.IMPORTANCE_DEFAULT);
           nc.setDescription("开机自动运行");
           nc.setLockscreenVisibility(Notification.VISIBILITY_SECRET);
           nm.createNotificationChannel(nc);
           }
           Notification.Builder nb=new Notification.Builder(p1,"boot");
           nb.setTicker("自动运行");
           nb.setContentTitle("自启");
           nb.setContentText("成功");
           nb.setSmallIcon(R.drawable.ic_launcher);
           nb.setAutoCancel(true);
           nm.notify(0,nb.build());
        IntentFilter intentFilter =new IntentFilter();
        intentFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
        //intentFilter.addAction(Intent.ACTION_PACKAGE_REPLACED);
        intentFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        intentFilter.addDataScheme("package");
        p1.getApplicationContext().registerReceiver(new AppInstallBroadcast(),intentFilter);

            }




}
