package com.moe.shell;
import java.io.IOException;
import java.io.InputStream;

public class ShellUtil {
    
    public static String exec(String cmd){
        try {
            Process p=Runtime.getRuntime().exec("sh");
            p.getOutputStream().write(cmd.getBytes());
            p.getOutputStream().write("\nexit\n".getBytes());
            p.getOutputStream().flush();
            InputStream in=p.getInputStream();
            StringBuilder sb=new StringBuilder();
            int len;
            byte[] buff=new byte[512];
            while((len=in.read(buff))!=-1){
                sb.append(new String(buff,0,len));
            }
            p.destroy();
            return sb.toString();
        } catch (IOException e) {}
        return null;
    }
    
}
