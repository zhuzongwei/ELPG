package edu.ustc.PowerAnalyser.log;

import java.util.Date;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.ListView;
import edu.ustc.PowerAnalyser.R;

public class LogActivity  extends Activity {
	private LogDialog dialog;
	private LinearLayout layout;
	private ListView loglist;
	private LogAdapter logadapter;
	private Date date ;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		Bundle extras = getIntent().getExtras();
		date = (Date)extras.get("Date");
		setContentView(R.layout.dailog);
		dialog = new LogDialog(this);
		layout=(LinearLayout)findViewById(R.id.layout);
		loglist = (ListView)findViewById(R.id.loglist);
		logadapter = new LogAdapter(this);
		layout.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
			}
		});
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();		
		
		logadapter.createLogList(date, 1);
		loglist.setAdapter(logadapter);
		
	}

	@Override
	public boolean onTouchEvent(MotionEvent event){
		finish();
		return true;
	}
}
