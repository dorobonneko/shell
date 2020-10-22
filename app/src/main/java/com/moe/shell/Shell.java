package com.moe.shell;
import java.io.*;
import java.util.*;
import java.util.regex.*;
import java.net.*;
import java.util.concurrent.*;
import java.nio.channels.*;
import java.nio.*;

public class Shell
{
	private static List<String> whitelist=new ArrayList<>();
	private static Pattern processPattern=Pattern.compile("^\\*APP\\*\\sUID\\s(\\d*?)\\sProcessRecord\\{.*?\\s\\d*?:([\\.a-zA-Z0-9:].*?)/.*?\\}$");
	private static PrintWriter exec;
	public static void main(String[] args){
		if(android.os.Process.myUid()>2000){
			System.out.println("权限不足！");
			System.exit(1);
		}else{
			for(int i=0;i<args.length;i+=2){
				switch(args[i]){
					case "-w":
						whitelist.addAll(Arrays.asList(args[i+1].trim().split(",")));
						break;
				}
			}
			
			//mTimer.schedule(new MainProcess(),0,60*1000*5);
			
			try
			{
				//ServerSocket ss=new ServerSocket(3335);
				/*new Thread(){
					public void run(){
						while(true){
							kill();
							try
							{
								Thread.sleep(60 * 1000);
							}
							catch (InterruptedException e)
							{}
						}
					}}.start();*/
				ServerSocketChannel ssc=ServerSocketChannel.open();
					ssc.configureBlocking(false);
					ssc.socket().bind(new InetSocketAddress(3335));
				ScheduledExecutorService service = Executors
					.newSingleThreadScheduledExecutor();
				
				service.scheduleAtFixedRate(new Runnable(){

						@Override
						public void run()
						{
							kill();
							//System.exit(0);
						}
					}, 0, 1, TimeUnit.MINUTES);
				 
					System.out.println("启动成功");
				/*while(!ss.isClosed()){
					final Socket socket=ss.accept();
					new Thread(){
						public void run(){
							handle(socket);
						}
					}.start();
				}*/
				Selector mSelector=Selector.open();
				ssc.register(mSelector,SelectionKey.OP_ACCEPT);
				ByteBuffer echoBuffer=ByteBuffer.allocate(1048576);
				while(true){
					int num=mSelector.select();
					if(num<1)continue;
					Iterator<SelectionKey> iterator=mSelector.selectedKeys().iterator();
					while(iterator.hasNext()){
						SelectionKey key=iterator.next();
						if((key.readyOps()&SelectionKey.OP_ACCEPT)==SelectionKey.OP_ACCEPT){
							ServerSocketChannel serverChanel = (ServerSocketChannel)key.channel(); 
							SocketChannel sc = serverChanel.accept(); 
							sc.configureBlocking( false );
							SelectionKey newKey = sc.register( mSelector, 
															  SelectionKey.OP_READ ); 
							iterator.remove();
						}else if((key.readyOps()&SelectionKey.OP_READ)==SelectionKey.OP_READ){
							SocketChannel sc = (SocketChannel)key.channel();
							int bytesEchoed = 0; 
							while((bytesEchoed = sc.read(echoBuffer))> 0){ 
								System.out.println("bytesEchoed:"+bytesEchoed); 
							} 
							echoBuffer.flip(); 
							byte [] content = new byte[echoBuffer.limit()]; 
							echoBuffer.get(content); 
							String result=new String(content);
							doPost(result,sc,echoBuffer);
							echoBuffer.clear(); 
							iterator.remove(); 
						}
					}
					
				}
			}
			catch (Exception e)
			{
				System.exit(2);
				System.out.println(e.getMessage());
			}
		}
		
	}
	static void doPost(String result,SocketChannel sc,ByteBuffer buff){
		try
		{
			ProcessBuilder pb=new ProcessBuilder();
			pb.directory(new File("/sdcard"));
			pb.redirectErrorStream(true);
			pb.command("sh");
			java.lang.Process process=pb.start();
			PrintWriter pw=new PrintWriter(process.getOutputStream());
			pw.println(result);
			pw.println("exit");
			pw.flush();
			BufferedReader response=new BufferedReader(new InputStreamReader(process.getInputStream()));
			buff.clear();
			String line=null;
			while((line=response.readLine())!=null){
				buff.put(line.getBytes());
				buff.putChar('\n');
				buff.flip();
				buff.limit();
				sc.write(buff);
				buff.clear();
			}
			//out.print("--exit--");
			sc.close();
		}
		catch (Exception e)
		{
			System.out.println(e.getMessage());
		}
	}
	/*static void handle(Socket socket){
		try
		{
			BufferedReader br=new BufferedReader(new InputStreamReader(socket.getInputStream()));
			ProcessBuilder pb=new ProcessBuilder();
			pb.directory(new File("/sdcard"));
			pb.redirectErrorStream(true);
			pb.command("sh");
			java.lang.Process process=pb.start();
			PrintWriter pw=new PrintWriter(process.getOutputStream());
			String line=null;
			System.out.println("读");
			while((line=br.readLine())!=null){
				System.out.println(line);
				if("--exit--".equals(line))
					break;
				pw.println(line);
			}
			pw.println("exit");
			pw.flush();
			System.out.println("读取完毕");
			BufferedReader response=new BufferedReader(new InputStreamReader(process.getInputStream()));
			PrintWriter out=new PrintWriter(socket.getOutputStream());
			System.out.println("写");
			while((line=response.readLine())!=null){
				out.println(line);
				System.out.println(line);
			}
			//out.print("--exit--");
			out.flush();
			System.out.println("写入完毕");
		}
		catch (Exception e)
		{
			System.out.println(e.getMessage());
		}finally{
			try
			{
				socket.close();
			}
			catch (IOException ee)
			{}
		}
	}*/
	static void kill(){
			try{
				Set<String> recents=getRecents();
			for(String packageName:getBlackList()){
				if(whitelist.contains(packageName)||recents.contains(packageName))
					continue;
					kill(packageName);
			}
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
				
				/*if(line.startsWith("* Recent #")){
					for(String item:line.substring(line.indexOf("{")+1,line.length()-1).split(" ")){
						if(item.charAt(0)=='A'){
							int index=item.indexOf(":");
							if(index!=-1)
							list.add(item.substring(2).split(":")[1]);
							else
							list.add(item.substring(2));
						}
					}
				}else{
					for(String item:line.substring(line.indexOf("{")+1,line.indexOf("}")-1).split(" ")){
						if(item.startsWith("cmp=")){
							list.add(item.substring(4,item.indexOf("/")));
						}
					}
				}*/

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
	public static void kill(String packageName){
		try
		{
			if(exec==null){
			java.lang.Process process=Runtime.getRuntime().exec("sh");
			exec=new PrintWriter(process.getOutputStream());
			}
			exec.println("am kill "+packageName);
			exec.flush();
			System.out.println("kill "+packageName);
		}
		catch (Exception e)
		{
			System.out.println(e.getMessage());
		}
	}
	public static Set<String> getBlackList(){
		Set<String> list=new HashSet<>();
		try
		{
			FileReader fr=new FileReader("/data/data/com.moe.shell/files/forcestop");
			BufferedReader br=new BufferedReader(fr);
			String line=null;
			while((line=br.readLine())!=null){
				line=line.trim();
				if(!line.isEmpty())
					list.add(line);
			}
		}
		catch (Exception e)
		{}
		return list;
	}
}
