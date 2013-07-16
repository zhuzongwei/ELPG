package edu.ustc.PowerAnalyser.log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import edu.ustc.PowerAnalyser.R;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class LogAdapter extends BaseAdapter {

	private static LayoutInflater inflater = null;
	private List<String> logcat = null;
	private static Context mcontext;
	private static final String APP_SD_CACHE = "PowerAnalyserCache";
	private static String LOG_PREF = "LogHunterService";
	private static String SHOW_TAG = "showThis";
	
	//private static final String DALVIKVM = "dalvikvm";
	
	public LogAdapter(Context context)
	{
		mcontext = context;
		inflater = LayoutInflater.from(context);
		logcat = new ArrayList<String>();
	}
	class LogViewHolder
	{
		TextView logtext;
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return logcat.size();
	}

	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		if(logcat.size()!= 0)
			return logcat.get(arg0);
		else
			return null;
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return arg0;
	}

	@Override
	public View getView(int arg0, View arg1, ViewGroup arg2) {
		// TODO Auto-generated method stub
		LogViewHolder holder;
		if(arg1 == null)
		{
			arg1 =  inflater.inflate(R.layout.log_item, null);
			holder = new LogViewHolder();
			holder.logtext = (TextView)arg1.findViewById(R.id.log_item);
			arg1.setTag(holder);
		}
		else
		{
			holder = (LogViewHolder) arg1.getTag();			
		}
		if(logcat.size()!=0)
			holder.logtext.setText(logcat.get(arg0));
				
		return arg1;
	}
	@SuppressLint("SimpleDateFormat")
	public void createLogList(Date date,int offset)
	{
		
		Date dateoff =new Date(date.getTime() - 1000*offset); 
		Date datebefore =new Date(date.getTime() - 60*1000); 
		Date dateafter =new Date(date.getTime() + 60*1000); 
		//Date dateoffHour = new Date(date.getTime() - 60*60*1000); 
		SimpleDateFormat minFotmatter = new SimpleDateFormat("MM_dd_HH_mm");
		SimpleDateFormat timeFormatter = new SimpleDateFormat("MM-dd HH:mm:ss");
    	String curTime = timeFormatter.format(dateoff);
    	String minTime = minFotmatter.format(date);
    	createLoglistFromFile(minTime,curTime);
    	if(logcat.size()==0)
    	{
    		minTime = minFotmatter.format(dateafter);
    		createLoglistFromFile(minTime,curTime);
    	}
    	if(logcat.size()==0)
    	{
    		minTime = minFotmatter.format(datebefore);
    		createLoglistFromFile(minTime,curTime);
    	}
		
	}
	private void createLoglistFromFile(String minTime,String curTime)
	{
		boolean sdCardExist = Environment.getExternalStorageState().equals(  
                android.os.Environment.MEDIA_MOUNTED);
		File file = null;
		String fpath = null;
		FileReader m_Fr = null;
		BufferedReader m_Readbuf = null;
		String line = null;
		if(sdCardExist)
		{
			String sddir = Environment.getExternalStorageDirectory().toString();
			fpath = sddir + "/" + APP_SD_CACHE  + "/logHunt_"+ minTime +".txt";
			file = new File(fpath);
		}
		else
		{
			File fdir = mcontext.getFilesDir();
			fpath = fdir.getPath()+ "/logHunt_"+ minTime +".txt";
			file = new File(fpath);			
		}
		if(!file.exists())
		{
			Toast.makeText(mcontext,
					 "No Log File.",
					 Toast.LENGTH_SHORT).show();
		}
		else
		{
			try {
				SharedPreferences sp = mcontext.getSharedPreferences(LOG_PREF, 0);
				boolean showThis = sp.getBoolean(SHOW_TAG, false);
				m_Fr = new FileReader(fpath);
				m_Readbuf = new BufferedReader(m_Fr);
				boolean start = false;
				while((line = m_Readbuf.readLine()) != null)
				{
					if(line.contains(curTime))
						start = true;
					if(!showThis)
					{
						if(line.contains("PowerAnalyser") || line.contains(".remotePowerService"))
							continue;
					}
					if(start)
						logcat.add(line);
				}
				m_Readbuf.close();
				m_Fr.close();

			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			
		}
	}
	public void removelogList()
	{
		logcat.clear();
	}

}
