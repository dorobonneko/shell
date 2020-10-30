package com.moe.bgcheck.utils;
import java.util.Map;
import android.content.pm.PackageInfo;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Collector;
import java.net.Socket;
import java.net.InetSocketAddress;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.function.BinaryOperator;
import java.util.function.Predicate;

public class ProcessUtils {

    public static Map<String,AppProcess> getProcess(List<PackageInfo> list) {
        list = list.stream().filter(new Predicate<PackageInfo>(){

                @Override
                public boolean test(PackageInfo p1) {
                    if(p1==null)
                    return false;
                    return true;
                }
            }).collect(Collectors.toList());
       return list.stream() .map(new Function<PackageInfo,AppProcess>(){

                @Override
                public AppProcess apply(PackageInfo p1) {
                    if(p1==null)return null;
                    return getProcess(p1.packageName);
                }
            }).filter(new Predicate<AppProcess>(){

                @Override
                public boolean test(AppProcess p1) {
                    if(p1==null)
                    return false;
                    return true;
                }
            }).collect(Collectors.toMap(new Function<AppProcess,String>(){

                               @Override
                               public String apply(AppProcess p1) {
                                   if(p1==null)return null;
                                   return p1.packageName;
                               }
                           }, new Function<AppProcess,AppProcess>(){

                               @Override
                               public AppProcess apply(AppProcess p1) {
                                   return p1;
                               }
                      }, new BinaryOperator<AppProcess>(){

                          @Override
                          public AppProcess apply(AppProcess p1, AppProcess p2) {
                              return p2;
                          }
                      }));
    }
    public static AppProcess getProcess(String packageName) {
        AppProcess ap=new AppProcess();
        ap.packageName = packageName;
        java.lang.Process exec=null;
        try {
            exec = Runtime.getRuntime().exec("sh");
            PrintWriter pw=new PrintWriter(exec.getOutputStream());
            pw.println("dumpsys activity p " + packageName);
            pw.println("dumpsys activity s " + packageName);
            pw.println("exit");
            pw.flush();
            BufferedReader br=new BufferedReader(new InputStreamReader(exec.getInputStream()));
            String line=null;
            StringBuilder sb=new StringBuilder();
            boolean process=false,service=false;
            List<AppProcess.Process> processes=new ArrayList<>();
            List<AppProcess.Service> services=new ArrayList<>();

            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (process) {
                    if (line.startsWith("*APP*")) {
                        if (sb.length() > 0) {
                            processes.add(readProcess(sb.toString()));
                            sb.setLength(0);
                        }
                        sb.append(line).append(" ");
                    } else if (line.isEmpty()) {
                        if (sb.length() > 0) {
                            processes.add(readProcess(sb.toString()));
                            sb.setLength(0);
                            process = false;
                        }
                    } else if (sb.length() > 0) {
                        sb.append(line).append(" ");
                    }
                } else if (service) {
                    //读取服务
                    if (line.startsWith("* ServiceRecord")) {
                        if (sb.length() > 0) {
                            services.add(readService(sb.toString()));
                            sb.setLength(0);
                        }
                        sb.append(line).append(" ");
                    } else if (line.isEmpty()) {
                        if (sb.length() > 0) {
                            services.add(readService(sb.toString()));
                            sb.setLength(0);
                            service = false;
                        }
                    } else if (sb.length() > 0) {
                        sb.append(line).append(" ");
                    }
                } else {
                    if (line.startsWith("ACTIVITY MANAGER SERVICES"))
                        service = true;
                    else if (line.startsWith("ACTIVITY MANAGER RUNNING PROCESSES"))
                        process = true;
                }
            }
            ap.process = processes.toArray(new AppProcess.Process[0]);
            ap.services = services.toArray(new AppProcess.Service[0]);
        } catch (IOException e) {} finally {try {
                exec.destroy();
            } catch (Exception e) {}}
        return ap;
    }
    public static AppProcess.Process readProcess(String str) {
        AppProcess.Process p=new AppProcess.Process();
        int index=str.indexOf(":");
        p.processName = str.substring(index + 1, str.indexOf("/", index + 1));
        index = str.indexOf("cached=");
        if (index != -1) {
            p.cached = Boolean.valueOf(str.substring(index + 7, str.indexOf(" ", index + 7)));
        }
        index = str.indexOf("hasShownUi=");
        if (index != -1) {
            p.shown = Boolean.valueOf(str.substring(index + 11, str.indexOf(" ", index + 11)));
        }
        index = str.indexOf("foregroundActivities=");
        if (index != -1) {
            p.foreground = Boolean.valueOf(str.substring(index + 21, str.indexOf(" ", index + 21)));
        }
        index = str.indexOf("empty=");
        if (index != -1) {
            p.empty = Boolean.valueOf(str.substring(index + 6, str.indexOf(" ", index + 6)));
        }
        return p;
    }
    public static AppProcess.Service readService(String str) {
        AppProcess.Service s=new AppProcess.Service();
        int index=str.indexOf("intent=");
        if (index != -1) {
            String intent=str.substring(index + 8, str.indexOf("}", index + 8));
            for (String item:intent.split(" ")) {
                if (item.startsWith("cmp=")) {
                    s.className = item.substring(4);
                }
            }
        }
        return s;
    }
}
