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
import android.widget.SearchView.*;
import android.animation.*;
import android.view.ContextMenu.*;

public class LaunchActivity extends Activity implements ListView.OnItemClickListener,
SearchView.OnCloseListener,
SearchView.OnQueryTextListener,
ListView.OnItemLongClickListener
{
	private Map<String,Map<String,String>> blacklist;
	private Menu mMenu;
	private ListView mListView;
	private SearchView searchView;
	private List<PackageInfo> data;
	private int id;
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		// TODO: Implement this method
		super.onCreate(savedInstanceState);
		setContentView(R.layout.listview);
		setActionBar((Toolbar)findViewById(R.id.toolbar));
		StrictMode.allowThreadDiskReads();
		StrictMode.allowThreadDiskWrites();
		init();
		searchView=findViewById(R.id.search);
		searchView.setOnCloseListener(this);
		searchView.setOnQueryTextListener(this);
		searchView.setVisibility(View.INVISIBLE);
		searchView.setLayoutParams(new FrameLayout.LayoutParams(searchView.getLayoutParams()));
		mListView=findViewById(R.id.listview);
		mListView.setOnItemClickListener(this);
		mListView.setOnItemLongClickListener(this);
		registerForContextMenu(mListView);
		mListView.setAdapter(new Adapter(getPackageManager(),data=getApks(false),blacklist=Shell.getBlackList()));
	}
	
	@Override
	public boolean onQueryTextSubmit(String p1)
	{
		// TODO: Implement this method
		return false;
	}

	@Override
	public boolean onQueryTextChange(String p1)
	{
		ArrayList<PackageInfo> list=new ArrayList<>();
		for(int i=0;i<data.size();i++){
			PackageInfo info=data.get(i);
			if(info.applicationInfo.loadLabel(getPackageManager()).toString().contains(p1)||info.packageName.contains(p1))
				list.add(info);
		}
		mListView.setAdapter(new Adapter(getPackageManager(),list,blacklist));
		return false;
	}

	@Override
	public boolean onClose()
	{
		Animator a=ObjectAnimator.ofFloat(searchView,"Alpha",1,0);
		a.addListener(new Animator.AnimatorListener(){

				@Override
				public void onAnimationStart(Animator p1)
				{
					// TODO: Implement this method
				}

				@Override
				public void onAnimationEnd(Animator p1)
				{
					searchView.setVisibility(View.INVISIBLE);
					mListView.setAdapter(new Adapter(getPackageManager(),data,blacklist));
				}

				@Override
				public void onAnimationCancel(Animator p1)
				{
					// TODO: Implement this method
				}

				@Override
				public void onAnimationRepeat(Animator p1)
				{
					// TODO: Implement this method
				}
			});
		a.setDuration(300);
		a.start();
		return true;
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
		List<PackageInfo> list=getPackageManager().getInstalledPackages(PackageManager.MATCH_ALL|PackageManager.GET_SERVICES);
		Iterator<PackageInfo> i=list.iterator();
		while(i.hasNext()){
			PackageInfo info=i.next();
			if(!system){
			if((info.applicationInfo.flags&ApplicationInfo.FLAG_SYSTEM)==ApplicationInfo.FLAG_SYSTEM){
				i.remove();
				continue;
				}
			}
			if(info.services==null||info.services.length==0)
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
				mListView.setAdapter(new Adapter(getPackageManager(),data=getApks(true),blacklist));
				item.setVisible(false);
				mMenu.findItem(R.id.showApk).setVisible(true);
				break;
			case R.id.showApk:
				mListView.setAdapter(new Adapter(getPackageManager(),data=getApks(false),blacklist));
				item.setVisible(false);
				mMenu.findItem(R.id.showSystem).setVisible(true);
				break;
			case R.id.root:
				try{
					java.lang.Process p=Runtime.getRuntime().exec("su");
					PrintWriter pw=new PrintWriter(p.getOutputStream());
					pw.println("sh /data/data/com.moe.shell/files/exe.sh");
					pw.println("sleep 1s");
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
			case R.id.search:
				Animator a=ObjectAnimator.ofFloat(searchView,"Alpha",0,1);
				a.addListener(new Animator.AnimatorListener(){

						@Override
						public void onAnimationStart(Animator p1)
						{
							searchView.setAlpha(0);
							searchView.setVisibility(View.VISIBLE);
							
						}

						@Override
						public void onAnimationEnd(Animator p1)
						{
							// TODO: Implement this method
							searchView.onActionViewExpanded();
						}

						@Override
						public void onAnimationCancel(Animator p1)
						{
							// TODO: Implement this method
						}

						@Override
						public void onAnimationRepeat(Animator p1)
						{
							// TODO: Implement this method
						}
					});
				a.setDuration(150);
				a.start();
			break;
		}
		return true;
	}
	@Override
	public void onItemClick(AdapterView l, View v, int position, long id)
	{
		if(checkCallingOrSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)==PackageManager.PERMISSION_DENIED){
			requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},356);
			return;
		}
		Adapter.ViewHolder vh=(Adapter.ViewHolder) v.getTag();
		String packageName=((PackageInfo)l.getAdapter().getItem(position)).packageName;
		if(vh.check.isChecked()){
			blacklist.remove(packageName);
			((BaseAdapter)mListView.getAdapter()).notifyDataSetInvalidated();
		}else{
			blacklist.put(packageName,null);
			((BaseAdapter)mListView.getAdapter()).notifyDataSetInvalidated();
		}
		save();
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> p1, View p2, int p3, long p4)
	{
		id=p3;
		return false;
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo)
	{
		getMenuInflater().inflate(R.menu.context,menu);
		boolean radical=false;
		PackageInfo info=(PackageInfo) mListView.getAdapter().getItem(id);
		Map<String,String> property=blacklist.get(info.packageName);
		if(property!=null){
			String radical_=property.get("radical");
			if(radical_!=null)
				radical=radical_.equals("true");
		}
		MenuItem radical_menu=menu.findItem(R.id.radical);
		radical_menu.setVisible(!radical);
	MenuItem cancel_radical_menu=menu.findItem(R.id.cancel_radical);
	cancel_radical_menu.setVisible(radical);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item)
	{
		PackageInfo info=(PackageInfo) mListView.getAdapter().getItem(id);
		Map<String,String> property=blacklist.get(info.packageName);
		switch(item.getItemId()){
			case R.id.radical:
				if(property==null)
					blacklist.put(info.packageName,property=new HashMap<>());
					property.put("radical","true");
				break;
			case R.id.cancel_radical:
				if(property!=null)
					property.remove("radical");
					
				break;
		}
		if(property!=null){
			if(property.isEmpty())
				blacklist.put(info.packageName,null);
		}
		save();
		((BaseAdapter)mListView.getAdapter()).notifyDataSetInvalidated();
		return true;
	}

	
	void save(){
		try
		{
			PrintWriter pw=new PrintWriter(openFileOutput("forcestop",MODE_WORLD_READABLE));
			Iterator<Map.Entry<String,Map<String,String>>> i=blacklist.entrySet().iterator();
			while(i.hasNext()){
				Map.Entry<String,Map<String,String>> entry=i.next();
				pw.print(entry.getKey());
				Map<String,String> property=entry.getValue();
				if(property!=null){
					pw.print(":");
					Iterator<Map.Entry<String,String>> iterator=property.entrySet().iterator();
					while(iterator.hasNext()){
						Map.Entry<String,String> property_item=iterator.next();
						pw.print(property_item.getKey());
						pw.print("=");
						pw.print(property_item.getValue());
						if(iterator.hasNext())
							pw.print(",");
					}
				}
				pw.println();
			}
			pw.flush();
			pw.close();
		}
		catch (IOException e)
		{}
	}

	@Override
	public void onBackPressed()
	{
		if(searchView.getVisibility()==View.VISIBLE)
			onClose();
			else
		super.onBackPressed();
	}
	
}
