package com.moe.bgcheck.utils;
import java.util.Map;
import android.app.usage.UsageStatsManager;
import java.util.HashMap;
import android.app.usage.UsageEvents;
import java.util.List;
import java.util.ArrayList;
import android.app.usage.UsageStats;
import java.util.Iterator;

public class AppProcess implements Comparable<AppProcess> {

    @Override
    public int compareTo(AppProcess p1) {
        if(p1==null)return 0;
        if(isRunning()){
            if(!p1.isRunning())
                return -1;
        }else{
            if(p1.isRunning())
                return 1;
        }
        return 0;
    }
    
    public String packageName;
    public Process[] process;
    public Service[] services;
    public String state;
    public boolean isRunning(){
        return process!=null&&process.length>0;
    }
    public boolean isForeground(){
        if(process!=null){
            for(Process p:process){
                if(p.foreground)
                    return true;
            }
        }
        return false;
    }
    public static class Service{
        public String className;
    }
    public static class Process{
        public String processName;
        public boolean foreground;
        public boolean shown;
        public boolean cached;
        public boolean empty;
    }
}
