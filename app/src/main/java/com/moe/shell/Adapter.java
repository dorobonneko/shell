package com.moe.shell;
import android.widget.*;
import android.view.*;
import android.content.pm.*;
import java.util.*;

public class Adapter extends BaseAdapter
{
	private PackageManager pm;
	private List<PackageInfo> list;
	private Set<String> blacklist;
	public Adapter(PackageManager pm,List<PackageInfo> list,Set<String> blacklist){
		this.pm=pm;
		this.list=list;
		this.blacklist=blacklist;
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
		vh.check.setChecked(blacklist.contains(info.packageName));
		return p2;
	}
	class ViewHolder{
		TextView title,summary;
		ImageView icon;
		CheckBox check;
		ViewHolder(View v){
			title=v.findViewById(R.id.title);
			summary=v.findViewById(R.id.summary);
			icon=v.findViewById(R.id.icon);
			check=v.findViewById(R.id.checked);
		}
	}
}
