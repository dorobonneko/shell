package com.moe.shell;
import java.util.*;
import java.io.*;

public class ActivityInfos
{
	public static List<Process> getProcess(){
		List<Process> list=new ArrayList<>();
		try
		{
			java.lang.Process process=Runtime.getRuntime().exec(new String[]{"su"});
			OutputStream out=process.getOutputStream();
			out.write("dumpsys activity -a p\n".getBytes());
			out.flush();
			BufferedReader br=new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line=null;
			StringBuilder sb=new StringBuilder();
			while((line=br.readLine())!=null){
				line=line.trim();
				if(line.matches("^\\*(APP|PERS)\\*.*?")){
					if(sb.length()>0){
						//已有记录
						//int index=sb.indexOf("data=/data/");
						//index=sb.indexOf("\n",index);
						//String packageName=sb.substring(sb.lastIndexOf("/",index)+1,index);
						//List<Process> list=map.get(packageName);
						//if(list==null)
						//map.put(packageName,list=new ArrayList<Process>());
						list.add(new Process(sb.toString()));
					}
					sb.setLength(0);
					if(line.startsWith("*APP*"))
					sb.append(line).append("\n");
				}else if(line.length()==0){
					//查找完毕，退出
					if(sb.length()>0){
						//int index=sb.indexOf("data=/data/");
						//index=sb.indexOf("\n",index);
						//String packageName=sb.substring(sb.lastIndexOf("/",index)+1,index);
						//List<Process> list=map.get(packageName);
						//if(list==null)
							//map.put(packageName,list=new ArrayList<Process>());
						list.add(new Process(sb.toString()));
					}
					break;
				}else if(sb.length()>0){
					sb.append(line).append("\n");
				}
			}
			out.write("exit\n".getBytes());
			out.flush();
			br.close();
			process.destroy();
		}
		catch (IOException e)
		{}
		return list;
	}
	public class Recent{
		public boolean visible;
		public String packageName;
		public String activity;
	}
	public class Service{}
	public static class Process{
		private Map<String,String> property=new HashMap<>();
		public Process(String content){
			boolean end=false;
			String key=null;
			StringBuilder sb=new StringBuilder();
			for(String line:content.split("\n")){
				if(!end){
				switch(line.substring(0,3)){
					case "dir":
					case "pid":
					case "cac":
						for(String item:line.split(" ")){
							int index=item.indexOf("=");
							property.put(item.substring(0,index),item.substring(index+1));
						}
						break;
					case "oom":
						end=true;
						break;
				}}else{
					switch(line){
						case "Services:":
						case "Recent Tasks:":
						case "Activities:":
							if(key!=null){
								property.put(key,sb.toString());
								sb.setLength(0);
							}
							key=line.substring(0,line.length()-1);
							break;
						default:
						if(line.charAt(0)=='-'){
							if(key!=null)
							sb.append(line).append("\n");
							if(line.startsWith("- Task")){
								toString();
							}
						}else{
							if(key!=null){
								property.put(key,sb.toString());
								sb.setLength(0);
								key=null;
							}
						}
						break;
					}
				}
			}
		}
		public List<Service> getServices(){
			return null;
		}
		public List<Recent> getRecents(){
			return null;
		}
		public boolean isSystemProcess(){
			return property.get("dir").matches("^/(system|vendor).*?");
		}
		public boolean isCached(){
			return Boolean.getBoolean(property.get("cached"));
		}
		public String get(String key){
			return property.get(key);
		}
	}
}
