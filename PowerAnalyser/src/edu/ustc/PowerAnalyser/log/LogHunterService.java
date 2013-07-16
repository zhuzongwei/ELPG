package edu.ustc.PowerAnalyser.log;

import java.io.DataOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

@SuppressLint("SimpleDateFormat")
public class LogHunterService extends Service {

	private static final boolean DEBUG = false;
	private static String TAG = "LogHunterService";
	private static final String APP_SD_CACHE = "PowerAnalyserCache";
	//private String FILE_PATH = null;
	Process logrecord = null;
	DataOutputStream localDataOutputStream = null;

	
	public void startHunt() throws IOException, InterruptedException
	{
		if(DEBUG) Log.v(TAG,"startHunt");
		boolean sdCardExist = Environment.getExternalStorageState().equals(  
                android.os.Environment.MEDIA_MOUNTED);
		String logcmd[] = new String[7];
		logcmd[0] = "logcat";
		logcmd[1] = "-v";
		logcmd[2] = "time";			
		logcmd[3] = "-f";
		StringBuffer str = new StringBuffer();
		if(sdCardExist)
		{
			str.append(Environment.getExternalStorageDirectory().toString());
			str.append("/" + APP_SD_CACHE);
		}
		else
			str.append(this.getFilesDir().getPath());
		 
		str.append("/"+ "AM_log.txt");
		logcmd[4] = str.toString();		
		logcmd[5] = "ActivityManager:I";
		logcmd[6] = "*:S";	 		
		ExecProgram(logcmd);

	}
	
	 public void ExecProgram(String[] paramArrayOfString)
	    throws IOException, InterruptedException
	  {
		localDataOutputStream = new DataOutputStream(logrecord.getOutputStream());
	    String str1 = "";
	    int i = paramArrayOfString.length;
	    for (int j = 0; j < i; j++)
	    {
	      String str2 = paramArrayOfString[j];
	      str1 = str1 + str2 + " ";
	    }
	    if(DEBUG) Log.d(TAG,"ExecProgram");
	    if(DEBUG) Log.d(TAG, str1);
	    localDataOutputStream.writeBytes(str1 + "\n");
	    localDataOutputStream.flush();
	    logrecord.waitFor();
	    
	  }

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		try {
			if(localDataOutputStream!= null)
				localDataOutputStream.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    if(logrecord!=null)
	    	logrecord.destroy();

	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		if(logrecord == null)
			try {
				logrecord = Runtime.getRuntime().exec("/system/bin/sh");				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
	}


	@Override
	public void onStart(Intent intent, int startId) {
		// TODO Auto-generated method stub
		super.onStart(intent, startId);
		try {
			startHunt();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
}
