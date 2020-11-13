package com.moe.shell;
import java.net.Socket;
import java.io.DataOutputStream;
import java.net.InetSocketAddress;
import java.io.IOException;
import java.net.SocketException;
import android.os.Handler;
import android.os.Looper;

public class Heart implements AutoCloseable {
    Callback call;
    Socket socket;
    DataOutputStream dos;
    public Heart(Callback call){
        this.call=call;
        socket=new Socket();
        
    }
    public boolean test(){
        try {
            if (socket.isConnected()) {
                dos.writeUTF("heart");
            } else {
                socket.connect(new InetSocketAddress(3335));
                dos=new DataOutputStream(socket.getOutputStream());
                new Thread(new Runnable(){
                    public void run(){
                        if(socket.isConnected()){
                            try {
                                while (socket.getInputStream().read() != -1){
                                    try {
                                        Thread.sleep(1000);
                                    } catch (InterruptedException e) {}
                                }
                            } catch (IOException e) {
                                try {
                                    socket.close();
                                } catch (IOException ee) {
                                    socket=new Socket();
                                }
                                
                            }finally{
                                new Handler(Looper.getMainLooper()).post(new Runnable(){
                                    public void run(){
                                call.onDisConnected();
                                }
                                });
                            }

                        }
                    }
                }).start();
            }
            call.onConnected();
            return true;
        } catch (IOException e) {
            try {
                socket.close();
            } catch (IOException ee) {}
            socket = new Socket();
            call.onDisConnected();
        }
        return false;
    }

    @Override
    public void close() throws Exception {
        socket.close();
    }

    
    public interface Callback{
        void onConnected();
        void onDisConnected();
    }
    
}
