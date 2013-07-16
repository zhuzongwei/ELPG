package edu.ustc.PowerAnalyser.ui;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import edu.ustc.PowerAnalyser.R;
import edu.ustc.PowerAnalyser.chartengine.chart.AppStatisticsActivity;
import edu.ustc.PowerAnalyser.tools.PULLPowerDataParser;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class ApplicationList extends Activity {
	private ListView appList;
	private AppInfoAdapter appAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.application_usage);
		
		// 应用程序列表
		appList = (ListView)findViewById(R.id.applicationList);  
		appAdapter = new AppInfoAdapter(this, getAppListValues(), R.layout.applist_item,   
                new String[]{"icon", "appName", "packageName", "uid","usage"},  
                new int[]{R.id.icon, R.id.appName, R.id.packageName, R.id.uid, R.id.powerUsage});
		
		appList.setAdapter(appAdapter);

        
		appList.setOnItemClickListener(new ItemClick());
	}

	//获取应用的信息
	private ArrayList<HashMap<String, Object>> getAppListValues(){
		ArrayList<HashMap<String, Object>> items = new ArrayList<HashMap<String, Object>>();
		//得到PackageManager对象  
        PackageManager pm = getPackageManager();  
        
        //得到系统安装的所有程序包的PackageInfo对象  
        //List<ApplicationInfo> packs = pm.getInstalledApplications(0);  
        List<PackageInfo> packs = pm.getInstalledPackages(0);  
        PULLPowerDataParser parser = new PULLPowerDataParser(this);
        HashMap<String,String> app = parser.readApps("AppData.xml");
        HashMap<String, Object> os = new HashMap<String, Object>();
        
        
        
        
        
        //添加UID=0的耗电项
        if(app.get("0")!=null)
        {
        Resources res = this.getResources();
        os.put("icon", res.getDrawable(R.drawable.ic_power_system));
        os.put("appName", "Android OS");
        os.put("packageName", "Android Kernel");
        os.put("uid", 0);
        os.put("usage",  new DecimalFormat("0.00").format(Float.parseFloat(app.get("0"))));
        items.add(os);
        }
        Collections.sort(packs, new Comparator<PackageInfo>(){

			@Override
			public int compare(PackageInfo lhs, PackageInfo rhs) {
				// TODO Auto-generated method stub
				if(lhs.applicationInfo.uid > rhs.applicationInfo.uid)
					return 1;
				else if (lhs.applicationInfo.uid < rhs.applicationInfo.uid)
					return -1;
				else
					return 0;
			}
    	});
        Iterator iter = app.entrySet().iterator();
        StringBuffer name;
        StringBuffer pack_name;
        HashMap<String, Object> map ;
        while (iter.hasNext()) {
        	
        	Map.Entry entry = (Map.Entry) iter.next();
        	String uid = (String) entry.getKey();
        	int uid_int = Integer.parseInt(uid);
        	if(uid_int == 0)
        		continue;
        	int size = packs.size();
        	int count = 0;
        	map = new HashMap<String, Object>();
        	name = new StringBuffer();
        	pack_name = new StringBuffer();
        	for(int i=0;i<size;i++ )
        	{
        		if(uid_int == packs.get(i).applicationInfo.uid)
        		{
        			name.append(packs.get(i).applicationInfo.loadLabel(pm)+"\n");
        			pack_name.append(packs.get(i).applicationInfo.packageName+"\n");
        			count++;
        		}
        		else if (uid_int < packs.get(i).applicationInfo.uid || i==size-1)
        		{
        			if(count <= 1)
        			{
        				map.put("icon", packs.get(i-1).applicationInfo.loadIcon(pm));//图标
        				map.put("appName", packs.get(i-1).applicationInfo.loadLabel(pm));//应用程序名称
        				map.put("packageName", packs.get(i-1).applicationInfo.packageName);//应用程序包名
        			}
        			else
        			{
        				map.put("icon", Resources.getSystem().getDrawable(android.R.drawable.sym_def_app_icon));
        				map.put("appName", name.toString());//应用程序名称 
        				map.put("packageName", pack_name.toString());//应用程序包名
        			}
                    map.put("uid",  uid_int); //应用程序uid
        			map.put("usage",  new DecimalFormat("0.00").format(Float.parseFloat((String)entry.getValue())));
        			items.add(map);
        			break;
        		}
        	}
        	
        }
        
/*        for(PackageInfo pi:packs){  
            HashMap<String, Object> map = new HashMap<String, Object>();             
            if(app.get(String.valueOf(pi.applicationInfo.uid))!= null){
            	
                //这将会显示所有安装的应用程序，包括系统应用程序  
                map.put("icon", pi.applicationInfo.loadIcon(pm));//图标  
                map.put("appName", pi.applicationInfo.loadLabel(pm));//应用程序名称  
                map.put("packageName", pi.applicationInfo.packageName);//应用程序包名
                map.put("uid", pi.applicationInfo.uid); //应用程序uid
    			map.put("usage",  new DecimalFormat("0.00").format(Float.parseFloat(app.get(String.valueOf(pi.applicationInfo.uid).toString()))));
                
                //循环读取并存到HashMap中，再增加到ArrayList上，一个HashMap就是一项  
    			
                items.add(map);  
            }

        } */ 
        
        if(!items.isEmpty()){
        	Collections.sort(items, new Comparator<HashMap<String, Object>>(){
        		@Override
        		public int compare(HashMap<String, Object> object1, HashMap<String, Object> object2){
        			float o1 = Float.parseFloat(object1.get("usage").toString());
        			float o2 = Float.parseFloat(object2.get("usage").toString());

        			if(o1>o2)
        				return (int)-1;
        			else if(o1 == o2)
        				return (int)0;
        			else
        				return (int)1;
        		
        		}
        	});
        }
        return items;
	}
}

class ItemClick implements OnItemClickListener{
	@Override
	public void onItemClick(AdapterView<?> parent, View arg1, int position, long id){
		ListView lview=(ListView)parent;
		//PackageInfo pi = (PackageInfo)lview.getItemAtPosition(position);
		HashMap<String, Object> items = (HashMap<String, Object>)lview.getItemAtPosition(position);
		Log.v("appUID","clickUID:"+items.get("uid"));
	    Intent intent = new Intent();
		intent.setClass(lview.getContext(), AppStatisticsActivity.class);
		Bundle bundle = new Bundle();
		bundle.putString("uid", items.get("uid").toString());
		intent.putExtras(bundle);
		lview.getContext().startActivity(intent);
		//Toast.makeText(lview.getContext(), items.get("uid").toString(), 1).show();
	}
	
}

