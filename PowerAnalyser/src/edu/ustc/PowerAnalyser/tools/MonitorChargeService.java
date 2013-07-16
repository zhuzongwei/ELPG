package edu.ustc.PowerAnalyser.tools;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.BatteryManager;
import android.os.IBinder;
import android.widget.Toast;

public class MonitorChargeService extends Service {
	private static String PREFS_NAME="Battery";
	private static String CHARGE_START_TIME = "charge_start_time";
	private static String CHARGE_START_QUANTITY = "charge_start_quantity";
	private static String CHARGE_STOP_TIME = "charge_stop_time";
	private static String CHARGE_STOP_QUANTITY = "charge_stop_quantity"; 
	private static String BATTERY_EC = "Battery_Electric_Current";//电流值
	private static String BATTERY_MAX = "battery_max";
	private static String CAPACITY = "BatteryCapacity";
	private static String MODEL_BASE = "model_base";
	
	public MonitorChargeService() {
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO: Return the communication channel to the service.
		throw new UnsupportedOperationException("Not yet implemented");
	}
	
	@Override
	public void onCreate(){
		super.onCreate();
		
	}
	
	@Override
	public void onStart(Intent intent, int startId){
		super.onStart(intent, startId);
		
		IntentFilter filter = new IntentFilter();
		//获取电池信息
		filter.addAction(Intent.ACTION_BATTERY_CHANGED);
		registerReceiver(batteryChangeReceiver, filter);
		
		this.stopSelf();
		
	}
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		unregisterReceiver(batteryChangeReceiver);
	}
	private BroadcastReceiver batteryChangeReceiver = new BroadcastReceiver(){
    	@Override
    	public void onReceive(Context context, Intent intent){
    		String action = intent.getAction();
    		if(action.equals(Intent.ACTION_BATTERY_CHANGED)){
    			int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, 0);
    	      /*  boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
    	                            status == BatteryManager.BATTERY_STATUS_FULL;
    	    
    	        int chargePlug = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0);
    	        boolean usbCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_USB;
    	        boolean acCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_AC;
    	        Log.i("PowerConnectionReceiver","isCharing:"+isCharging+"\nusbCharge:"+usbCharge+"\nacCharge:"+acCharge);
    	        
    	        Toast.makeText(context, "isCharing:"+isCharging+"\nusbCharge:"+usbCharge+"\nacCharge:"+acCharge, Toast.LENGTH_LONG).show();
    	        Toast.makeText(context, String.valueOf(status), Toast.LENGTH_LONG).show();*/
    	        
    	        SharedPreferences SP_Battery = context.getSharedPreferences(PREFS_NAME, 0);
    	        
    	        Editor editor = SP_Battery.edit();//获取编辑器
    	        //在充电且未充满
    	        if((status == BatteryManager.BATTERY_STATUS_CHARGING)&& (status != BatteryManager.BATTERY_STATUS_FULL))
    	        {
    	        	Toast.makeText(context, "The Battery is charging！", Toast.LENGTH_LONG).show();
    	        	editor.putLong(CHARGE_START_TIME, System.currentTimeMillis());
    	        	int level = intent.getIntExtra("level", 0);
    	        	float chargeStartQuantity = SP_Battery.getInt("BatteryCapacity", 0)*level/100 * 3600;
    	        	editor.putFloat(CHARGE_START_QUANTITY, chargeStartQuantity);
    	        	editor.commit();
    	        	
    	        }else if(status == BatteryManager.BATTERY_STATUS_FULL) //充满电
    	        {
    	        	Toast.makeText(context, "clean the bin！", Toast.LENGTH_LONG).show(); //清除bin
    	        	float BatteryCapacity = SP_Battery.getInt(CAPACITY, 0);
    	        	editor.putFloat(BATTERY_MAX, BatteryCapacity*3600);
    	        	editor.putFloat(MODEL_BASE, 0);
    	        	editor.commit();
    	        	
    	        }else if(status == BatteryManager.BATTERY_STATUS_NOT_CHARGING)//拔掉电源
    	        {
    	        	Toast.makeText(context, "The Battery is not charging！", Toast.LENGTH_LONG).show();
    	        	long now = System.currentTimeMillis();
    	        	editor.putLong(CHARGE_STOP_TIME, now);
    	        	long chargeTime = now - SP_Battery.getLong(CHARGE_START_TIME, 0);
    	        	
    	        	float ec = SP_Battery.getFloat(BATTERY_EC, 0);
    	        	float power_input = (chargeTime/1000)*ec;
    	        	//float chargeStartQuantity = SP_Battery.getFloat(CHARGE_START_QUANTITY, 0);
    	        	float chargeQuantity = SP_Battery.getFloat(BATTERY_MAX, 0);
    	        	chargeQuantity += power_input;
    	        	editor.putFloat(BATTERY_MAX, chargeQuantity);
    	        	//editor.putFloat(CHARGE_STOP_QUANTITY, chargeStopQuantity);
    	        	editor.commit();
    	        	
    	        	
    	        	
    	        }
    	        
    		}
    	}
	};
	
}
