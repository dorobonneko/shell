package com.moe.bgcheck.beam;
import android.content.pm.ApplicationInfo;

public class BootInfo {
    public final static int TYPE_ACTIVITY=1,TYPE_RECIVER=2,TYPE_SERVICE=3,TYPE_PROVIDER=4;
    public String packageName,className;
    public ApplicationInfo info;
    public int type=2;
    
}
