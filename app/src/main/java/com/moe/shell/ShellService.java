package com.moe.shell;
import android.os.*;

public class ShellService extends Service.Stub
{
	public static void main(String args[]){
		Looper.prepare();
		new ShellService();
		Looper.loop();
	}
	public ShellService(){
		
	}
	@Override
	public int getUid() throws RemoteException
	{
		// TODO: Implement this method
		return android.os.Process.myUid();
	}

	@Override
	public String exec(String cmd) throws RemoteException
	{
		// TODO: Implement this method
		return null;
	}

	@Override
	public void kill(String packageName) throws RemoteException
	{
		// TODO: Implement this method
	}

	@Override
	public void forceStop(String packageName) throws RemoteException
	{
		// TODO: Implement this method
	}

	@Override
	public void setInactive(String packageName) throws RemoteException
	{
		// TODO: Implement this method
	}

	@Override
	public void disable(String packageName) throws RemoteException
	{
		// TODO: Implement this method
	}

	@Override
	public void enable(String packageName) throws RemoteException
	{
		// TODO: Implement this method
	}

	@Override
	public void suspend(String packageName) throws RemoteException
	{
		// TODO: Implement this method
	}

	@Override
	public void unsuspend(String packageNmae) throws RemoteException
	{
		// TODO: Implement this method
	}

	@Override
	public int getPid() throws RemoteException
	{
		// TODO: Implement this method
		return android.os.Process.myPid();
	}

	@Override
	public void kill(int pid) throws RemoteException
	{
		// TODO: Implement this method
	}

	@Override
	public void exit() throws RemoteException
	{
		Looper.myLooper().quit();
		System.exit(0);
	}
	
}
