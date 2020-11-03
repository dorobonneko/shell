package com.moe.bgcheck;

import android.app.*;
import android.os.*;
import android.widget.*;
import android.view.View.*;
import android.view.*;
import java.io.*;
import java.net.*;
import android.text.method.*;
import com.moe.shell.Shell;
import com.moe.shell.ShellOption;

public class ShellActivity extends Activity implements View.OnClickListener
{
	TextView screen;
	EditText input;
	ShellOption option;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
		StrictMode.ThreadPolicy policy=new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
		screen=findViewById(R.id.screen);
		screen.setMovementMethod(ScrollingMovementMethod.getInstance());
		screen.setTextIsSelectable(true);
		input=findViewById(R.id.input);
		findViewById(R.id.send).setOnClickListener(this);
        option=new ShellOption();
		}

	@Override
	public void onClick(View p1)
	{
		String text=input.getText().toString().trim();
        try {
           screen.setText(option.exec(text));
        } catch (Exception e) {
            screen.setText(e.getMessage());
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
