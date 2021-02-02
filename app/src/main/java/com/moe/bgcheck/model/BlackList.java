package com.moe.bgcheck.model;
import com.moe.bgcheck.LaunchActivity;
import com.moe.shell.Shell;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Map;

public class BlackList {
    private static BlackList object;
    private Map<String,Map<String,String>> blacklist;
    private Callback call;
    private BlackList(){}
    public static BlackList getInstance(){
        if(object==null){
            synchronized(BlackList.class){
                if(object==null)
                    object=new BlackList();
            }
        }
        return object;
    }
    public synchronized Map<String,Map<String,String>> getBlackList(){
        if(blacklist==null)
            blacklist=Shell.getBlackList();
            return blacklist;
    }
    public void notifyChanged(String packageName){
        if(call!=null)
            call.onChanged(true,packageName);
        save();
    }
    public void notifyAdded(String packageName){
        if(call!=null)
            call.onChanged(false,packageName);
            save();
    }
    public void notifyRemoved(String packageName){
        if(call!=null)
            call.onChanged(false,packageName);
            save();
    }
    void save(){
        try
        {
            File dir=new File("/data/data/com.moe.bgcheck/files");
            if(!dir.exists())
                dir.mkdirs();
            PrintWriter pw=new PrintWriter(new File(dir,"forcestop"));
            Iterator<Map.Entry<String,Map<String,String>>> i=blacklist.entrySet().iterator();
            while(i.hasNext()){
                Map.Entry<String,Map<String,String>> entry=i.next();
                pw.print(entry.getKey());
                Map<String,String> property=entry.getValue();
                if(property!=null){
                    pw.print(":");
                    Iterator<Map.Entry<String,String>> iterator=property.entrySet().iterator();
                    while(iterator.hasNext()){
                        Map.Entry<String,String> property_item=iterator.next();
                        pw.print(property_item.getKey());
                        pw.print("=");
                        pw.print(property_item.getValue());
                        if(iterator.hasNext())
                            pw.print(",");
                    }
                }
                pw.println();
            }
            pw.flush();
            pw.close();
        }
        catch (IOException e)
        {}
	}
    public void setCallback(Callback c){
        call=c;
    }
    public static interface Callback{
        void onChanged(boolean added,String packageName);
    }
}
