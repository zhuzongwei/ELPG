package edu.ustc.PowerAnalyser.powerme;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.android.internal.app.IBatteryStats;
import com.android.internal.os.BatteryStatsImpl;
import com.android.internal.os.PowerProfile;

import edu.ustc.PowerAnalyser.R;
import edu.ustc.PowerAnalyser.tools.PULLPowerDataParser;
import edu.ustc.PowerAnalyser.tools.PowerData;
import edu.ustc.PowerAnalyser.tools.PowerData.PowerType;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.hardware.SensorManager;
import android.net.ConnectivityManager;
import android.os.BatteryManager;
import android.os.BatteryStats;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Process;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.BatteryStats.Uid;
import android.util.Log;
import android.util.SparseArray;

public class PowerAnalyserService extends Service {

	private static final boolean DEBUG = false;
	private static final boolean LIST_DEBUG = false;
	private static final String TAG = "PowerAnalyserService";
	private static String PREFS_NAME="Battery";
	private static String CHARGE_START_TIME = "charge_start_time";
	private static String BATTERY_EC = "Battery_Electric_Current";//电流值
	private static String BATTERY_MAX = "battery_max";
	private static String MODEL_BASE = "model_base";

	// private final List<BatterySipper> mUsageList = new
	// ArrayList<BatterySipper>();
	// private ArrayList<BatterySipper> mRequestQueue = new
	// ArrayList<BatterySipper>();
	private List<PowerData> applist = new LinkedList<PowerData>();
	private List<PowerData> component = new LinkedList<PowerData>();
	private PULLPowerDataParser parser;
	SimpleDateFormat timeFormatter;
	String curTime;

	IBatteryStats mBatteryInfo;
	BatteryStatsImpl mStats;
	private int mStatsType = BatteryStats.STATS_SINCE_CHARGED;
	private double mMaxPower = 1;
	private double mTotalPower = 0;
	private PowerProfile mPowerProfile;
	private double appPower;
	private double cpuPower;
	private double phonePower;
	private double radioPower;
	private double wifiPower;
	private double mobileTransPower;
	private double bluetoothPower;
	private double screenPower;
	private double sensorPower;
	private double idlePower;

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		if (DEBUG)
			Log.v("PowerAnalyserService", "OnCreate");
		mBatteryInfo = IBatteryStats.Stub.asInterface(ServiceManager
				.getService("batteryinfo"));
		mPowerProfile = new PowerProfile(this);
		parser = new PULLPowerDataParser(this, "");

	}

	@Override
	public void onStart(Intent intent, int startId) {
		// TODO Auto-generated method stub
		super.onStart(intent, startId);
		if (DEBUG)
			Log.v("PowerAnalyserService", "OnStart");
		timeFormatter = new SimpleDateFormat("yy-MM-dd HH:mm:ss");
		curTime = timeFormatter.format(new java.util.Date());

		refreshStats();
	}

	private void refreshStats() {
		applist.clear();
		component.clear();
		appPower = 0;
		sensorPower = 0;
		cpuPower = 0;
		phonePower = 0;
		radioPower = 0;
		wifiPower = 0;
		bluetoothPower = 0;
		screenPower = 0;
		idlePower = 0;
		mTotalPower = 0;
		mMaxPower = 1;

		load();

		processAppUsage();
		processMiscUsage();

		if (DEBUG) {
			Log.v("Result", "MaxPower: " + mMaxPower + " TotalPower:"
					+ mTotalPower + "\n");
			Log.v("applist", String.valueOf(applist.size()));
		}

		SharedPreferences SP_base = this.getSharedPreferences(PREFS_NAME, 0);
		if(SP_base.getFloat(MODEL_BASE, -1) < 0)
		{
			Editor baseEditor = SP_base.edit();
			baseEditor.putFloat(MODEL_BASE, (float)mTotalPower);
			baseEditor.commit();
			return;
		}
		
		for (PowerData pd : applist) {
			double percent = Double.parseDouble(pd.getPower()) * 100
					/ mTotalPower;
			pd.setPercent(String.valueOf(percent));
			parser.writeLists(pd.getPower(), pd.getTime(),
					PULLPowerDataParser.APP_UID_LIST, pd.getMark());
			if(LIST_DEBUG)
				Log.d("applist", pd.getName() + " : " + pd.getPercent());
		}
		for (PowerData pd : component) {
			double percent = Double.parseDouble(pd.getPower()) * 100
					/ mTotalPower;
			pd.setPercent(String.valueOf(percent));
			parser.writeLists(pd.getPercent(), pd.getTime(),
					PULLPowerDataParser.COMPONENT_LIST, pd.getType());
			if(LIST_DEBUG)
				Log.d("component", pd.getType() + " : " + pd.getPercent());
		}

		parser.writeComponent(applist, curTime,
				PULLPowerDataParser.APP_COMPONENT);
		parser.writeComponent(component, curTime,
				PULLPowerDataParser.HW_COMPONENET);
		IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
		Intent BSintent = this.registerReceiver(null, ifilter);
		String batteryLevel = getBatteryPercentage(BSintent);
		// String batteryStatus = getBatteryStatus(getResources(),BSintent);
		parser.writeLists(batteryLevel, curTime,
				PULLPowerDataParser.BATTERY_LIST);
		DecimalFormat df = new DecimalFormat("0.000000000");
		String total = null;
		SharedPreferences SP_Battery = getSharedPreferences(PREFS_NAME, 0);   
		float battery_max = SP_Battery.getFloat(BATTERY_MAX, 0);
		if(!isChargeing(BSintent))			
		{	
			float base = SP_base.getFloat(MODEL_BASE, -1);		
			total = df.format(battery_max-mTotalPower+base); 
		}
		else
		{
			long now = System.currentTimeMillis();
        	long chargeTime = now - SP_Battery.getLong(CHARGE_START_TIME, 0);      	
        	float ec = SP_Battery.getFloat(BATTERY_EC, 0);
        	float power_input = (chargeTime/1000)*ec;
        	float base = SP_base.getFloat(MODEL_BASE, -1);
        	total = df.format(battery_max+power_input-mTotalPower+base); 
		}
		parser.writeLists(total, curTime,
				PULLPowerDataParser.MODULE_TOTAL_LIST);

	}

	@SuppressLint("Recycle")
	private void load() {
		try {
			byte[] data = mBatteryInfo.getStatistics();
			
			Parcel parcel = Parcel.obtain();
			parcel.unmarshall(data, 0, data.length);
			parcel.setDataPosition(0);
			mStats = com.android.internal.os.BatteryStatsImpl.CREATOR
					.createFromParcel(parcel);
			mStats.distributeWorkLocked(BatteryStats.STATS_SINCE_CHARGED);
		} catch (RemoteException e) {
			Log.e(TAG, "RemoteException:", e);
		}
	}

	public static String getBatteryPercentage(Intent batteryChangedIntent) {
		int level = batteryChangedIntent.getIntExtra("level", 0);
		int scale = batteryChangedIntent.getIntExtra("scale", 100);
		return String.valueOf(level * 100 / scale);
	}
	public static boolean isChargeing(Intent batteryChangedIntent)
	{
		// BatteryManager.BATTERY_STATUS_CHARGING 表示是充电状态  
        // BatteryManager.BATTERY_STATUS_DISCHARGING 放电中  
        // BatteryManager.BATTERY_STATUS_NOT_CHARGING 未充电  
        // BatteryManager.BATTERY_STATUS_FULL 电池满  
		int status = batteryChangedIntent.getIntExtra("status", BatteryManager.BATTERY_STATUS_UNKNOWN);
		if(status == BatteryManager.BATTERY_STATUS_CHARGING)
			return true;
		else
			return false;
	}

	public static String getBatteryStatus(Resources res,
			Intent batteryChangedIntent) {
		final Intent intent = batteryChangedIntent;

		int plugType = intent.getIntExtra("plugged", 0);
		int status = intent.getIntExtra("status",
				BatteryManager.BATTERY_STATUS_UNKNOWN);
		String statusString;
		if (status == BatteryManager.BATTERY_STATUS_CHARGING) {
			statusString = res.getString(R.string.battery_info_status_charging);
			if (plugType > 0) {
				statusString = statusString
						+ " "
						+ res.getString((plugType == BatteryManager.BATTERY_PLUGGED_AC) ? R.string.battery_info_status_charging_ac
								: R.string.battery_info_status_charging_usb);
			}
		} else if (status == BatteryManager.BATTERY_STATUS_DISCHARGING) {
			statusString = res
					.getString(R.string.battery_info_status_discharging);
		} else if (status == BatteryManager.BATTERY_STATUS_NOT_CHARGING) {
			statusString = res
					.getString(R.string.battery_info_status_not_charging);
		} else if (status == BatteryManager.BATTERY_STATUS_FULL) {
			statusString = res.getString(R.string.battery_info_status_full);
		} else {
			statusString = res.getString(R.string.battery_info_status_unknown);
		}

		return statusString;
	}

	@SuppressWarnings("unused")
	private void processAppUsage() {
		if (DEBUG)
			Log.v("PowerAnalyserService", "processAppUsage");
		SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		final int which = mStatsType;
		final int speedSteps = mPowerProfile.getNumSpeedSteps();
		final double[] powerCpuNormal = new double[speedSteps];
		final long[] cpuSpeedStepTimes = new long[speedSteps];
		// double appTotalPower = 0;
		for (int p = 0; p < speedSteps; p++) {
			powerCpuNormal[p] = mPowerProfile.getAveragePower(
					PowerProfile.POWER_CPU_ACTIVE, p);
		}
		final double averageCostPerByte = getAverageDataCost();
		long uSecTime = mStats.computeBatteryRealtime(
				SystemClock.elapsedRealtime() * 1000, which);
		long appWakelockTime = 0;
		// BatterySipper osApp = null;
		SparseArray<? extends Uid> uidStats = mStats.getUidStats();
		final int NU = uidStats.size();
		if (DEBUG)
			Log.v("UId size", String.valueOf(NU));
		for (int iu = 0; iu < NU; iu++) {
			Uid u = uidStats.valueAt(iu);
			double power = 0;
			double highestDrain = 0;
			String packageWithHighestDrain = null;

			Map<String, ? extends BatteryStats.Uid.Proc> processStats = u
					.getProcessStats();
			long cpuTime = 0;
			long cpuFgTime = 0;
			long wakelockTime = 0;
			long gpsTime = 0;

			if (processStats.size() > 0) {
				// Process CPU time
				for (Map.Entry<String, ? extends BatteryStats.Uid.Proc> ent : processStats
						.entrySet()) {
					if (DEBUG)
						Log.i(TAG, "Process name = " + ent.getKey());
					Uid.Proc ps = ent.getValue();
					final long userTime = ps.getUserTime(which); // 1/100 sec
					final long systemTime = ps.getSystemTime(which); // 1/100
																		// sec
					final long foregroundTime = ps.getForegroundTime(which); // microseconds
					cpuFgTime += foregroundTime * 10; // convert to millis
					final long tmpCpuTime = (userTime + systemTime) * 10; // convert
																			// to
																			// millis
					int totalTimeAtSpeeds = 0;
					// Get the total first
					for (int step = 0; step < speedSteps; step++) {
						cpuSpeedStepTimes[step] = ps.getTimeAtCpuSpeedStep(
								step, which);
						totalTimeAtSpeeds += cpuSpeedStepTimes[step];
					}
					if (totalTimeAtSpeeds == 0)
						totalTimeAtSpeeds = 1;
					// Then compute the ratio of time spent at each speed
					double processPower = 0;
					for (int step = 0; step < speedSteps; step++) {
						double ratio = (double) cpuSpeedStepTimes[step]
								/ totalTimeAtSpeeds;
						processPower += ratio * tmpCpuTime
								* powerCpuNormal[step];
					}
					cpuTime += tmpCpuTime;
					power += processPower;
					cpuPower += processPower;
					if (packageWithHighestDrain == null
							|| packageWithHighestDrain.startsWith("*")) {
						highestDrain = processPower;
						packageWithHighestDrain = ent.getKey();
					} else if (highestDrain < processPower
							&& !ent.getKey().startsWith("*")) {
						highestDrain = processPower;
						packageWithHighestDrain = ent.getKey();
					}
				}
				if (DEBUG)
					Log.i(TAG, "Max drain of " + highestDrain + " by "
							+ packageWithHighestDrain);
			}
			if (cpuFgTime > cpuTime) {
				if (DEBUG && cpuFgTime > cpuTime + 10000) {
					Log.i(TAG,
							"WARNING! Cputime is more than 10 seconds behind Foreground time");
				}
				cpuTime = cpuFgTime; // Statistics may not have been gathered
										// yet.
			}
			power /= 1000;
			cpuPower /= 1000;

			// Process wake lock usage
			Map<String, ? extends BatteryStats.Uid.Wakelock> wakelockStats = u
					.getWakelockStats();
			for (Map.Entry<String, ? extends BatteryStats.Uid.Wakelock> wakelockEntry : wakelockStats
					.entrySet()) {
				Uid.Wakelock wakelock = wakelockEntry.getValue();
				// Only care about partial wake locks since full wake locks
				// are canceled when the user turns the screen off.
				BatteryStats.Timer timer = wakelock
						.getWakeTime(BatteryStats.WAKE_TYPE_PARTIAL);
				if (timer != null) {
					wakelockTime += timer.getTotalTimeLocked(uSecTime, which);// microseconds
				}
			}
			wakelockTime /= 1000; // convert to millis
			appWakelockTime += wakelockTime;

			// Add cost of holding a wake lock
			power += (wakelockTime * mPowerProfile
					.getAveragePower(PowerProfile.POWER_CPU_AWAKE)) / 1000;
			cpuPower += (wakelockTime * mPowerProfile
					.getAveragePower(PowerProfile.POWER_CPU_AWAKE)) / 1000;
			// Add cost of data traffic
			long tcpBytesReceived = u.getTcpBytesReceived(mStatsType);
			long tcpBytesSent = u.getTcpBytesSent(mStatsType);
			power += (tcpBytesReceived + tcpBytesSent) * averageCostPerByte;

			// Add cost of keeping WIFI running.
			long wifiRunningTimeMs = u.getWifiRunningTime(uSecTime, which) / 1000;

			power += (wifiRunningTimeMs * mPowerProfile
					.getAveragePower(PowerProfile.POWER_WIFI_ON)) / 1000;
			wifiPower += (wifiRunningTimeMs * mPowerProfile
					.getAveragePower(PowerProfile.POWER_WIFI_ON)) / 1000;

			// Process Sensor usage
			Map<Integer, ? extends BatteryStats.Uid.Sensor> sensorStats = u
					.getSensorStats();
			for (Map.Entry<Integer, ? extends BatteryStats.Uid.Sensor> sensorEntry : sensorStats
					.entrySet()) {
				Uid.Sensor sensor = sensorEntry.getValue();
				int sensorType = sensor.getHandle();
				BatteryStats.Timer timer = sensor.getSensorTime();
				long sensorTime = timer.getTotalTimeLocked(uSecTime, which) / 1000;
				double multiplier = 0;
				switch (sensorType) {
				case Uid.Sensor.GPS:
					multiplier = mPowerProfile
							.getAveragePower(PowerProfile.POWER_GPS_ON);
					gpsTime = sensorTime;
					break;
				default:
					android.hardware.Sensor sensorData = sensorManager
							.getDefaultSensor(sensorType);
					if (sensorData != null) {
						multiplier = sensorData.getPower();
						if (DEBUG) {
							Log.i(TAG, "Got sensor " + sensorData.getName()
									+ " with power = " + multiplier);
						}
					}
				}
				power += (multiplier * sensorTime) / 1000;
				sensorPower += (multiplier * sensorTime) / 1000;
			}

			if (DEBUG)
				Log.i(TAG, "UID " + u.getUid() + ": power=" + power);

			// Add the app to the list if it is consuming power
			if (power != 0 || u.getUid() == 0) {
				/*
				 * app.cpuTime = cpuTime; app.gpsTime = gpsTime;
				 * app.wifiRunningTime = wifiRunningTimeMs; app.cpuFgTime =
				 * cpuFgTime; app.wakeLockTime = wakelockTime;
				 * app.tcpBytesReceived = tcpBytesReceived; app.tcpBytesSent =
				 * tcpBytesSent;
				 */

			}
			if (u.getUid() == 0) {
				// The device has probably been awake for longer than the screen
				// on
				// time and application wake lock time would account for. Assign
				// this remainder to the OS, if possible.
				packageWithHighestDrain = "Android OS";
				long wakeTimeMillis = mStats.computeBatteryUptime(
						SystemClock.uptimeMillis() * 1000, which) / 1000;
				wakeTimeMillis -= appWakelockTime
						+ (mStats.getScreenOnTime(
								SystemClock.elapsedRealtime(), which) / 1000);
				if (wakeTimeMillis > 0) {
					double wlpower = (wakeTimeMillis * mPowerProfile
							.getAveragePower(PowerProfile.POWER_CPU_AWAKE)) / 1000;
					if (DEBUG)
						Log.i(TAG, "OS wakeLockTime " + wakeTimeMillis
								+ " power " + power);
					// osApp.wakeLockTime += wakeTimeMillis;
					// osApp.value += wlpower;
					// osApp.values[0] += wlpower;
					power += wlpower;
					// if (osApp.value > mMaxPower) mMaxPower = osApp.value;
				}
			} else if (u.getUid() == 1010/* Process.WIFI_UID */) {
				wifiPower += power;
				mTotalPower -= power;
			} else if (u.getUid() == Process.BLUETOOTH_GID) {
				bluetoothPower += power;
				mTotalPower -= power;
			}
			if (power > mMaxPower)
				mMaxPower = power;
			mTotalPower += power;

			PowerData pd = new PowerData();
			pd.setName(packageWithHighestDrain);
			pd.setMark(String.valueOf(u.getUid()));
			pd.setPower(String.valueOf(power));
			pd.setTime(curTime);
			applist.add(pd);
			if (DEBUG)
				Log.i(TAG, "Added power = " + power);

		}

		PowerData SensorPower = new PowerData("Sensor", "Total",
				PowerType.Sensor.toString(), String.valueOf(sensorPower),
				"by App", curTime);
		component.add(SensorPower);

	}

	private double getAverageDataCost() {
		final long WIFI_BPS = 1000000; // TODO: Extract average bit rates from
										// system
		final long MOBILE_BPS = 200000; // TODO: Extract average bit rates from
										// system
		final double WIFI_POWER = mPowerProfile
				.getAveragePower(PowerProfile.POWER_WIFI_ACTIVE) / 3600;
		final double MOBILE_POWER = mPowerProfile
				.getAveragePower(PowerProfile.POWER_RADIO_ACTIVE) / 3600;
		final long mobileData = mStats.getMobileTcpBytesReceived(mStatsType)
				+ mStats.getMobileTcpBytesSent(mStatsType);
		final long wifiData = mStats.getTotalTcpBytesReceived(mStatsType)
				+ mStats.getTotalTcpBytesSent(mStatsType) - mobileData;
		final long radioDataUptimeMs = mStats.getRadioDataUptime() / 1000;
		final long mobileBps = radioDataUptimeMs != 0 ? mobileData * 8 * 1000
				/ radioDataUptimeMs : MOBILE_BPS;

		double mobileCostPerByte = MOBILE_POWER / (mobileBps / 8);
		double wifiCostPerByte = WIFI_POWER / (WIFI_BPS / 8);
		if (wifiData + mobileData != 0) {
			wifiPower += wifiCostPerByte * wifiData;
			mobileTransPower += mobileCostPerByte * mobileData;
			return (mobileCostPerByte * mobileData + wifiCostPerByte * wifiData)
					/ (mobileData + wifiData);
		} else {
			return 0;
		}
	}

	private void processMiscUsage() {
		if (DEBUG)
			Log.v("PowerSummaryService", "processMiscUsage");
		final int which = mStatsType;
		long uSecTime = SystemClock.elapsedRealtime() * 1000;
		final long uSecNow = mStats.computeBatteryRealtime(uSecTime, which);
		final long timeSinceUnplugged = uSecNow;
		if (DEBUG) {
			Log.i(TAG, "Uptime since last unplugged = "
					+ (timeSinceUnplugged / 1000));
		}

		addPhoneUsage(uSecNow);
		addScreenUsage(uSecNow);
		addWiFiUsage(uSecNow);
		addBluetoothUsage(uSecNow);
		addIdleUsage(uSecNow); // Not including cellular idle power
		addRadioUsage(uSecNow);

		PowerData PhonePower = new PowerData("Phone", "Phone",
				PowerType.Phone.toString(), String.valueOf(phonePower
						+ radioPower + mobileTransPower),
				"Phone+Radio+MobileTrans", curTime);
		component.add(PhonePower);
	}

	private void addPhoneUsage(long uSecNow) {
		long phoneOnTimeMs = mStats.getPhoneOnTime(uSecNow, mStatsType) / 1000;
		phonePower = mPowerProfile
				.getAveragePower(PowerProfile.POWER_RADIO_ACTIVE)
				* phoneOnTimeMs / 1000;
		if (DEBUG) {
			Log.i(TAG, "Phone power = " + (int) phonePower + ", time = "
					+ phoneOnTimeMs);
		}
		mTotalPower += phonePower;
	}

	private void addScreenUsage(long uSecNow) {
		long screenOnTimeMs = mStats.getScreenOnTime(uSecNow, mStatsType) / 1000;
		screenPower += screenOnTimeMs
				* mPowerProfile.getAveragePower(PowerProfile.POWER_SCREEN_ON);
		final double screenFullPower = mPowerProfile
				.getAveragePower(PowerProfile.POWER_SCREEN_FULL);
		for (int i = 0; i < BatteryStats.NUM_SCREEN_BRIGHTNESS_BINS; i++) {
			double screenBinPower = screenFullPower * (i + 0.5f)
					/ BatteryStats.NUM_SCREEN_BRIGHTNESS_BINS;
			long brightnessTime = mStats.getScreenBrightnessTime(i, uSecNow,
					mStatsType) / 1000;
			screenPower += screenBinPower * brightnessTime;
			if (DEBUG) {
				Log.i(TAG, "Screen bin power = " + (int) screenBinPower
						+ ", time = " + brightnessTime);
			}
		}
		screenPower /= 1000; // To seconds
		mTotalPower += screenPower;
		PowerData ScreenPower = new PowerData("Screen", "Total",
				PowerType.Screen.toString(), String.valueOf(screenPower), "",
				curTime);
		component.add(ScreenPower);
	}

	private void addWiFiUsage(long uSecNow) {
		long onTimeMs = mStats.getWifiOnTime(uSecNow, mStatsType) / 1000;
		long runningTimeMs = mStats.getGlobalWifiRunningTime(uSecNow,
				mStatsType) / 1000;
		if (DEBUG)
			Log.i(TAG, "WIFI runningTime=" + runningTimeMs);

		if (runningTimeMs < 0)
			runningTimeMs = 0;
		wifiPower += (onTimeMs * 0 /* TODO */
				* mPowerProfile.getAveragePower(PowerProfile.POWER_WIFI_ON) + runningTimeMs
				* mPowerProfile.getAveragePower(PowerProfile.POWER_WIFI_ON)) / 1000;
		if (DEBUG)
			Log.i(TAG, "WIFI power=" + wifiPower);
		mTotalPower += wifiPower;
		PowerData WIFIPower = new PowerData("WIFI", "Total",
				PowerType.WIFI.toString(), String.valueOf(wifiPower),
				"app+hw+tran+on", curTime);
		component.add(WIFIPower);
	}

	private void addBluetoothUsage(long uSecNow) {
		long btOnTimeMs = mStats.getBluetoothOnTime(uSecNow, mStatsType) / 1000;
		bluetoothPower += btOnTimeMs
				* mPowerProfile
						.getAveragePower(PowerProfile.POWER_BLUETOOTH_ON)
				/ 1000;
		int btPingCount = mStats.getBluetoothPingCount();
		bluetoothPower += (btPingCount * mPowerProfile
				.getAveragePower(PowerProfile.POWER_BLUETOOTH_AT_CMD)) / 1000;

		if (DEBUG)
			Log.i(TAG, "Bluetooth power=" + bluetoothPower);
		mTotalPower += bluetoothPower;
		PowerData BTPower = new PowerData("Bluetooth", "Total",
				PowerType.Bluetooth.toString(), String.valueOf(bluetoothPower),
				"", curTime);
		component.add(BTPower);

	}

	private void addIdleUsage(long uSecNow) {
		long idleTimeMs = (uSecNow - mStats
				.getScreenOnTime(uSecNow, mStatsType)) / 1000;
		idlePower = (idleTimeMs * mPowerProfile
				.getAveragePower(PowerProfile.POWER_CPU_IDLE)) / 1000;

		if (DEBUG)
			Log.i(TAG, "Idle power=" + idlePower + " time=" + idleTimeMs);
		cpuPower += idlePower;
		mTotalPower += idlePower;
		PowerData CPUPower = new PowerData("CPU", "Total",
				PowerType.CPU.toString(), String.valueOf(cpuPower),
				"app+idle+wakelock", curTime);
		component.add(CPUPower);

	}

	private void addRadioUsage(long uSecNow) {
		final int BINS = 5;// SignalStrength.NUM_SIGNAL_STRENGTH_BINS
		long signalTimeMs = 0;
		for (int i = 0; i < BINS; i++) {
			long strengthTimeMs = mStats.getPhoneSignalStrengthTime(i, uSecNow,
					mStatsType) / 1000;
			radioPower += strengthTimeMs
					/ 1000
					* mPowerProfile.getAveragePower(
							PowerProfile.POWER_RADIO_ON, i);
			signalTimeMs += strengthTimeMs;
		}
		long scanningTimeMs = mStats.getPhoneSignalScanningTime(uSecNow,
				mStatsType) / 1000;
		radioPower += scanningTimeMs
				/ 1000
				* mPowerProfile
						.getAveragePower(PowerProfile.POWER_RADIO_SCANNING);
		mTotalPower += radioPower;
		/*
		 * if (signalTimeMs != 0) { bs.noCoveragePercent =
		 * mStats.getPhoneSignalStrengthTime(0, uSecNow, mStatsType) / 1000 *
		 * 100.0 / signalTimeMs; }
		 */
		
		if (DEBUG) {
			Log.i(TAG, "Radio power = " + (int) radioPower + ", time = "
					+ signalTimeMs);
		}

	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}



}
