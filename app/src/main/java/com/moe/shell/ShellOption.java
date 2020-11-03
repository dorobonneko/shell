package com.moe.shell;
import java.net.Socket;
import java.net.InetSocketAddress;
import java.io.IOException;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.SocketException;

public class ShellOption {
    Socket socket;
    DataInputStream dis;
    DataOutputStream dos;
    Callback call;
    public ShellOption(){
        
    }
    protected void connect() throws IOException{
        if(socket==null){
            socket=new Socket();
            conn();
            }
        else if(!socket.isConnected()){
            socket=new Socket();
            conn();
             }else{
               try{
                   dos.writeUTF("heart");
               dos.flush();
               }catch(SocketException s){
                   try{
                       socket.close();
                       }catch(Exception ee){}
                       socket=new Socket();
                       try{
                           conn();
                           }catch(IOException eee){
                               if(call!=null)
                                   call.onCloseShell();
                               throw eee;
                           }
                   
               }
             }
    }
    void conn() throws IOException{
        socket.connect(new InetSocketAddress(3335));
        dis=new DataInputStream(socket.getInputStream());
        dos=new DataOutputStream(socket.getOutputStream());
        
    }
    public void reboot() throws IOException{
        connect();
        dos.writeUTF("reboot");
        dos.flush();
    }
    public void exit() throws IOException{
        connect();
        dos.writeUTF("exit");
        dos.flush();
    }
    public boolean isRunning(){
        try {
            connect();
            dos.writeUTF("heart");
            return true;
        } catch (IOException e) {
            return false;
        }
    }
    public int getUid() throws IOException{
        connect();
        dos.writeUTF("getUid");
        dos.flush();
       return dis.readInt();
    }
    public int getGid() throws IOException{
        connect();
        dos.writeUTF("getGid");
        dos.flush();
        return dis.readInt();
    }
    public int getPort() throws IOException{
        connect();
        dos.writeUTF("getPort");
        dos.flush();
        return dis.readInt();
    }
    public String exec(String cmd) throws IOException{
        connect();
        dos.writeUTF("exec");
        dos.writeUTF(cmd);
        dos.flush();
        StringBuilder sb=new StringBuilder();
        int len=-1;
        byte[] buff=new byte[4096];
        do{
            len=dis.readInt();
            if(len<=0)break;
            if(len>4096)throw new IllegalArgumentException("分片大小不可>4096");
            dis.readFully(buff,0,len);
            sb.append(new String(buff,0,len));
        }while(true);
        return sb.toString();
    }
    public void close() throws IOException{
        if(socket!=null)
        socket.close();
    }
    public void setCallback(Callback c){
        call=c;
    }
    public interface Callback{
        void onCloseShell();
    }
}
