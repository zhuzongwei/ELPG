package edu.ustc.PowerAnalyser.tools;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.BatteryManager;
import android.util.Log;
import android.widget.Toast;

public class PowerConnectionReceiver extends BroadcastReceiver {
	
	
	@Override
	public void onReceive(Context context, Intent intent) {
		Intent i = new Intent(context, MonitorChargeService.class);
		//i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startService(i);
	}
}
