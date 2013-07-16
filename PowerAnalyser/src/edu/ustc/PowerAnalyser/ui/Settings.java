package edu.ustc.PowerAnalyser.ui;

import java.io.IOException;

import edu.ustc.PowerAnalyser.R;
import edu.ustc.PowerAnalyser.log.LogHunterService;
import edu.ustc.PowerAnalyser.powerme.PowerAnalyserService;
import android.os.Bundle;
import android.os.SystemClock;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;
import android.view.Menu;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class Settings extends Activity {

	private CheckBox isLog;
	private CheckBox clickable;
	private CheckBox showThis;
	private LogHunterService log;
	private static String PREFS_NAME = "Battery";
	private static String LOG_TAG = "isLog";
	private static String SHOW_TAG = "showThis";
	private SharedPreferences SP_log;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
		isLog = (CheckBox)findViewById(R.id.islog);
		clickable = (CheckBox)findViewById(R.id.clickbale);
		showThis = (CheckBox)findViewById(R.id.showThis);
		SP_log = getSharedPreferences(PREFS_NAME, 0);
		if(SP_log.getBoolean(LOG_TAG, false))			
		{
			isLog.setChecked(true);
		}
		else
		{
			isLog.setChecked(false);
		}
		if(SP_log.getBoolean(SHOW_TAG, false))
		{
			showThis.setChecked(true);
		}
		else
		{
			showThis.setChecked(false);
		}
		showThis.setOnCheckedChangeListener(new OnCheckedChangeListener(){

			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				// TODO Auto-generated method stub
				Editor editor = SP_log.edit();
				if(arg1)
					editor.putBoolean(SHOW_TAG, true);
				else
					editor.putBoolean(SHOW_TAG, false);
				editor.commit();
			}});
		
		
		
		isLog.setOnCheckedChangeListener(new OnCheckedChangeListener(){

			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				// TODO Auto-generated method stub
				Editor editor = SP_log.edit();
				if(arg1)
				{
					editor.putBoolean(LOG_TAG, true);
					//startLogHuntServiceRepeat(Settings.this);
				}
				else
				{
					editor.putBoolean(LOG_TAG, false);
					//stopLogHuntService(Settings.this);
				}
				editor.commit();
			}});
	}
	

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		
	}


	public static void startLogHuntServiceRepeat(Context ctx) {
		Log.v("MainActivity", "Start Alarm");
		AlarmManager am = getAlarmManager(ctx);
		
		Intent intentBatteryUsage = new Intent(ctx, LogHunterService.class);
		intentBatteryUsage.setAction("repeating");
		PendingIntent pendingIntent = PendingIntent.getService(ctx, 1,
				intentBatteryUsage, 0);

		// 开始时间
		long firstime = SystemClock.elapsedRealtime();

		// 1分钟一个周期，不停地启动服务
		am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, firstime, 60*1000,
				pendingIntent);
	}
	public static void stopLogHuntService(Context ctx){
	    AlarmManager am = getAlarmManager(ctx);
        // 取消时注意必须与设置时一致,这样才要正确取消
	    //Intent intent = new Intent(ctx,PowerDataService.class);  
	    Intent intentBatteryUsage = new Intent(ctx,
	    		LogHunterService.class);
	    intentBatteryUsage.setAction("repeating");
		/*intentBatteryUsage.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK 
				                  | Intent.FLAG_ACTIVITY_MULTIPLE_TASK
				                  | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
		                          | Intent.FLAG_ACTIVITY_NO_HISTORY);*/
	    PendingIntent pendingIntent = PendingIntent.getService(ctx, 1, intentBatteryUsage, 0);
	    am.cancel(pendingIntent);
    }
	
	public static AlarmManager getAlarmManager(Context ctx) {
		return (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
	}

}
