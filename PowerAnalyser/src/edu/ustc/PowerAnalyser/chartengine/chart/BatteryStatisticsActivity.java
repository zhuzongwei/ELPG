/**
 * Copyright (C) 2009, 2010 SC 4ViewSoft SRL
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
import org.achartengine.model.TimeSeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import edu.ustc.PowerAnalyser.R;
import edu.ustc.PowerAnalyser.chartengine.chart.AbstractData;
import edu.ustc.PowerAnalyser.log.LogActivity;
import edu.ustc.PowerAnalyser.tools.IPowerDataParser;
import edu.ustc.PowerAnalyser.tools.PULLPowerDataParser;
import edu.ustc.PowerAnalyser.tools.PowerLists;
import edu.ustc.PowerAnalyser.ui.Settings;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Paint.Align;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.format.Time;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

/**
 * Battery Statistics demo chart.
 */
public class BatteryStatisticsActivity extends Activity {
	private GraphicalView mChartView;

	private XYMultipleSeriesDataset mDataset = new XYMultipleSeriesDataset();
	private XYMultipleSeriesRenderer mRenderer = new XYMultipleSeriesRenderer();

	private ImageButton mSave;
	private String begin = null;
	private String end = null;

	private List<Date[]> dates;
	private List<double[]> values;

	AbstractData BatteryStatisticsData = new AbstractData();

	AbstractChart BatteryStatistics = new AbstractChart();
	
	public static final String tag = "BatteryStatisticsActivity";
	private static final boolean DEBUG = false;
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
				Toast.makeText(BatteryStatisticsActivity.this, "无数据", Toast.LENGTH_SHORT).show();
				
				BatteryStatisticsActivity.this.finish();
				
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
						Toast.makeText(BatteryStatisticsActivity.this,
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
				
				String[] titles = new String[] { "Battery Statistcis from last charged" };
				PULLPowerDataParser parser = new PULLPowerDataParser(BatteryStatisticsActivity.this);
				PowerLists lists = parser.readList(IPowerDataParser.BATTERY_LIST, 0);


				if (lists==null || lists.getPower().isEmpty() || lists.getPower().get(0).length==0) {
					if(DEBUG) Log.v("BatteryStatistics", "无数据");
					
					
					//BatteryStatisticsActivity.this.finish();
					Message msg = mHandler.obtainMessage(NODATA);
					mHandler.sendMessage(msg);
				    return; 
				} else {
					if(DEBUG) Log.v("BatteryStatistics", "check pass");
					dates = lists.getTime();
					values = lists.getPower();
					begin = String.valueOf(dates.get(0)[0].getTime());
					end = String.valueOf(dates.get(0)[dates.get(0).length - 1]
							.getTime());

				}

				// dates.add(dateValues);

				// values.add(new double[] { 100, 99, 95, 92, 90, 80, 75, 68, 60, 52,
				// 40, 35, 32, 30,
				// 20, 18, 10, 9, 8, 7, 6, 5, 4, 3, 2,1,0.5,0.2,0.1,0 });

				BatteryStatisticsData.setTitles(titles);
				BatteryStatisticsData.setDates(dates);
				BatteryStatisticsData.setValues(values);

				int[] colors = new int[] { Color.GREEN };
				PointStyle[] styles = new PointStyle[] { PointStyle.POINT };

				// 渲染
				mRenderer = BatteryStatistics.buildRenderer(colors, styles);
				BatteryStatistics.setChartSettings(mRenderer, "Battery Statistics",
						"Date", "%", dates.get(0)[0].getTime(),
						dates.get(0)[dates.get(0).length - 1].getTime(), 0, 100,
						Color.GRAY, Color.LTGRAY);
				mRenderer.setYLabels(10);
				mRenderer.setZoomButtonsVisible(true);

				// 生成数据集
				mDataset = BatteryStatistics.buildDateDataset(
						BatteryStatisticsData.getTitles(),
						BatteryStatisticsData.getDates(),
						BatteryStatisticsData.getValues());

				
				// UPDATE是一个自己定义的整数，代表了消息ID
				Message msg = mHandler.obtainMessage(UPDATE);
				mHandler.sendMessage(msg);
				Log.d(tag, "send message");
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
		
		

		// 保存图片
		mSave = (ImageButton) findViewById(R.id.save);

		mSave.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				Bitmap bitmap = mChartView.toBitmap();
				try {
					File file = new File(Environment
							.getExternalStorageDirectory(),
							"BatteryStatisticsChart" + begin + "_" + end
									+ ".jpg");
					FileOutputStream output = new FileOutputStream(file);
					bitmap.compress(CompressFormat.JPEG, 100, output);
					Toast.makeText(BatteryStatisticsActivity.this, "保存成功",
							Toast.LENGTH_SHORT).show();

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

		mSave.setEnabled(true);

	}

	// 从其他Activity返回时重画
	@Override
	protected void onResume() {
		super.onResume();
		
	}

	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, Menu.FIRST, 0, "设置");
		menu.add(0, Menu.FIRST + 1, 0, "退出");
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case 1:
			Intent mIntent = new Intent(this, Settings.class);
			startActivity(mIntent);
			break;
		case 2:
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	 

}
