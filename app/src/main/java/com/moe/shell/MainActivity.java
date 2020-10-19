package com.moe.shell;

import android.app.*;
import android.os.*;
import android.widget.*;
import android.view.View.*;
import android.view.*;
import java.io.*;
import java.net.*;
import android.text.method.*;

public class MainActivity extends Activity implements View.OnClickListener
{
	TextView screen;
	EditText input;
	InputStream inputStream;
	OutputStream outputStream;
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
		
    }

	@Override
	public void onClick(View p1)
	{
		Socket s=new Socket();
		try
		{
			s.connect(new InetSocketAddress("127.0.0.1", 3335));
			String text=input.getText().toString();
			PrintWriter pw=new PrintWriter(s.getOutputStream());
			pw.println(text);
			//pw.println("--exit--");
			pw.flush();
			BufferedReader br=new BufferedReader(new InputStreamReader(s.getInputStream()));
			StringBuilder sb=new StringBuilder();
			String line=null;
			while((line=br.readLine())!=null){
				if("--exit--".equals(line))break;
				sb.append(line).append("\n");
			}
			screen.setText(sb.toString());
			screen.append("执行结束");
		}
		catch (Exception e)
		{
			screen.setText(e.getMessage());
		}finally{
			try
			{
				s.close();
			}
			catch (IOException e)
			{}
		}
		/*try
		{
			String text=input.getText().toString();
			if(!text.endsWith("\n"))
				text=text.concat("\n");
			outputStream.write(text.getBytes());
			outputStream.flush();
		}
		catch (IOException e)
		{
			screen.append(e.getMessage());
			screen.append("\n");
		}*/
	}
	void loop(){
		new Thread(){
			public void run(){
				BufferedReader reader=new BufferedReader(new InputStreamReader(inputStream));
				String line=null;
				try
				{
					while ((line = reader.readLine()) != null)
					{
						final String result=line;
						runOnUiThread(new Runnable(){

								@Override
								public void run()
								{
									// TODO: Implement this method
									screen.append(result);
								}
							});
					}
				}
				 catch (final IOException e)
				{
					runOnUiThread(new Runnable(){

							@Override
							public void run()
							{
								// TODO: Implement this method
								screen.append(e.getMessage());
							}
						});
				}finally{
					runOnUiThread(new Runnable(){

							@Override
							public void run()
							{
								// TODO: Implement this method
								screen.append("连接已关闭");
							}
						});
				}
			}
		}.start();
	}
	
}
