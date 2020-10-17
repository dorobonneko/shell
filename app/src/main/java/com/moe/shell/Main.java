package com.moe.shell;
import java.io.*;
import java.net.*;

public class Main
{
	private static boolean run=true;
	public static void main(String[] args){
		ServerSocket server=null;
		try
		{
			server=new ServerSocket(3368);
			ProcessBuilder pb=new ProcessBuilder();
			pb.directory(new File("/"));
			pb.redirectErrorStream(true);
			pb.command("sh");
			System.out.println("Shell Sever Started!");
			System.out.println("Server running on port "+server.getLocalPort());
			System.out.println("UID:"+android.os.Process.myUid());
			System.out.println("PID:"+android.os.Process.myPid());
			while(run){
				Socket socket=server.accept();
				new Thread(new SocketHandler(socket,pb.start())).start();
			}
		}
		catch (IOException e)
		{
			System.out.println(e.getMessage());
			}
			finally{
				try
				{
					if (server != null)
						server.close();
				}
				catch (IOException e)
				{}
				System.out.println("Shell Server Closed!");
				System.exit(0);
			}
	}
	static class SocketHandler implements Runnable{
		Socket socket;
		Process process;
		SocketHandler(Socket socket,Process process){
			this.socket=socket;
			this.process=process;
		}
		void close(){
			try
			{
				socket.close();
			}
			catch (IOException e)
			{}
			process.destroy();
			System.out.println(socket.getInetAddress()+" socket close");
		}
		@Override
		public void run()
		{
			try
			{
				loop(socket.getInputStream(), process.getOutputStream());
				loop(process.getInputStream(), socket.getOutputStream());
			}
			catch (IOException e)
			{
				close();
			}
			
		}
		void loop(final InputStream in,final OutputStream out){
			new Thread(){
				public void run(){
					byte[] buff=new byte[512];
					int len=-1;
					try
					{
						while ((len = in.read(buff)) != -1)
						{
							System.out.print(new String(buff,0,len));
							if(new String(buff,0,len).startsWith("--exit--")){
								run=false;
								break;
								}
								out.write(buff,0,len);
								if(len<buff.length)
									out.write('\n');
							out.flush();
						}
					}
					catch (IOException e)
					{
						
						//System.out.println(e.getMessage());
					}finally{
						close();
					}
				}
			}.start();
		}
		
	}
}
