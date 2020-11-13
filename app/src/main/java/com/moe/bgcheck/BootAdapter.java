package com.moe.bgcheck;
import android.widget.BaseAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.content.Context;
import android.content.pm.PackageManager;
import java.util.List;
import android.content.pm.ResolveInfo;
import android.view.LayoutInflater;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.Switch;
import android.content.pm.PackageInfo;
import android.content.pm.ApplicationInfo;
import android.Manifest;
import android.content.pm.ActivityInfo;
import android.widget.CompoundButton;
import android.content.ComponentName;

public class BootAdapter extends BaseAdapter {
    private PackageManager pm;
    private List<ActivityInfo> list;
    private OnCheckedChangeListener l;
    public BootAdapter(Context context,List<ActivityInfo> list){
        pm=context.getPackageManager();
        this.list=list;
    }
    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int p1) {
        return list.get(p1);
    }

    @Override
    public long getItemId(int p1) {
        return p1;
    }

    @Override
    public View getView(int p1, View p2, ViewGroup p3) {
        if(p2==null){
            p2=LayoutInflater.from(p3.getContext()).inflate(R.layout.package_boot_item_view,p3,false);
        }
        ViewHolder vh=(BootAdapter.ViewHolder) p2.getTag();
        if(vh==null)
            p2.setTag(vh=new ViewHolder(p2));
            vh.position=p1;
            ActivityInfo info=list.get(p1);
            vh.name.setText(info.loadLabel(pm));
            vh.icon.setImageDrawable(info.loadIcon(pm));
            vh.switch_.setOnCheckedChangeListener(null);
            int state=pm.getComponentEnabledSetting(new ComponentName(info.packageName,info.name));
           vh.switch_.setChecked(state==pm.COMPONENT_ENABLED_STATE_DEFAULT||state==pm.COMPONENT_ENABLED_STATE_ENABLED);
           vh.switch_.setOnCheckedChangeListener(vh);
        return p2;
    }
    
    
    class ViewHolder implements Switch.OnCheckedChangeListener{
        TextView name;
        ImageView icon;
        Switch switch_;
        public int position;
        ViewHolder(View v){
            name=v.findViewById(R.id.name);
            icon=v.findViewById(R.id.icon);
            switch_=v.findViewById(R.id.switch_);
            //switch_.setOnCheckedChangeListener(this);
        }

        @Override
        public void onCheckedChanged(CompoundButton p1, boolean p2) {
            if(l!=null)
                l.onCheckedChanged(this,p2);
        }

        
    }
    public void setOnCheckedChangListener(OnCheckedChangeListener l){
        this.l=l;
    }
    public interface OnCheckedChangeListener{
        void onCheckedChanged(ViewHolder vh,boolean checked);
    }
}
