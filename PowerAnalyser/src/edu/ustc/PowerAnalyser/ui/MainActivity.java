package edu.ustc.PowerAnalyser.ui;


import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



import org.taptwo.android.widget.TitleFlowIndicator;
import org.taptwo.android.widget.ViewFlow;

import com.android.internal.os.PowerProfile;

//import edu.ustc.BatteryAnalyser.chartengine.chart.IChart;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;

import android.content.BroadcastReceiver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Context;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;

import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import android.widget.TextView;
import android.widget.Toast;


import edu.ustc.PowerAnalyser.R;

import edu.ustc.PowerAnalyser.chartengine.chart.BatteryStatisticsActivity;
import edu.ustc.PowerAnalyser.chartengine.chart.ComponentsLineChartActivity;
import edu.ustc.PowerAnalyser.chartengine.chart.ComponentsPieChartActivity;
import edu.ustc.PowerAnalyser.chartengine.chart.ModelsComparisonChartActivity;
import edu.ustc.PowerAnalyser.log.LogHunterService;
import edu.ustc.PowerAnalyser.powerme.PowerAnalyserService;


public class MainActivity extends Activity {
	
	private static final String APP_SD_CACHE = "PowerAnalyserCache";
	private static final String PREFS_NAME="Battery";
	private static final String CAPACITY = "BatteryCapacity";
	private static final String BATTERY_EC = "Battery_Electric_Current";//电流值;
	//private static final String FIRST_QUANTITY = "First_Quantity"; //第一次使用时的电量
	private static String BATTERY_MAX = "battery_max";
	private static final String SE_PREFS="PowerService";
	private static final String SE_TAG = "isStart";
	private static String LOG_PREF = "LogHunterService";
	private static String LOG_TAG = "isLog";
	private static final int dialog1 =1;
	private int BatteryCapacity = 0;
	private float ChargeCurrent = 0;
	
	private ViewFlow viewFlow;
	private ListView reportList;
	
	private String[] mMenuText;
	private String[] mMenuSummary;

	private String  NAME = "name";
	private String  DESC = "desc";
	
	
	
	/** Called when the activity is first created. */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.title_layout);
		//Restore preferences		
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		BatteryCapacity = settings.getInt(CAPACITY, 0);
		ChargeCurrent = settings.getFloat(BATTERY_EC, 0);
		
		if(BatteryCapacity==0||ChargeCurrent==0){
			
			showDialog(dialog1);
			Toast.makeText(this, "The Application is first run", Toast.LENGTH_LONG).show();
			Editor editor = settings.edit();
			editor.putBoolean(LOG_TAG, true);
			editor.commit();
		}
		
		CreateSDDir(APP_SD_CACHE);
		
		
		viewFlow = (ViewFlow) findViewById(R.id.viewflow);
		ViewAdapter adapter = new ViewAdapter(this);
		viewFlow.setAdapter(adapter);
		TitleFlowIndicator indicator = (TitleFlowIndicator) findViewById(R.id.viewflowindic);
		indicator.setTitleProvider(adapter);
		viewFlow.setFlowIndicator(indicator);		
			
	}
	public void CreateSDDir(String dir){
		boolean sdCardExist = Environment.getExternalStorageState().equals(  
                android.os.Environment.MEDIA_MOUNTED);
		String SDCardRoot = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator;
        File file = new File(SDCardRoot + dir + File.separator);
        if(!sdCardExist)
        	return;
        if(!file.exists())
            file.mkdir();  //如果不存在则创建
        else{
            return;
        }
    }
	
	@Override
		protected void onResume(){
		super.onResume();
		IntentFilter filter = new IntentFilter();
		//获取电池信息
		filter.addAction(Intent.ACTION_BATTERY_CHANGED);
		registerReceiver(mBroadcastReceiver, filter);
    	Button btn = (Button)findViewById(R.id.PowerDetial);
    	btn.setOnClickListener(new View.OnClickListener(){
    		@Override
    		public void onClick(View arg0){
    			Intent intent = new Intent(arg0.getContext(), BatteryStatisticsActivity.class);
    			//Intent intent = new Intent(arg0.getContext(), BatteryActivity.class);
				startActivity(intent);
    			
    		}
    	});
		
		//显示耗电基准信息
		getPowerProfile(this);
		
		//图表分析列表
		reportListView();
	}
	@Override
	protected void onPause(){
		super.onPause();
		unregisterReceiver(mBroadcastReceiver);
	}
	
	@Override
	protected Dialog onCreateDialog(int id){
		switch(id){
		case 1: return entryDialog(MainActivity.this);
		}
		return null;
	}


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event){
    	if(keyCode == KeyEvent.KEYCODE_BACK){
    		new AlertDialog.Builder(this).setTitle("退出程序")
    		                             .setMessage("是否要退出？")
    									 .setPositiveButton("确定", new DialogInterface.OnClickListener() {
											
											@Override
											public void onClick(DialogInterface dialog, int which) {
												
												System.exit(0);
											}
										}).setNegativeButton("取消", new DialogInterface.OnClickListener() {
											
											@Override
											public void onClick(DialogInterface dialog, int which) {
												// TODO Auto-generated method stub
												
											}
										}).create().show();
    	}
    	return super.onKeyDown(keyCode, event);
    }

	
	@Override
	protected void onStop(){
		super.onStop(); 
	}

	private List<Map<String, String>> getList1Values() {
		List<Map<String, String>> values = new ArrayList<Map<String, String>>();
		int length = mMenuText.length;
		for (int i = 0; i < length; i++) {
			Map<String, String> v = new HashMap<String, String>();
			v.put(NAME, mMenuText[i]);
			v.put(DESC, mMenuSummary[i]);
			values.add(v);
		}
		return values;
	}

	
	
	private Dialog entryDialog(Context context){
		
		LayoutInflater inflater = LayoutInflater.from(this);
		
		final View textEntryview = inflater.inflate(R.layout.dialog_text_entry, null);
		final EditText capacity = (EditText)textEntryview.findViewById(R.id.capacity_edit);
		final EditText current = (EditText)textEntryview.findViewById(R.id.current_edit);
		
		AlertDialog.Builder builder= new AlertDialog.Builder(context);
		builder.setTitle(R.string.dialog_msg);
		builder.setView(textEntryview);
		builder.setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener(){
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				
				if(capacity.getText()!=null && current.getText()!=null){
					int batteryCapacity = Integer.parseInt(capacity.getText().toString());
					float chargeEC = Float.parseFloat(current.getText().toString());
					
					IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
				    Intent BSintent = MainActivity.this.registerReceiver(null, ifilter);
				    
			    	int level = BSintent.getIntExtra("level", 0);
			    	int scale = BSintent.getIntExtra("scale", 100);
			    	
					SharedPreferences settings = getSharedPreferences(PREFS_NAME,0);
					SharedPreferences.Editor editor = settings.edit();
					
					float firstQuantity =  batteryCapacity * ((float)level / scale) * 3600;
					
					editor.putInt(CAPACITY, batteryCapacity);
					editor.putFloat(BATTERY_EC, chargeEC);
					editor.putFloat(BATTERY_MAX, firstQuantity);
					editor.commit();			
					}

			}
		});
		return builder.create();
	}
	
	public static void startPowerServiceRepeat(Context ctx){
		Log.v("MainActivity", "Start Alarm");
    	AlarmManager am = getAlarmManager(ctx);
        //Intent intent =new Intent(ctx,PowerDataService.class);
    	Intent intentBatteryUsage = new Intent(ctx,PowerAnalyserService.class);
    	intentBatteryUsage.setAction("repeating");
		/*intentBatteryUsage.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK 
				                  | Intent.FLAG_ACTIVITY_MULTIPLE_TASK
				                  | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
		                          | Intent.FLAG_ACTIVITY_NO_HISTORY);*/
        PendingIntent pendingIntent = PendingIntent.getService(ctx, 1, intentBatteryUsage, 0);
     
        //开始时间
        long firstime=SystemClock.elapsedRealtime();
     
        
        //1秒一个周期，不停地启动服务
        am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, 0, 1000, pendingIntent);
    }
	
	
    public static void stopPowerService(Context ctx){
	    AlarmManager am = getAlarmManager(ctx);
        // 取消时注意必须与设置时一致,这样才要正确取消
	    //Intent intent = new Intent(ctx,PowerDataService.class);  
	    Intent intentBatteryUsage = new Intent(ctx,
	    		PowerAnalyserService.class);
	    intentBatteryUsage.setAction("repeating");
		/*intentBatteryUsage.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK 
				                  | Intent.FLAG_ACTIVITY_MULTIPLE_TASK
				                  | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
		                          | Intent.FLAG_ACTIVITY_NO_HISTORY);*/
	    PendingIntent pendingIntent = PendingIntent.getService(ctx, 1, intentBatteryUsage, 0);
	    am.cancel(pendingIntent);
    }
   
    
    public static AlarmManager getAlarmManager(Context ctx){
		return (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
	}
    
    //获取电池信息
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver(){
    	@Override
    	public void onReceive(Context context, Intent intent){
    		String action = intent.getAction();
			SharedPreferences settings = getSharedPreferences(PREFS_NAME,0);
			int capacity = settings.getInt(CAPACITY, 0);
    		if(action.equals(Intent.ACTION_BATTERY_CHANGED)){
    			int status = intent.getIntExtra("status", 0);
    			int health = intent.getIntExtra("health", 0);
    			boolean present = intent.getBooleanExtra("present", false);
    			int level = intent.getIntExtra("level", 0);
    			int scale = intent.getIntExtra("scale", 0);
    			int icon_small = intent.getIntExtra("icon-small", 0);
    			int plugged = intent.getIntExtra("plugged", 0);
    			int voltage = intent.getIntExtra("voltage", 0);
    			int temperature = intent.getIntExtra("temperature", 0);
    			String technology = intent.getStringExtra("technology");
				String working = "";
    			if(present){
    				working = "正在使用";
    			}else
    			{
    				working = "没有使用";
    			}
    			
    			String statusString = "";
    			
    			switch(status){
    			case BatteryManager.BATTERY_STATUS_UNKNOWN:
    				statusString = "unknown";
    				break;
    			case BatteryManager.BATTERY_STATUS_CHARGING:
    				statusString = "正在充电";
    				break;
    			case BatteryManager.BATTERY_STATUS_DISCHARGING:
    				statusString = "放电状态";
    				break;
    			case BatteryManager.BATTERY_STATUS_NOT_CHARGING:
    				statusString = "未充电";
    				break;
    			case BatteryManager.BATTERY_STATUS_FULL:
    				statusString = "充满电";
    				break;
    			}
    			
    			String healthString = "";
    			switch(health){
    			case BatteryManager.BATTERY_HEALTH_UNKNOWN:
    				healthString = "unknown";
    				break;
    			case BatteryManager.BATTERY_HEALTH_GOOD:
    				healthString = "状态良好";
    				break;
    			case BatteryManager.BATTERY_HEALTH_OVERHEAT:
    				healthString = "电池过热";
    				break;
    			case BatteryManager.BATTERY_HEALTH_DEAD:
    				healthString = "电池没电";
    				break;
    			case BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE:
    				healthString = "电池电压过高";
    				break;
    			case BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE:
    				healthString = "unspecified failure";
    				break;
    				
    			}
    			
    			String acString = "";
    			switch(plugged){
    			case BatteryManager.BATTERY_PLUGGED_AC:
    				acString = "AC充电";
    				break;
    			case BatteryManager.BATTERY_PLUGGED_USB:
    				acString = "USB充电";
    				break;
    			}
    			TextView batteryStatus =(TextView)findViewById(R.id.batteryStatus);
    			batteryStatus.setText(statusString);
    			TextView batteryHealth =(TextView)findViewById(R.id.batteryHealth);
    			batteryHealth.setText(healthString);
    			TextView batteryPresent = (TextView)findViewById(R.id.batteryPresent);
    			batteryPresent.setText(working);
    			TextView batteryLevel = (TextView)findViewById(R.id.batteryLevel);
    			batteryLevel.setText(String.valueOf(level)+"%");
    			TextView batteryScale = (TextView)findViewById(R.id.batteryScale);
    			batteryScale.setText(String.valueOf(capacity)+"mAh");
    			TextView batteryPlugged = (TextView)findViewById(R.id.batteryPlugged);
    			batteryPlugged.setText(String.valueOf(acString));
    			TextView batteryVoltage = (TextView)findViewById(R.id.batteryVoltage);
    			if(voltage<10)
    				batteryVoltage.setText(String.valueOf(voltage)+"V");
    			else
    				batteryVoltage.setText(String.valueOf((float)voltage/1000)+"V");
    			TextView batteryTemperature = (TextView)findViewById(R.id.batteryTemperature);
    			batteryTemperature.setText(String.valueOf(temperature/10.0)+"摄氏度");
    			TextView batteryTechnology = (TextView)findViewById(R.id.batteryTechnology);
    			batteryTechnology.setText(String.valueOf(technology));
    			
    		}
    		
    	}    	
    };
    
    //获取电池基准信息
    private void getPowerProfile(Context ctx){
    	PowerProfile mPowerProfile = new PowerProfile(ctx);
    	//TextView powerProfileTextView = (TextView)findViewById(R.id.powerProfileTextView);
    	//powerProfileTextView.setMovementMethod(ScrollingMovementMethod.getInstance());
    	TextView batteryCapacity = (TextView)findViewById(R.id.batteryCapacity);    	
    	batteryCapacity.setText(Double.toString(mPowerProfile.getAveragePower(PowerProfile.POWER_BATTERY_CAPACITY))+" mAh");
    	TextView screenOn =(TextView)findViewById(R.id.screenOn);
    	screenOn.setText(Double.toString(mPowerProfile.getAveragePower(PowerProfile.POWER_SCREEN_ON))+" mAh");
    	TextView screenFull =(TextView)findViewById(R.id.screenFull);
    	screenFull.setText(Double.toString(mPowerProfile.getAveragePower(PowerProfile.POWER_SCREEN_FULL))+" mAh");
    	TextView bluetoothOn = (TextView)findViewById(R.id.bluetoothOn);
    	bluetoothOn.setText(Double.toString(mPowerProfile.getAveragePower(PowerProfile.POWER_BLUETOOTH_ON))+" mAh");
    	TextView bluetoothActive = (TextView)findViewById(R.id.bluetoothActive);
    	bluetoothActive.setText(Double.toString(mPowerProfile.getAveragePower(PowerProfile.POWER_BLUETOOTH_ACTIVE))+" mAh");
    	TextView bluetoothAt = (TextView)findViewById(R.id.bluetoothAt);
    	bluetoothAt.setText(Double.toString(mPowerProfile.getAveragePower(PowerProfile.POWER_BLUETOOTH_AT_CMD))+" mAh");
    	TextView wifiOn = (TextView)findViewById(R.id.wifiOn);
    	wifiOn.setText(Double.toString(mPowerProfile.getAveragePower(PowerProfile.POWER_WIFI_ON))+" mAh");
    	TextView wifiActive = (TextView)findViewById(R.id.wifiActive);
    	wifiActive.setText(Double.toString(mPowerProfile.getAveragePower(PowerProfile.POWER_WIFI_ACTIVE))+" mAh");
    	TextView wifiScan = (TextView)findViewById(R.id.wifiScan);
    	wifiScan.setText(Double.toString(mPowerProfile.getAveragePower(PowerProfile.POWER_WIFI_SCAN))+" mAh");
    	TextView dspAudio = (TextView)findViewById(R.id.dspAudio);
    	dspAudio.setText(Double.toString(mPowerProfile.getAveragePower(PowerProfile.POWER_AUDIO))+" mAh");
    	TextView dspVideo = (TextView)findViewById(R.id.dspVideo);
    	dspVideo.setText(Double.toString(mPowerProfile.getAveragePower(PowerProfile.POWER_VIDEO))+" mAh");
    	TextView radioOn = (TextView)findViewById(R.id.radioOn);
    	radioOn.setText(Double.toString(mPowerProfile.getAveragePower(PowerProfile.POWER_RADIO_ON))+" mAh");
    	TextView radioActive = (TextView)findViewById(R.id.radioActive);
    	radioActive.setText(Double.toString(mPowerProfile.getAveragePower(PowerProfile.POWER_RADIO_ACTIVE))+" mAh");
    	TextView radioScan = (TextView)findViewById(R.id.radioScanning);
    	radioScan.setText(Double.toString(mPowerProfile.getAveragePower(PowerProfile.POWER_RADIO_SCANNING))+" mAh");
    	TextView gpsOn = (TextView)findViewById(R.id.gpsOn);
    	gpsOn.setText(Double.toString(mPowerProfile.getAveragePower(PowerProfile.POWER_GPS_ON))+" mAh");
    	TextView cpuIdle = (TextView)findViewById(R.id.cpuIdle);
    	cpuIdle.setText(Double.toString(mPowerProfile.getAveragePower(PowerProfile.POWER_CPU_IDLE))+" mAh");
    	TextView cpuAwake = (TextView)findViewById(R.id.cpuAwake);
    	cpuAwake.setText(Double.toString(mPowerProfile.getAveragePower(PowerProfile.POWER_CPU_AWAKE))+" mAh");
    	
    	TextView cpuSpeeds = (TextView)findViewById(R.id.cpuSpeeds);
    	cpuSpeeds.setText(mPowerProfile.getNumSpeedSteps()+"个工作频率");	
    	
    	final int speedSteps = mPowerProfile.getNumSpeedSteps();
    	final double[] powerCpuNormal = new double[speedSteps];
    	String cpuRange = "";
    	for(int p=0; p<speedSteps; p++){
    		powerCpuNormal[p] = mPowerProfile.getAveragePower(PowerProfile.POWER_CPU_ACTIVE, p);
    		cpuRange = cpuRange+ Double.toString(powerCpuNormal[p])+" mAh\n";
    	}
    	TextView cpuActive = (TextView)findViewById(R.id.cpuActive);
    	cpuActive.setText(cpuRange);
    	
    	
    }
    
    private void reportListView(){
    	
    	reportList = (ListView) findViewById(R.id.reportList);
		
		mMenuText = new String[] { "模型对比", "应用分析","模块分布", "模块分布详情", "用户行为报告"};
		mMenuSummary = new String[] { "对比驱动得到的数据和模型计算的数据", "分析每个应用的耗电情况","部件耗电的比重", "各部件的耗电详情","展示用户使用各App的情况" };
		// Create an ArrayAdapter, that will actually make the Strings above
		// appear in the ListView
		
		//用SimpleAdapter填充listView
		reportList.setAdapter(new SimpleAdapter(this, getList1Values(),
				android.R.layout.simple_list_item_2, new String[] {
						NAME, DESC }, new int[] {
						android.R.id.text1, android.R.id.text2 }));
		

		

		//监听点击事件
		reportList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> l, View v, int position,long id) 
			{
				Intent intent = null;
				// TODO Auto-generated method stub
				switch (position) {

				case 0:
					intent = new Intent(reportList.getContext(), ModelsComparisonChartActivity.class);
					startActivity(intent);
					break;
				case 1:
					intent = new Intent(reportList.getContext(), ApplicationList.class);
					startActivity(intent);
					break;
				case 2:
					intent = new Intent(reportList.getContext(), ComponentsPieChartActivity.class);
					startActivity(intent);
					break;
				case 3:
					intent = new Intent(reportList.getContext(), ComponentsLineChartActivity.class);
					startActivity(intent);
					break;
				case 4:
					intent = new Intent(reportList.getContext(), UserBehaviorActivity.class);
					startActivity(intent);
					break;
					
				}
				
			}
		});
    }
    
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}
	


	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		menu.clear();
		SharedPreferences SP_service = this.getSharedPreferences(SE_PREFS, 0);
		if(!SP_service.getBoolean(SE_TAG, false))
			menu.add(0, Menu.FIRST, 0, "开始记录");
		else
			menu.add(0, Menu.FIRST, 0, "停止记录");
		menu.add(0, Menu.FIRST + 1, 0, "设置");
		return super.onPrepareOptionsMenu(menu);
		
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		SharedPreferences SP_service = this.getSharedPreferences(SE_PREFS, 0);
		SharedPreferences SP_log = this.getSharedPreferences(LOG_PREF, 0);
		Editor editor = SP_service.edit();
		Editor logeditor = SP_log.edit();
		Intent service = new Intent(this,LogHunterService.class);
		switch (item.getItemId()) {
		case 1:
			if(!SP_service.getBoolean(SE_TAG, false))
			{		
				startPowerServiceRepeat(this);
				startService(service);
				editor.putBoolean(SE_TAG, true);
				logeditor.putBoolean(LOG_TAG, true);
				editor.commit();
				logeditor.commit();
			}
			else
			{
				stopPowerService(this);
				stopService(service);
				editor.putBoolean(SE_TAG, false);
				logeditor.putBoolean(LOG_TAG, false);
				editor.commit();
				logeditor.commit();
			}
			break;
		case 2:
			Intent intent = new Intent(this,Settings.class);
			startActivity(intent);
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	

	
}

