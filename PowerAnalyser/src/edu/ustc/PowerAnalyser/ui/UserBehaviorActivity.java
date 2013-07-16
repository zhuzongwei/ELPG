package edu.ustc.PowerAnalyser.ui;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import edu.ustc.PowerAnalyser.R;
import edu.ustc.PowerAnalyser.R.layout;
import edu.ustc.PowerAnalyser.R.menu;
import edu.ustc.PowerAnalyser.tools.PULLPowerDataParser;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;

public class UserBehaviorActivity extends Activity {

	private static final int UPDATE = 1111;
	private List<Map<String, String>> UserBData;
	private ListView UBList;
	private PULLPowerDataParser parser;
	private SimpleAdapter adapter;
	private ProgressDialog progressDialog = null;
	private String packageName = null;
	private static final int NORMAL = 1;
	private static final int DETAIL = 2;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_user_behavior);
		UBList = (ListView)findViewById(R.id.UBlist);
		//UserBData = new LinkedList<Map<String, String>>();
		parser = new PULLPowerDataParser(this);
		progressDialog = ProgressDialog.show(this, "请稍等...", "加载中...",true);
		Intent data = this.getIntent();
		packageName = data.getStringExtra("PackageName");
		if(packageName==null)
		{
			filterFromFile(NORMAL);
		}
		else
		{
			this.setTitle(packageName);
			filterFromFile(DETAIL);
		}
		UBList.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				if(packageName==null)
				{
					String pkname = UserBData.get(arg2).get("pkname");
					Intent intent = new Intent(UserBehaviorActivity.this,UserBehaviorActivity.class);
					intent.putExtra("PackageName", pkname);
					startActivity(intent);
					
				}
			}

		});
		
	}
	 private Handler mHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			switch(msg.what)
			{
			case UPDATE:
				viewRefresh();
				break;
			}
		}
		
	};
	private void filterFromFile(final int type)
	{
		new Thread(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				super.run();
				try {
					UserBData = parser.readUB(type,packageName);
				} catch (NameNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if(UserBData!=null)
				{
					adapter = new SimpleAdapter(UserBehaviorActivity.this, 
							UserBData, 
							R.layout.ub_item,
							new String[] {"time","op","name"},
							new int[]{R.id.ub_time,R.id.ub_op,R.id.ub_name});
					Message msg = new Message();
					msg.what = UPDATE;
					mHandler.sendMessage(msg);
				}
				
			}}.start();
	}
	
	private void viewRefresh()
	{
		progressDialog.dismiss();
		if(UBList == null||adapter == null)
			return;
		UBList.setAdapter(adapter);
		UBList.invalidate();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.user_behavior, menu);
		return true;
	}

}
