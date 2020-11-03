package com.moe.shell;

import android.os.Looper;
import android.os.RemoteException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.ServerSocket;

public class Shell implements Thread.UncaughtExceptionHandler 
{

   
    
	/*am set-standby-bucket packagename active|working_set|frequent|rare
	*/
	private PrintWriter exec;
	private ThreadPoolExecutor tpe;
	private ProcessBuilder mProcessBuilder;
    private ServerSocket ssc;
	public static void main(String[] args){
		if(android.os.Process.myUid()>2000){
			System.out.println("权限不足！");
			System.exit(1);
		}else{
			Looper.prepare();
			new Shell();
			Looper.loop();
		}
	}
    
    public void reboot() {
        try {
            ssc.close();
        } catch (IOException e) {}
        exec.println("sh /data/data/com.moe.bgcheck/files/exe.sh");
        exec.flush();
        System.out.println("系统重启");
    }

    
    public int getUid() {
        return android.os.Process.myUid();
    }

    
    public int getGid() {
        return android.os.Process.myPid();
    }

    
    public String exec(String cmd) {
        try {
           java.lang.Process p= mProcessBuilder.start();
           p.getOutputStream().write(cmd.getBytes());
           p.getOutputStream().write("\nexit\n".getBytes());
           p.getOutputStream().flush();
           InputStream in=p.getInputStream();
           int len=-1;
           byte[] buff=new byte[512];
           StringBuilder sb=new StringBuilder();
           while((len=in.read(buff))!=-1){
               sb.append(new String(buff,0,len));
           }
           p.destroy();
           return sb.toString();
        } catch (IOException e) {}
        return null;
    }

    
    public int getPort() {
        return 3335;
    }

    
    public void exit() {
        exec.println("exit");
        exec.flush();
        exec.close();
        tpe.shutdown();
        System.exit(0);
    }

    @Override
    public void uncaughtException(Thread p1, Throwable p2) {
        System.out.println(p2.getMessage());
        for(StackTraceElement e:p2.getStackTrace())
        System.out.println(e.toString());
    }


    
	Shell(){
       Thread.setDefaultUncaughtExceptionHandler(this);
        try{
            java.lang.Process process=Runtime.getRuntime().exec("sh");
            exec=new PrintWriter(process.getOutputStream());
            exec.println("pm grant com.moe.bgcheck android.permission.PACKAGE_USAGE_STATS");
            exec.println("pm grant com.moe.bgcheck android.permission.DUMP");
            exec.flush();
			ScheduledExecutorService service = Executors
				.newSingleThreadScheduledExecutor();
				tpe=new ThreadPoolExecutor(4,32,5,TimeUnit.SECONDS,new ArrayBlockingQueue<Runnable>(255));
			service.scheduleAtFixedRate(new Runnable(){

					@Override
					public void run()
					{
						kill();
						//System.exit(0);
					}
				}, 1, 5, TimeUnit.MINUTES);
			mProcessBuilder=new ProcessBuilder();
			mProcessBuilder.directory(new File("/sdcard"));
			mProcessBuilder.redirectErrorStream(true);
			mProcessBuilder.command("sh");
			ssc=new ServerSocket();
            ssc.bind(new InetSocketAddress(3335));
			new Thread(){
				public void run(){
					
                    try {
                        while(!ssc.isClosed()){
                            Socket s=ssc.accept();
                            new Thread(new Op_Read(s)).start();
                        }
                    } catch (Exception e) {
                        uncaughtException(Thread.currentThread(),e);
                    }

				}}.start();
			System.out.println("启动成功");
		}
		catch (Exception e)
		{
			System.exit(2);
			System.out.println(e.getMessage());
		}
	}
	class Op_Read implements Runnable{
		Socket mSelectionKey;
		Op_Read(Socket mSelector){
			this.mSelectionKey=mSelector;
		}

		@Override
		public void run()
		{
            System.out.println("read");
			Socket s=mSelectionKey;
            try {
                DataInputStream dis=new DataInputStream(s.getInputStream());
               DataOutputStream dos=new DataOutputStream(s.getOutputStream());
                while(s.isConnected()){
                    String cmd=dis.readUTF();
                    System.out.println(cmd);
                    switch(cmd){
                    case "exit":
                        exit();
                        break;
                    case "getUid":
                        dos.writeInt(getUid());
                        break;
                    case "getGid":
                        dos.writeInt(getGid());
                        break;
                    case "reboot":
                        reboot();
                        break;
                    case "getPort":
                        dos.writeInt(getPort());
                        break;
                    case "exec":
                        System.out.println("exec cmd");
                        java.lang.Process p=mProcessBuilder.start();
                        OutputStream cmdout=p.getOutputStream();
                        cmdout.write(dis.readUTF().getBytes());
                        cmdout.write("\nexit\n".getBytes());
                        cmdout.flush();
                        InputStream cmdin=p.getInputStream();
                        byte[] buff=new byte[4096];
                        int len=-1;
                        while((len=cmdin.read(buff))!=-1){
                            dos.writeInt(len);
                            dos.write(buff,0,len);
                        }
                        dos.writeInt(0);
                        p.destroy();
                        System.out.println("exec cmd end");
                            
                         break;
                      case "heart":
                          //心跳
                          break;
                }
                dos.flush();
                }
            } catch (IOException e) {
                
            } finally {
                try {
                    s.close();
                } catch (IOException ee) {}
                System.out.println("close");
            }
			/*ByteBuffer echoBuffer=ByteBuffer.allocate(4096);
			java.lang.Process p=(java.lang.Process) mSelectionKey.attachment();
			OutputStream cmdout=p.getOutputStream();
			byte[] buff=new byte[4096];
			int len=-1;
			try
			{
				System.out.println("开始写入");
				while ((sc.read(echoBuffer)) > 0)
				{ 
					echoBuffer.flip();
					echoBuffer.get(buff,0,echoBuffer.limit());
					cmdout.write(buff,0,echoBuffer.limit());
					echoBuffer.clear();
				}
				cmdout.write("\nexit\n".getBytes());
				cmdout.flush();
				System.out.println("开始读取");
				InputStream cmdin=p.getInputStream();
				
					while ((len = cmdin.read(buff)) != -1)
					{
						echoBuffer.put(buff,0,len);
						echoBuffer.flip();
						echoBuffer.limit();
						sc.write(echoBuffer);
						echoBuffer.clear();
					}
				System.out.println("结束");
			}
			catch (IOException e)
			{}finally{
				try
				{
					p.destroy();
					sc.close();
				}
				catch (IOException e)
				{}
				/*try
				{
					SelectionKey key=sc.register(mSelectionKey.selector(), SelectionKey.OP_WRITE);
					key.attach(p);
				}
				catch (ClosedChannelException e)
				{}
                
			}*/
		}
	}
 
	
	private void kill(){
			try{
				Set<String> recents=getRecents();
				Map<String,Map<String,String>> blacklist=getBlackList();
			for(String packageName:blacklist.keySet()){
				if(recents.contains(packageName)){
						//standby(packageName);
					continue;
					}
					Map<String,String> property=blacklist.get(packageName);
					if(property==null)
					kill(packageName,false);
					else{
						String radical=property.get("radical");
						if(radical==null)
							kill(packageName,false);
							else if(radical.equals("true"))
								kill(packageName,true);
								else
								kill(packageName,false);
					}
			}
				exec.println("dumpsys activity -a s|grep \"* ServiceRecord{\"|awk -F '[ /]+' '{cmd=\"am set-inactive \"$5\" true\";system(cmd)}'");
				exec.flush();
			}catch(Exception e){
				System.out.println(e.getMessage());
			}
		}
		
		
	
	public static Set<String> getRecents(){
		Set<String> list=new HashSet<>();
		try
		{
			java.lang.Process process=Runtime.getRuntime().exec("sh");
			PrintWriter out=new PrintWriter(process.getOutputStream());
			//out.write("dumpsys activity r|grep -e \"* Recent #\" -e baseIntent\n".getBytes());
			out.println("dumpsys activity r|grep Activities");
			out.println("exit");
			out.flush();
			
			BufferedReader br=new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line=null;
			while((line=br.readLine())!=null){
				int start=line.indexOf("[")+1;
				int end=line.lastIndexOf("]");
				if(start==end)continue;
				line=line.substring(start,end);
				for(String item:line.split(",")){
					item=item.trim().split(" ")[2];
					list.add(item.substring(0,item.indexOf("/")));
				}
			}
			br.close();
			process.destroy();
		}
		catch (Exception e)
		{
			System.out.println(e.getMessage());
			for(StackTraceElement ee:e.getStackTrace())
			System.out.println(ee.toString());
		}
		System.out.print("当前 Recents:");
		System.out.println(list.toString());
		return list;
	}
	/*public static List<String> getProcess(){
		List<String> list=new ArrayList<>();
		try
		{
			java.lang.Process process=Runtime.getRuntime().exec("sh");
			OutputStream out=process.getOutputStream();
			out.write("dumpsys activity -a p|grep *APP*\n".getBytes());
			out.flush();
			out.write("exit\n".getBytes());
			out.flush();
			
			BufferedReader br=new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line=null;
			while((line=br.readLine())!=null){
				line=line.trim();
				if(line.startsWith("*APP*")){
					Matcher m=processPattern.matcher(line);
					if(m.find()){
						if(Integer.parseInt(m.group(1))>10000){
							String processName=m.group(2);
							if(processName.contains(":"))
								list.add(processName.split(":")[0]);
								else
								list.add(processName);
							}
					}
				}
					
			}
			br.close();
			process.destroy();
		}
		catch (Exception e)
		{
			System.out.println(e.getMessage());
		}
		System.out.println("Process:");
		System.out.println(list.toString());
		return list;
	}*/
	public void kill(String packageName,boolean radical){
		try
		{
			
			if(radical)
				exec.println("am force-stop "+packageName);
				else
			exec.println("am kill "+packageName);
			//exec.println("dumpsys deviceidle whilelist -"+packageName);//移出未优化白名单
			//exec.println("am set-inactive "+packageName+" true");//使app进入standby模式
			exec.flush();
			System.out.println("kill "+packageName);
		}
		catch (Exception e)
		{
			System.out.println(e.getMessage());
		}
	}
	public void standby(String packageName){
		try
		{
			if(exec==null){
				java.lang.Process process=Runtime.getRuntime().exec("sh");
				exec=new PrintWriter(process.getOutputStream());
			}
			exec.println("dumpsys deviceidle whilelist -"+packageName);//移出未优化白名单
			exec.println("am set-inactive "+packageName+" true");//使app进入standby模式
			exec.flush();
		}
		catch (Exception e)
		{
			System.out.println(e.getMessage());
		}
	}
	public static Map<String,Map<String,String>> getBlackList(){
		Map<String,Map<String,String>> list=new HashMap<>();
		try
		{
			FileReader fr=new FileReader("/data/data/com.moe.bgcheck/files/forcestop");
			BufferedReader br=new BufferedReader(fr);
			String line=null;
			while((line=br.readLine())!=null){
				line=line.trim();
				if(!line.isEmpty()){
					int index=line.indexOf(":");
					if(index==-1)
					list.put(line,null);
					else{
						Map<String,String> property=new HashMap<>();
						list.put(line.substring(0,index),property);
						for(String item:line.substring(index+1).split(",")){
							index=item.indexOf("=");
							property.put(item.substring(0,index),item.substring(index+1));
						}
					}
					}
			}
		}
		catch (Exception e)
		{}
		return list;
	}
}
