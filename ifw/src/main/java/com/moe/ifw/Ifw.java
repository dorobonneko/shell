package com.moe.ifw;
import java.io.InputStream;
import android.content.IntentFilter;
import java.util.Set;
import java.util.HashMap;
import java.util.HashSet;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.function.BiConsumer;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParser;
import java.io.InputStreamReader;
import android.util.Xml;
import java.io.IOException;

public class Ifw {
    private HashMap<String,Set<Component>> list=new HashMap<>();
    public Ifw(){}
    public Set<Component> activitys(){
        return get("activity");
    }
    public Set<Component> services(){
        return get("service");
    }
    public Set<Component> broadcasts(){
        return get("broadcast");
    }
    protected Set<Component> get(String name){
        if(list.containsKey(name))
            return list.get(name);
        Set<Component> set=new HashSet<>();
        list.put(name,set);
        return set;
    }
    public void saveTo(OutputStream o){
        final PrintWriter pw=new PrintWriter(o);
        pw.println("<rules>");
        list.forEach(new BiConsumer<String,Set<Component>>(){

                @Override
                public void accept(String p1, Set<Ifw.Component> p2) {
                    pw.print("<");
                    pw.print(p1);
                    pw.println(" block=\"true\" log=\"false\">");
                    
                    p2.parallelStream().forEach(new java.util.function.Consumer<Component>(){

                            @Override
                            public void accept(Ifw.Component p1) {
                                pw.print("<component-filter name=\"");
                                pw.print(p1.packageName);
                                pw.print("/");
                                pw.print(p1.className);
                                pw.println("\"/>");
                            }
                        });
                    
                    
                    pw.print("</");
                    pw.print(p1);
                    pw.println(">");
                }
            });
        pw.println("</rules>");
    }
    public static Ifw parse(InputStream i) throws XmlPullParserException, IOException{
        XmlPullParser xpp=Xml.newPullParser();
        xpp.setInput(i,"utf-8");
        int type=xpp.next();
        Ifw ifw=new Ifw();
        String class_=null;
        while(type!=XmlPullParser.END_DOCUMENT){
            switch(type){
                case XmlPullParser.START_TAG:
                    switch(xpp.getName()){
                        case "rule":
                            break;
                        case "component-filter":
                            Set<Component> set=ifw.get(class_);
                            Component cpt=new Component();
                            set.add(cpt);
                            String componentName=xpp.getAttributeValue(xpp.getNamespace(),"name");
                            int index=componentName.indexOf("/");
                            cpt.packageName=componentName.substring(0,index);
                            cpt.className=componentName.substring(index+1);
                            break;
                        default:
                            class_=xpp.getName();
                            break;
                    }
                    break;
                case XmlPullParser.END_TAG:
                    break;
            }
            type=xpp.next();
        }
        return ifw;
    }
    public static class Component{
        public String packageName,className;
        @Override
        public boolean equals(Object obj) {
            if(obj instanceof String)
                return obj.toString().equals(packageName+"/"+className);
            if(obj instanceof Component)
                return this.packageName.equals(((Component)obj).packageName)&&this.className.equals(((Component)obj).className);
                return this==obj;
        }
        
    }
}
