package com.moe.shell;
interface Service{
 int getUid();
 String exec(String cmd);
 void kill(String packageName);
 void forceStop(String packageName);
 void setInactive(String packageName);
 void disable(String packageName);
 void enable(String packageName);
 void suspend(String packageName);
 void unsuspend(String packageNmae);
 int getPid();
 void kill(int pid);
 void exit();
}
