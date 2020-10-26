package com.moe.shell;
import java.io.*;
import java.util.*;
import java.util.regex.*;
import java.net.*;
import java.util.concurrent.*;
import java.nio.channels.*;
import java.nio.*;
import android.os.*;
import java.lang.reflect.*;

public class Shell
{
	/*am set-standby-bucket packagename active|working_set|frequent|rare
	*/
	private PrintWriter exec;
	private ThreadPoolExecutor tpe;
	private ProcessBuilder mProcessBuilder;
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
	
	public Shell(){
		try{
			ServerSocketChannel ssc=ServerSocketChannel.open();
			ssc.configureBlocking(false);
			ssc.socket().bind(new InetSocketAddress(3335));
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
				}, 1, 1, TimeUnit.MINUTES);
			mProcessBuilder=new ProcessBuilder();
			mProcessBuilder.directory(new File("/sdcard"));
			mProcessBuilder.redirectErrorStream(true);
			mProcessBuilder.command("sh");
			final Selector mSelector=Selector.open();
			ssc.register(mSelector,SelectionKey.OP_ACCEPT);
			new Thread(){
				public void run(){
					try
					{
						loop(mSelector);
					}
					catch (Exception e)
					{}
				}}.start();
			System.out.println("启动成功");
		}
		catch (Exception e)
		{
			System.exit(2);
			System.out.println(e.getMessage());
		}
	}
    void exit(){
        exec.println("exit");
        exec.flush();
        exec.close();
        tpe.shutdown();
        System.exit(0);
    }
	private void loop(Selector mSelector) throws Exception{
		while(true){
			int num=mSelector.select();
			if(num<1)continue;
			Set<SelectionKey> keys=mSelector.selectedKeys();
			Iterator<SelectionKey> iterator=keys.iterator();
			while(iterator.hasNext()){
				SelectionKey key=iterator.next();
				if((key.readyOps()&SelectionKey.OP_ACCEPT)==SelectionKey.OP_ACCEPT){
					ServerSocketChannel serverChanel = (ServerSocketChannel)key.channel(); 
					SocketChannel sc = serverChanel.accept(); 
                    if(((InetSocketAddress)sc.getRemoteAddress()).getPort()==3336){
                       sc.close();
                       serverChanel.close();
                       exec.println("sh /data/data/com.moe.bgcheck/files/exe.sh");
                      exec.flush();
                       exit();
                    }
					sc.configureBlocking( false );
					SelectionKey newKey = sc.register( mSelector, 
													  SelectionKey.OP_READ );
					newKey.attach(mProcessBuilder.start());
					//iterator.remove();
				}else if((key.readyOps()&SelectionKey.OP_READ)==SelectionKey.OP_READ){
					new Op_Read(key).run();
					//iterator.remove(); 
				}//op_write仅在socket缓冲区已满无法写入才使用
			}
			keys.clear();
		}
	}
	
	class Op_Read implements Runnable{
		SelectionKey mSelectionKey;
		Op_Read(SelectionKey mSelector){
			this.mSelectionKey=mSelector;
		}

		@Override
		public void run()
		{
			SocketChannel sc = (SocketChannel)mSelectionKey.channel();
			ByteBuffer echoBuffer=ByteBuffer.allocate(4096);
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
				{}*/
			}
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
			if(exec==null){
			java.lang.Process process=Runtime.getRuntime().exec("sh");
			exec=new PrintWriter(process.getOutputStream());
			}
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
