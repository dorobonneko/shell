package com.moe.bgcheck;
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
import java.util.stream.Collectors;
import java.util.function.Predicate;
import com.moe.shell.Shell;
import com.jaredrummler.android.processes.models.AndroidAppProcess;
import android.app.job.JobScheduler;
import com.jaredrummler.android.processes.AndroidProcesses;
import com.jaredrummler.android.processes.models.AndroidProcess;
import android.app.usage.UsageStatsManager;
import com.moe.bgcheck.utils.AppProcess;
import com.moe.bgcheck.utils.ProcessUtils;
import com.moe.shell.ShellOption;
import com.moe.shell.ShellUtil;

public class LaunchActivity extends Activity implements ListView.OnItemClickListener,
SearchView.OnCloseListener,
SearchView.OnQueryTextListener,
ListView.OnItemLongClickListener,
ShellOption.Callback
{
	private Map<String,Map<String,String>> blacklist;
	private Menu mMenu;
	private ListView mListView;
	private SearchView searchView;
	private List<PackageInfo> data,mList;
	private int id;
	private java.lang.Process p;
	private PrintWriter pw;
	private Adapter mAdapter;
    private ShellOption option;
    private UsageStatsManager usm;
    private Map<String,AppProcess> running;
    private Timer mTimer=new Timer();
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		StrictMode.allowThreadDiskReads();
        StrictMode.allowThreadDiskWrites();
        try{
            p=Runtime.getRuntime().exec("sh");
            pw=new PrintWriter(p.getOutputStream());
        }catch(Exception e){}
		init();
		super.onCreate(savedInstanceState);
        usm=(UsageStatsManager) getSystemService(USAGE_STATS_SERVICE);
		setContentView(R.layout.listview);
		setActionBar((Toolbar)findViewById(R.id.toolbar));
		searchView=findViewById(R.id.search);
		searchView.setOnCloseListener(this);
		searchView.setOnQueryTextListener(this);
		searchView.setVisibility(View.INVISIBLE);
		searchView.setLayoutParams(new FrameLayout.LayoutParams(searchView.getLayoutParams()));
		mListView=findViewById(R.id.listview);
		mListView.setOnItemClickListener(this);
		mListView.setOnItemLongClickListener(this);
		registerForContextMenu(mListView);
		data=getPackageManager().getInstalledPackages(PackageManager.MATCH_ALL | PackageManager.GET_SERVICES);
		mListView.setAdapter(mAdapter=new Adapter(getApplicationContext(),mList=new ArrayList<>(),blacklist=Shell.getBlackList(),running=new HashMap<>()));
		mList.addAll(getApks(false));
		mAdapter.notifyDataSetChanged();
        option=new ShellOption();
        option.setCallback(this);
        mTimer.schedule(new Task(),0,30000);
	}
    @Override
	public void onCloseShell(){
        runOnUiThread(new Runnable(){
            public void run(){
        onResume();
        }});
    }
	@Override
	public boolean onQueryTextSubmit(String p1)
	{
		// TODO: Implement this method
		return false;
	}

	@Override
	public boolean onQueryTextChange(final String p1)
	{
		
		ArrayList<PackageInfo> list=getApks(!mMenu.findItem(R.id.showSystem).isVisible()).stream().filter(new Predicate<PackageInfo>(){

				@Override
				public boolean test(PackageInfo info)
				{
					if(info.applicationInfo.loadLabel(getPackageManager()).toString().contains(p1)||info.packageName.contains(p1))
						return true;
					return false;
				}
			}).collect(Collectors.toList());
		mList.clear();
		mList.addAll(list);
		mAdapter.notifyDataSetChanged();
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
					mList.clear();
					mList.addAll(getApks(!mMenu.findItem(R.id.showSystem).isVisible()));
					mAdapter.notifyDataSetChanged();
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
		a.setDuration(200);
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
				pw.println("chmod 777 "+exe.getAbsolutePath());
				pw.flush();
			}
			catch (IOException e)
			{}
		//}
		File stop=new File(getFilesDir(),"moestop");
		//if(!stop.exists()){
			try
			{
				ZipFile zf=new ZipFile(getPackageResourcePath());
				InputStream i=zf.getInputStream(zf.getEntry("classes.dex"));
				byte[] buff=new byte[128];
				OutputStream o=openFileOutput("moestop",MODE_PRIVATE);
				int len=-1;
				while((len=i.read(buff))!=-1){
					o.write(buff,0,len);
				}
				o.flush();
				o.close();
				i.close();
				zf.close();
				pw.println("chmod 777 "+stop.getAbsolutePath());
				pw.flush();
			}
			catch (IOException e)
			{}
		//}
	}
	List<PackageInfo> getApks(final boolean system){
		return data.stream().filter(new Predicate<PackageInfo>(){

				@Override
				public boolean test(PackageInfo info)
				{
					if(!system){
						if((info.applicationInfo.flags&ApplicationInfo.FLAG_SYSTEM)==ApplicationInfo.FLAG_SYSTEM){
							return false;
						}
					}
					if(info.services==null||info.services.length==0)
						return false;
					return true;
				}
			}).collect(Collectors.toList());
	}
	@Override
	protected void onResume()
	{
		// TODO: Implement this method
		super.onResume();
		
        getActionBar().setSubtitle(option.isRunning()?"服务已启动":"服务未启动！！！");
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
				mList.clear();
				mList.addAll(getApks(true));
				mAdapter.notifyDataSetChanged();
				item.setVisible(false);
				mMenu.findItem(R.id.showApk).setVisible(true);
				break;
			case R.id.showApk:
				mList.clear();
				mList.addAll(getApks(false));
				mAdapter.notifyDataSetChanged();
				
				item.setVisible(false);
				mMenu.findItem(R.id.showSystem).setVisible(true);
				break;
			case R.id.root:
                new Thread(){
                    public void run(){
				try{
					java.lang.Process p=Runtime.getRuntime().exec("su");
					PrintWriter pw=new PrintWriter(p.getOutputStream());
					pw.println("sh /data/data/com.moe.bgcheck/files/exe.sh");
					//pw.println("sleep 1s");
					pw.println("exit");
					pw.flush();
					try
					{
						p.waitFor();
					}
					catch (InterruptedException e)
					{}
                    pw.close();
					p.destroy();
                    getWindow().getDecorView().postDelayed(new Runnable(){

                            @Override
                            public void run() {
                                /*try {
                                    option.close();
                                } catch (IOException e) {}
                                option = new ShellOption();*/
                                onResume();
                            }
                        }, 1000);
				}catch(Exception e){}
                }}.start();
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
				a.setDuration(500);
				a.start();
			break;
			case R.id.about:
				new AlertDialog.Builder(this).setTitle("关于").setMessage(R.string.about).setPositiveButton("重启服务", new DialogInterface.OnClickListener(){

                        @Override
                        public void onClick(DialogInterface p1, int p2) {
                            new Thread(){
                                public void run(){
                                    try {
                                        option.reboot();
                                    } catch (IOException e) {}
                                    getWindow().getDecorView().postDelayed(new Runnable(){

                                        @Override
                                        public void run() {
                                            /*try {
                                                option.close();
                                            } catch (IOException e) {}
                                            option = new ShellOption();*/
                                            onResume();
                                        }
                                    }, 1000);
                                }}.start();
                            
                        }
                    }).show();
                break;
			case R.id.shizuku:
				/*if(checkCallingOrSelfPermission(ShizukuApiConstants.PERMISSION)==PackageManager.PERMISSION_DENIED){
					requestPermissions(new String[]{ShizukuApiConstants.PERMISSION},3338);
				}else onRequestPermissionsResult(3338,new String[]{ShizukuApiConstants.PERMISSION},new int[]{PackageManager.PERMISSION_GRANTED});
				*/break;
		}
		return true;
	}

	/*@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
	{
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if(requestCode==3338&&grantResults[0]==PackageManager.PERMISSION_GRANTED){
			try
			{
				if (ShizukuService.getUid() <= 2000)
				{
					Toast.makeText(this,"Shizuku权限不足",Toast.LENGTH_SHORT).show();
				}else{
					java.lang.Process p=ShizukuService.newProcess(new String[]{"sh"},null,null);
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
				}
			}
			catch (RemoteException e)
			{}
		}
	}*/
	
	@Override
	public void onItemClick(AdapterView l, View v, int position, long id)
	{
		/*if(checkCallingOrSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)==PackageManager.PERMISSION_DENIED){
			requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},356);
			return;
		}*/
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
		final PackageInfo info=(PackageInfo) mListView.getAdapter().getItem(id);
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
            case R.id.appstandby:
                if(!usm.isAppInactive(info.packageName))
                    new Thread(){
                        public void run(){
                            
                              try{
                               option.exec("am set-inactive "+info.packageName+" true");
                                runOnUiThread(new Runnable(){

                                        @Override
                                        public void run() {
                                              mAdapter.notifyDataSetChanged();
                                              }
                                    });
                            }catch(final Exception e){
                                runOnUiThread(new Runnable(){

                                        @Override
                                        public void run() {
                                            Toast.makeText(getApplicationContext(),"失败"+e.getMessage(),Toast.LENGTH_SHORT).show();
                                        }
                                    });
                            }finally{
                                
                            }
                        }
                    }.start();
                break;
		}
		if(property!=null){
			if(property.isEmpty())
				blacklist.put(info.packageName,null);
		}
		save();
		((BaseAdapter)mListView.getAdapter()).notifyDataSetChanged();
		return true;
	}

	
	void save(){
		try
		{
			PrintWriter pw=new PrintWriter(openFileOutput("forcestop",MODE_PRIVATE));
			LaunchActivity.this.pw.println("chmod 644 "+new File(getFilesDir(),"forcestop").getAbsolutePath());
			LaunchActivity.this.pw.flush();
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

	@Override
	protected void onDestroy()
	{
		// TODO: Implement this method
		super.onDestroy();
		pw.println("exit");
		pw.flush();
		pw.close();
		p.destroy();
        mTimer.cancel();
	}
    class Task extends TimerTask {

        @Override
        public void run() {
            Iterator<PackageInfo> i=data.iterator();
            StringBuilder sb=new StringBuilder();
            while(i.hasNext()){
                String pn=i.next().packageName;
                sb.append("echo ");
                sb.append(pn);
                sb.append("\n");
                sb.append("echo $(dumpsys appops --package ");
                sb.append(pn);
                sb.append("|grep state=|cut -d = -f 2)\n");
            }
            final Map<String,AppProcess> list=new HashMap<>();
            ShellOption option=new ShellOption();
            try{
            BufferedReader br=new BufferedReader(new InputStreamReader(new ByteArrayInputStream(option.exec(sb.toString()).getBytes())));
            
                do{
                    String line=br.readLine();
                    if (line == null)
                        break;
                        AppProcess ap=new AppProcess();
                        ap.state=br.readLine();
                        
                        list.put(line,ap);
                }while(true);
                br.close();
                option.close();
            } catch (IOException e) {}
            finally{
                
                runOnUiThread(new Runnable(){

                        @Override
                        public void run() {
                            running.clear();
                            running.putAll(list);
                            mAdapter.notifyDataSetChanged();
                        }
                    });
            }
        }

        
    }
}
