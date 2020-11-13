package com.moe.bgcheck;
import android.app.Activity;
import android.os.Bundle;
import android.widget.Toolbar;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.widget.ListView;
import java.util.List;
import android.content.pm.ResolveInfo;
import android.content.pm.PackageInfo;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ApplicationInfo;
import com.moe.bgcheck.beam.BootInfo;
import android.Manifest;
import android.content.pm.ActivityInfo;
import java.util.function.Predicate;
import com.moe.ifw.Ifw;
import com.moe.bgcheck.BootAdapter.ViewHolder;
import com.moe.shell.ShellOption;
import java.io.IOException;
import android.widget.Toast;

public class BootActivity extends Activity implements BootAdapter.OnCheckedChangeListener {

   
    private ListView mListView;
    private BootAdapter adapter;
    private List<ActivityInfo> list;
    private ShellOption option;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.listview);
        setActionBar((Toolbar)findViewById(R.id.toolbar));
        mListView=findViewById(R.id.listview);
        option=new ShellOption();
        list = getPackageManager().queryBroadcastReceivers(new Intent(Intent.ACTION_BOOT_COMPLETED),PackageManager.MATCH_UNINSTALLED_PACKAGES|PackageManager.MATCH_DISABLED_COMPONENTS).stream().map(new Function<ResolveInfo,ActivityInfo>(){

                @Override
                public ActivityInfo apply(ResolveInfo p1) {
                    return p1.activityInfo;
                }
            }).filter(new Predicate<ActivityInfo>(){

                @Override
                public boolean test(ActivityInfo p1) {
                    if((p1.applicationInfo.flags&ApplicationInfo.FLAG_SYSTEM)==ApplicationInfo.FLAG_SYSTEM)
                        return false;
                    return true;
                }
            }).collect(Collectors.toList());
        mListView.setAdapter(adapter=new BootAdapter(this,list));
        adapter.setOnCheckedChangListener(this);
    }
    @Override
    public void onCheckedChanged(BootAdapter.ViewHolder vh, boolean checked) {
        ActivityInfo info=list.get(vh.position);
        try {
            String msg=option.exec("pm " + (checked ?"enable": "disable") +" "+ info.packageName + "/" + info.name.replaceAll("\\$","\\\\\\$"));
            adapter.notifyDataSetChanged();
        } catch (IOException e) {
            Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            option.close();
        } catch (IOException e) {}
    }
    
    
}
