package com.moe.bgcheck;
import android.content.pm.*;
import android.view.*;
import android.widget.*;
import java.util.*;
import android.app.ActivityManager;
import android.content.Context;
import android.app.usage.UsageStatsManager;
import com.moe.bgcheck.utils.AppProcess;
import com.moe.bgcheck.utils.ProcessUtils;
import android.os.BatteryManager;
import android.os.PowerManager;
public class Adapter extends BaseAdapter
{
    private PowerManager bm;
	private PackageManager pm;
	private List<PackageInfo> list;
    private UsageStatsManager usm;
	private Map<String,Map<String,String>> blacklist;
    private Map<String,AppProcess> running;
    private static Map<String,String> STATE=new HashMap<>();
    static{
        STATE.put("cch","");
        STATE.put("bg","后台");
        STATE.put("top","顶层");
        STATE.put("pers","前台");
    }
	public Adapter(Context context,List<PackageInfo> list,Map<String,Map<String,String>> blacklist,Map<String,AppProcess> running){
		this.pm=context.getPackageManager();
        bm=(PowerManager) context.getSystemService(Context.POWER_SERVICE);
        usm=(UsageStatsManager) context.getSystemService(context.USAGE_STATS_SERVICE);
		this.list=list;
		this.blacklist=blacklist;
        this.running=running;
	}

  
    
	@Override
	public int getCount()
	{
		// TODO: Implement this method
		return list.size();
	}

	@Override
	public Object getItem(int p1)
	{
		// TODO: Implement this method
		return list.get(p1);
	}

	@Override
	public long getItemId(int p1)
	{
		// TODO: Implement this method
		return p1;
	}

	@Override
	public View getView(int p1, View p2, ViewGroup p3)
	{
		if(p2==null){
			p2=LayoutInflater.from(p3.getContext()).inflate(R.layout.package_item_view,p3,false);
		}
		ViewHolder vh=(Adapter.ViewHolder) p2.getTag();
		if(vh==null)
			p2.setTag(vh=new ViewHolder(p2));
		PackageInfo info=list.get(p1);
		vh.title.setText(info.applicationInfo.loadLabel(pm));
		vh.icon.setImageDrawable(info.applicationInfo.loadIcon(pm));
		vh.summary.setText(info.packageName);
		vh.check.setChecked(blacklist.containsKey(info.packageName));
		boolean radical=false;
		Map<String,String> property=blacklist.get(info.packageName);
		if(property!=null){
			String radical_=property.get("radical");
			if(radical_!=null)
				radical=radical_.equals("true");
		}
		vh.radical.setVisibility(radical?View.VISIBLE:View.GONE);
        vh.idle.setVisibility(usm.isAppInactive(info.packageName)?View.VISIBLE:View.GONE);
        AppProcess process=running.get(info.packageName);
        if(process!=null)
        {
            /*vh.fore.setVisibility(process.isForeground()?View.VISIBLE:View.GONE);
             if(!process.isRunning())
                vh.service.setVisibility(View.GONE);
                else{
                    AppProcess.Process[] processes=process.process;
                    AppProcess.Service[] services=process.services;
                vh.service.setVisibility(View.VISIBLE);
                vh.service.setText(processes.length+"个进程"+services.length+"个服务");
                }
                */
                vh.fore.setVisibility(View.GONE);
                vh.service.setVisibility(View.VISIBLE);
                vh.service.setText(STATE.get(process.state));
        }else{
            vh.fore.setVisibility(View.GONE);
            vh.service.setVisibility(View.GONE);
        }
        if(bm.isIgnoringBatteryOptimizations(info.packageName)){
            vh.ignoreidle.setVisibility(View.VISIBLE);
        }else{
            vh.ignoreidle.setVisibility(View.GONE);
        }
		return p2;
	}
	class ViewHolder{
		TextView title,summary,idle,fore,service,ignoreidle;
		ImageView icon,radical;
		CheckBox check;
		ViewHolder(View v){
			title=v.findViewById(R.id.title);
			summary=v.findViewById(R.id.summary);
			icon=v.findViewById(R.id.icon);
			check=v.findViewById(R.id.checked);
			radical=v.findViewById(R.id.radical);
            idle=v.findViewById(R.id.idle);
            fore=v.findViewById(R.id.foreground);
            service=v.findViewById(R.id.service);
            ignoreidle=v.findViewById(R.id.ignoreidle);
		}
	}
}
