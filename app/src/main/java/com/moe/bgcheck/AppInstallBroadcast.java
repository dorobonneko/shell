package com.moe.bgcheck;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.moe.shell.Shell;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.io.FileOutputStream;
import android.os.StrictMode;
import com.moe.bgcheck.model.BlackList;

public class AppInstallBroadcast extends BroadcastReceiver {

    @Override
    public void onReceive(Context p1, Intent p2) {
        StrictMode.allowThreadDiskWrites();
        BlackList bl=BlackList.getInstance();
        Map<String,Map<String,String>> blacklist=bl.getBlackList();
        String packageName = p2.getData().getSchemeSpecificPart();
        switch(p2.getAction()){
            case p2.ACTION_PACKAGE_ADDED:
                Map<String,String> property=new HashMap<>();
                blacklist.put(packageName,property);
                property.put("radical","true");
                bl.notifyAdded(packageName);
                break;
            case p2.ACTION_PACKAGE_REMOVED:
                blacklist.remove(packageName);
                bl.notifyRemoved(packageName);
                break;
        }
          
    }
    
    
    
    
}
