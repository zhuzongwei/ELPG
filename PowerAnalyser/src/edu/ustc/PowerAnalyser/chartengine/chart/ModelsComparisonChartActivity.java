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
import org.achartengine.model.CategorySeries;
import org.achartengine.model.SeriesSelection;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import edu.ustc.PowerAnalyser.R;
import edu.ustc.PowerAnalyser.chartengine.chart.AbstractData;
import edu.ustc.PowerAnalyser.tools.IPowerDataParser;
import edu.ustc.PowerAnalyser.tools.PULLPowerDataParser;
import edu.ustc.PowerAnalyser.tools.PowerLists;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Paint.Align;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
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
 * Models comparison demo chart.
 */
public class ModelsComparisonChartActivity extends Activity {

	public static final boolean DEBUG = false;

	
	private GraphicalView mChartView;

	private XYMultipleSeriesDataset mDataset = new XYMultipleSeriesDataset();
	private XYMultipleSeriesRenderer mRenderer = new XYMultipleSeriesRenderer();

	private ImageButton mSave;
	private String begin = null;
	private String end = null;

	private List<Date[]> dates;
	private List<double[]> values;
	private List<Date[]> datesM;
	private List<double[]> valueM;

	AbstractChart ModelsComparisonChart = new AbstractChart();
	AbstractData ModelsComparsionData = new AbstractData();
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
				Toast.makeText(ModelsComparisonChartActivity.this, "无数据", Toast.LENGTH_SHORT).show();
				ModelsComparisonChartActivity.this.finish();
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
						Toast.makeText(ModelsComparisonChartActivity.this,
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
				//
				String[] titles = new String[] { "Statistics from Drive",
						"Statistics from Model",
						"Difference between Drive and Model" };
				SharedPreferences sharedPreferences = getSharedPreferences(
						"Battery", Context.MODE_PRIVATE);
				int batteryMax = sharedPreferences.getInt("BatteryCapacity", 0);
				PULLPowerDataParser parser = new PULLPowerDataParser(
						ModelsComparisonChartActivity.this);
				PowerLists lists = parser.readList(
						IPowerDataParser.BATTERY_CMP_LIST, batteryMax);
				PowerLists listsM = parser.readList(
						IPowerDataParser.MODULE_TOTAL_LIST, batteryMax);

				if (lists==null || lists.getPower().isEmpty() || lists.getPower().get(0).length==0) {
					if(DEBUG) Log.v("ModelsComparison", "无数据");
					
					
					//BatteryStatisticsActivity.this.finish();
					Message msg = mHandler.obtainMessage(NODATA);
					mHandler.sendMessage(msg);
				    return; 
				}

				dates = lists.getTime();
				valueM = listsM.getPower();
				datesM = listsM.getTime();
				values = lists.getPower();
				values.add(valueM.get(0));
				dates.add(datesM.get(0));
				dates.add(datesM.get(0));

				begin = String.valueOf(datesM.get(0)[0].getTime());
				end = String.valueOf(datesM.get(0)[datesM.get(0).length - 1]
						.getTime());

				int length = values.get(0).length;
				double[] diff = new double[length];
				Date[] dateM = datesM.get(0);
				for (int i = 0; i < length; i++) {
					if (DEBUG)
						Log.d(tag, "D:" + String.valueOf(values.get(0)[i])
								+ "  M:" + String.valueOf(values.get(1)[i]));
					diff[i] = values.get(0)[i] - values.get(1)[i];
				}
				values.add(diff);

				ModelsComparsionData.setTitles(titles);
				ModelsComparsionData.setDates(dates);
				ModelsComparsionData.setValues(values);

				int[] colors = new int[] { Color.BLUE, Color.CYAN, Color.GREEN };
				PointStyle[] styles = new PointStyle[] { PointStyle.POINT,
						PointStyle.POINT, PointStyle.POINT };

				// 渲染
				mRenderer = ModelsComparisonChart.buildRenderer(colors, styles);
				ModelsComparisonChart.setChartSettings(mRenderer,
						"Difference between Model and Drive", "Time", "Power",
						datesM.get(0)[0].getTime(),
						datesM.get(0)[datesM.get(0).length - 1].getTime(),
						-batteryMax * 3600, batteryMax * 3600 * 2, Color.GRAY,
						Color.LTGRAY);
				mRenderer.setXLabels(12);
				mRenderer.setYLabels(10);
				mRenderer.setChartTitleTextSize(20);
				mRenderer.setTextTypeface("sans_serif", Typeface.BOLD);
				mRenderer.setLabelsTextSize(14f);
				mRenderer.setAxisTitleTextSize(15);
				mRenderer.setLegendTextSize(15);
				mRenderer.setZoomButtonsVisible(true);
				length = mRenderer.getSeriesRendererCount();
				for (int i = 0; i < length; i++) {
					XYSeriesRenderer seriesRenderer = (XYSeriesRenderer) mRenderer
							.getSeriesRendererAt(i);
					seriesRenderer.setFillBelowLine(i == length - 1);
					seriesRenderer.setFillBelowLineColor(colors[i]);
					seriesRenderer.setLineWidth(2.5f);
					seriesRenderer.setDisplayChartValues(true);
					seriesRenderer.setChartValuesTextSize(10f);
				}
				// 生成数据集
				// mDataset = ModelsComparisonChart.buildBarDataset(titles,
				// values);
				mDataset = ModelsComparisonChart.buildDateDataset(
						ModelsComparsionData.getTitles(),
						ModelsComparsionData.getDates(),
						ModelsComparsionData.getValues());
				/*
				 * if (mChartView != null) { mChartView.repaint(); }
				 */
				
				
				// mChartView.postInvalidate();

				// UPDATE是一个自己定义的整数，代表了消息ID
				Message msg = mHandler.obtainMessage(UPDATE);
				mHandler.sendMessage(msg);
				if(DEBUG) Log.d(tag, "send message");
			}
		}.start();
	}

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
							"ModelsComparisonChart" + begin + "_" + end
									+ ".jpg");
					FileOutputStream output = new FileOutputStream(file);
					bitmap.compress(CompressFormat.JPEG, 100, output);
					Toast.makeText(ModelsComparisonChartActivity.this, "保存成功",
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
		//updateByMessage();
		

	}

	/*
	 * @Override public boolean onCreateOptionsMenu(Menu menu) { menu.add(0,
	 * Menu.FIRST, 0, "设置"); menu.add(0, Menu.FIRST + 1, 0, "退出"); return
	 * super.onCreateOptionsMenu(menu); }
	 * 
	 * @Override public boolean onOptionsItemSelected(MenuItem item) { switch
	 * (item.getItemId()) { case 1: Intent mIntent = new Intent(this,
	 * UserBehaviorDetail.class); startActivity(mIntent); break; case 2: break; }
	 * return super.onOptionsItemSelected(item); }
	 */

}
