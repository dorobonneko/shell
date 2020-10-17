package com.moe.shell;
import android.app.*;
import android.os.*;
import android.view.*;

public class LaunchActivity extends Activity
{

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		// TODO: Implement this method
		super.onCreate(savedInstanceState);
		setContentView(R.layout.running_view);
		Object o=Shell.getProcess();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// TODO: Implement this method
		getMenuInflater().inflate(R.menu.menu,menu);
		return true;
	}
	
}
