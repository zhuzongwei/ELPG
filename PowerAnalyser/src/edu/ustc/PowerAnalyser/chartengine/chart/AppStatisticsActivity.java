package edu.ustc.PowerAnalyser.chartengine.chart;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.SeriesSelection;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.renderer.XYMultipleSeriesRenderer;

import edu.ustc.PowerAnalyser.R;
import edu.ustc.PowerAnalyser.chartengine.chart.AbstractData;
import edu.ustc.PowerAnalyser.log.LogActivity;
import edu.ustc.PowerAnalyser.tools.IPowerDataParser;
import edu.ustc.PowerAnalyser.tools.PULLPowerDataParser;
import edu.ustc.PowerAnalyser.tools.PowerLists;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Bitmap.CompressFormat;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

public class AppStatisticsActivity extends Activity {
	private GraphicalView mChartView;

	private XYMultipleSeriesDataset mDataset = new XYMultipleSeriesDataset();
	private XYMultipleSeriesRenderer mRenderer = new XYMultipleSeriesRenderer();

	private ImageButton mSave;
	private List<Date[]> dates ;
	private List<double[]> values ;
	private String begin = null;
	private String end = null;
	private int UID = 0;
	private static final boolean DEBUG = false;
	AbstractData AppStatisticsData = new AbstractData();
	
	AbstractChart AppStatistics = new AbstractChart();
	
	public static final String tag = "Model and Driver";
	private ProgressDialog progressDialog = null;

	private static final int UPDATE = 1111;
	private static final int NODATA = 1010;
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			switch (msg.what) {
			case UPDATE:// 在收到消息时，对界面进行更新
				viewRefresh();
				if(DEBUG) Log.d(tag, "view refresh");
				break;
			case NODATA:
				Toast.makeText(AppStatisticsActivity.this, "无数据", Toast.LENGTH_SHORT).show();
				
				AppStatisticsActivity.this.finish();
			}
		}
	};
	private void viewRefresh()
	{
		progressDialog.dismiss();
		if (mChartView == null) {
			LinearLayout layout = (LinearLayout) findViewById(R.id.chart);
			// mChartView = ChartFactory.getCubeLineChartView(this, mDataset,
			// mRenderer, 0.5f);
			mChartView = ChartFactory.getTimeChartView(this, mDataset,
					mRenderer, "h");

			mRenderer.setShowGrid(true); // 显示网格
			mRenderer.setClickEnabled(true);// 设置图表是否允许点击
			mRenderer.setSelectableBuffer(10);// 设置点的缓冲半径值(在某点附件点击时,多大范围内都算点击这个点)
			
			
			mChartView.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					// 这段代码处理点击一个点后,获得所点击的点在哪个序列中以及点的坐标.
					Date clickItemDate;
					SeriesSelection seriesSelection = mChartView
							.getCurrentSeriesAndPoint();
					double[] xy = mChartView.toRealPoint(0);
					if (seriesSelection != null) {
						clickItemDate = new Date((long) xy[0]);
						Toast.makeText(AppStatisticsActivity.this,
								"\nTime is: " + clickItemDate.toLocaleString(),
								Toast.LENGTH_SHORT).show();
					}

				}
			});

			layout.addView(mChartView, new LayoutParams(
					LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));

		}
		else
		{
			mChartView.repaint();
		}		
	}

	private void updateByMessage() {
		// 匿名对象
		new Thread() {
			public void run() {
				Bundle data = AppStatisticsActivity.this.getIntent().getExtras();
				
				if(data != null){
					UID = Integer.parseInt(data.getString("uid"));
					if(DEBUG) Log.v("uid", "the App's uid is "+ UID);
				}else{
					if(DEBUG) Log.v("data", "data is null!!");
				}
				String[] titles = new String[] { "Application's Usage" };			
				PULLPowerDataParser parser = new PULLPowerDataParser(AppStatisticsActivity.this);
				PowerLists lists = parser.readList(IPowerDataParser.APP_UID_LIST, data.getString("uid"));
				if (lists==null||lists.getPower()==null||lists.getPower().get(0).length == 0) {
					if(DEBUG) Log.v("APPStatistics", "无数据");
					Message msg = mHandler.obtainMessage(NODATA);
					mHandler.sendMessage(msg);
				    return; 
				} else {
					if(DEBUG) Log.v("AppStatistics", "check pass");
					dates = lists.getTime();
					values = lists.getPower();

				}
				
				
				double max = 0;
				for(double i:values.get(0))
				{
					if( i > max )
						max = i;
				}
				
				
				begin = String.valueOf(dates.get(0)[0].getTime());
				end = String.valueOf(dates.get(0)[dates.get(0).length - 1].getTime());
			    
			    AppStatisticsData.setTitles(titles);
			    AppStatisticsData.setDates(dates);
				AppStatisticsData.setValues(values);
				
			    int[] colors = new int[] { Color.GREEN };
			    PointStyle[] styles = new PointStyle[] { PointStyle.POINT };
			    //SharedPreferences sharedPreferences = getSharedPreferences("BatteryCapacity", Context.MODE_PRIVATE);
			    //int batteryMax = sharedPreferences.getInt("BatteryCapacity", 0);
			    
			    //渲染
			    mRenderer = AppStatistics.buildRenderer(colors, styles);
			    AppStatistics.setChartSettings(mRenderer, "Battery Statistics", "Time", "mAs", dates.get(0)[0].getTime(),
			        dates.get(0)[dates.get(0).length - 1].getTime(), 0, max, Color.GRAY, Color.LTGRAY);
			    mRenderer.setYLabels(10);
			    mRenderer.setZoomButtonsVisible(true);
			    
			  //生成数据集
			    mDataset = AppStatistics.buildDateDataset(AppStatisticsData.getTitles(), AppStatisticsData.getDates(), AppStatisticsData.getValues());

			 // UPDATE是一个自己定义的整数，代表了消息ID
				Message msg = mHandler.obtainMessage(UPDATE);
				mHandler.sendMessage(msg);
				if(DEBUG) Log.d(tag, "send message");
				
				
				
			}}.start();}
	
	
	@Override
	protected void onRestoreInstanceState(Bundle savedState) {
		super.onRestoreInstanceState(savedState);

		mDataset = (XYMultipleSeriesDataset) savedState
				.getSerializable("dataset");
		mRenderer = (XYMultipleSeriesRenderer) savedState
				.getSerializable("renderer");

	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putSerializable("dataset", mDataset);
		outState.putSerializable("renderer", mRenderer);

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.xy_chart);
		
		
		progressDialog = ProgressDialog.show(this, "请稍等...", "加载中...",true);
		updateByMessage();
		
		
		//保存图片
		mSave = (ImageButton) findViewById(R.id.save);

		mSave.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				Bitmap bitmap = mChartView.toBitmap();
				try {
					File file = new File(Environment
							.getExternalStorageDirectory(),
							"AppStatisticsChart" + UID + "_" + begin +"_"+ end + ".jpg");
					FileOutputStream output = new FileOutputStream(file);
					bitmap.compress(CompressFormat.JPEG, 100, output);
					Toast.makeText(AppStatisticsActivity.this, "保存成功",
							Toast.LENGTH_SHORT).show();

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

		mSave.setEnabled(true);
	}

	//从其他Activity返回时重画
	@Override
	protected void onResume() {
		super.onResume();
		
	}

}
