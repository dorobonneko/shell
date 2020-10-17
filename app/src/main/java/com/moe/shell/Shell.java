package com.moe.shell;
import java.io.*;
import java.util.*;
import java.util.regex.*;
import java.net.*;

public class Shell
{
	private static List<String> whitelist=new ArrayList<>();
	private static Pattern processPattern=Pattern.compile("^\\*APP\\*\\sUID\\s(\\d*?)\\sProcessRecord\\{.*?\\s\\d*?:([\\.a-zA-Z0-9:].*?)/.*?\\}$");
	
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
			System.out.println("启动成功");
			new Thread(){
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
			}}.start();
			try
			{
				ServerSocket ss=new ServerSocket(3335);
				while(true){
					final Socket socket=ss.accept();
					new Thread(){
						public void run(){
							handle(socket);
						}
					}.start();
				}
			}
			catch (Exception e)
			{
				System.out.println(e.getMessage());
			}
		}
		
	}
	static void handle(Socket socket){
		try
		{
			BufferedReader br=new BufferedReader(new InputStreamReader(socket.getInputStream()));
			java.lang.Process process=Runtime.getRuntime().exec("sh");
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
			
		}finally{
			try
			{
				socket.close();
			}
			catch (IOException ee)
			{}
		}
	}
	static void kill(){
			try{
				List<String> recents=getRecents();
			for(String packageName:getBlackList()){
				if(whitelist.contains(packageName)||recents.contains(packageName))
					continue;
					kill(packageName);
			}
			}catch(Exception e){
				System.out.println(e.getMessage());
			}
		}
		
		
	
	public static List<String> getRecents(){
		List<String> list=new ArrayList<>();
		try
		{
			java.lang.Process process=Runtime.getRuntime().exec("sh");
			OutputStream out=process.getOutputStream();
			out.write("dumpsys activity r|grep \"* Recent #\"\n".getBytes());
			out.flush();
			out.write("exit\n".getBytes());
			out.flush();
			
			BufferedReader br=new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line=null;
			while((line=br.readLine())!=null){
				line=line.trim();
				if(line.startsWith("* Recent #")){
					for(String item:line.substring(line.indexOf("{")+1,line.length()-1).split(" ")){
						if(item.charAt(0)=='A'){
							int index=item.indexOf(":");
							if(index!=-1)
							list.add(item.substring(2).split(":")[1]);
							else
							list.add(item.substring(2));
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
		System.out.println("当前 Recents:");
		System.out.println(list.toString());
		return list;
	}
	public static List<String> getProcess(){
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
	}
	public static void kill(String packageName){
		try
		{
			java.lang.Process process=Runtime.getRuntime().exec("sh");
			OutputStream out=process.getOutputStream();
			out.write(("am force-stop "+packageName+"\n").getBytes());
			out.flush();
			out.write("exit\n".getBytes());
			out.flush();
			InputStream i=process.getInputStream();
			int len=-1;
			byte[] buff=new byte[128];
			StringBuilder sb=new StringBuilder();
			while((len=i.read(buff))!=-1){
				sb.append(new String(buff,0,len));
			}
			System.out.println(sb.toString());
			process.destroy();
			System.out.println("kill "+packageName);
		}
		catch (Exception e)
		{
			System.out.println(e.getMessage());
		}
	}
	public static List<String> getBlackList(){
		List<String> list=new ArrayList<>();
		try
		{
			FileReader fr=new FileReader("/sdcard/forcestop");
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
