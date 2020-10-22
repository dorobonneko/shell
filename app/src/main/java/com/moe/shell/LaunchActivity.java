package com.moe.shell;
import android.app.*;
import android.os.*;
import android.view.*;
import android.content.*;
import android.content.pm.*;
import java.net.*;
import android.widget.*;
import java.util.*;
import android.*;
import java.io.*;
import java.util.zip.*;

public class LaunchActivity extends ListActivity
{
	private Set<String> blacklist;
	private Menu mMenu;
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		// TODO: Implement this method
		super.onCreate(savedInstanceState);
		StrictMode.allowThreadDiskReads();
		StrictMode.allowThreadDiskWrites();
		init();
		setListAdapter(new Adapter(getPackageManager(),getApks(false),blacklist=Shell.getBlackList()));
	}
	void init(){
		File exe=new File(getFilesDir(),"exe.sh");
		//if(!exe.exists()){
			try
			{
				InputStream i=getAssets().open("exe.sh");
				byte[] buff=new byte[128];
				OutputStream o=openFileOutput("exe.sh",0);
				int len=-1;
				while((len=i.read(buff))!=-1){
					o.write(buff,0,len);
				}
				o.flush();
				o.close();
				i.close();
				java.lang.Process p=Runtime.getRuntime().exec("sh");
				PrintWriter pw=new PrintWriter(p.getOutputStream());
				pw.println("chmod 777 "+exe.getAbsolutePath());
				pw.println("exit");
				pw.flush();
				try
				{
					p.waitFor();
				}
				catch (InterruptedException e)
				{}
				p.destroy();
			}
			catch (IOException e)
			{}
		//}
		//File stop=new File(getFilesDir(),"moestop");
		//if(!stop.exists()){
			try
			{
				ZipFile zf=new ZipFile(getPackageResourcePath());
				InputStream i=zf.getInputStream(zf.getEntry("classes.dex"));
				byte[] buff=new byte[128];
				OutputStream o=openFileOutput("moestop",MODE_WORLD_READABLE);
				int len=-1;
				while((len=i.read(buff))!=-1){
					o.write(buff,0,len);
				}
				o.flush();
				o.close();
				i.close();
				zf.close();
				//Runtime.getRuntime().exec("chmod 777 "+exe.getAbsolutePath()).destroy();
			}
			catch (IOException e)
			{}
		//}
	}
	List<PackageInfo> getApks(boolean system){
		List<PackageInfo> list=getPackageManager().getInstalledPackages(PackageManager.MATCH_ALL);
		if(system)
			return list;
		Iterator<PackageInfo> i=list.iterator();
		while(i.hasNext()){
			if((i.next().applicationInfo.flags&ApplicationInfo.FLAG_SYSTEM)==ApplicationInfo.FLAG_SYSTEM)
				i.remove();
		}
		return list;
	}
	@Override
	protected void onResume()
	{
		// TODO: Implement this method
		super.onResume();
		StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().build());
		Socket s=new Socket();
		
		try{
			s.connect(new InetSocketAddress(3335));
			if(s.isConnected())
			getActionBar().setSubtitle("服务已启动");
			else
				throw new ConnectException();
		}catch(ConnectException e){
			getActionBar().setSubtitle("服务未启动！！！");
		}catch(Exception e){}
		finally{
			try
			{
				s.close();
			}
			catch (IOException e)
			{}
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// TODO: Implement this method
		mMenu=menu;
		getMenuInflater().inflate(R.menu.menu,menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch(item.getItemId()){
			case R.id.shell:
				startActivity(new Intent(this,ShellActivity.class));
				break;
			case R.id.showSystem:
				setListAdapter(new Adapter(getPackageManager(),getApks(true),blacklist=Shell.getBlackList()));
				item.setVisible(false);
				mMenu.findItem(R.id.showApk).setVisible(true);
				break;
			case R.id.showApk:
				setListAdapter(new Adapter(getPackageManager(),getApks(false),blacklist=Shell.getBlackList()));
				item.setVisible(false);
				mMenu.findItem(R.id.showSystem).setVisible(true);
				break;
			case R.id.root:
				try{
					java.lang.Process p=Runtime.getRuntime().exec("su");
					PrintWriter pw=new PrintWriter(p.getOutputStream());
					pw.println("/data/data/com.moe.shell/exe.sh");
					pw.println("exit");
					pw.flush();
					try
					{
						p.waitFor();
					}
					catch (InterruptedException e)
					{}
					p.destroy();
					onResume();
				}catch(Exception e){}
				break;
		}
		return true;
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id)
	{
		if(checkCallingOrSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)==PackageManager.PERMISSION_DENIED){
			requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},356);
			return;
		}
		Adapter.ViewHolder vh=(Adapter.ViewHolder) v.getTag();
		String packageName=((PackageInfo)l.getAdapter().getItem(position)).packageName;
		if(vh.check.isChecked()){
			blacklist.remove(packageName);
			((BaseAdapter)getListAdapter()).notifyDataSetInvalidated();
		}else{
			blacklist.add(packageName);
			((BaseAdapter)getListAdapter()).notifyDataSetInvalidated();
		}
		save();
	}
	void save(){
		try
		{
			PrintWriter pw=new PrintWriter(openFileOutput("forcestop",MODE_WORLD_READABLE));
			Iterator<String> i=blacklist.iterator();
			while(i.hasNext()){
				pw.println(i.next());
			}
			pw.flush();
			pw.close();
		}
		catch (IOException e)
		{}
	}
}
