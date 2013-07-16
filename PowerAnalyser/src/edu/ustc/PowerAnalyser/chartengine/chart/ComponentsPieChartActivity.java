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

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.CategorySeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.renderer.DefaultRenderer;
import org.achartengine.renderer.SimpleSeriesRenderer;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import edu.ustc.PowerAnalyser.R;
import edu.ustc.PowerAnalyser.tools.PULLPowerDataParser;
import edu.ustc.PowerAnalyser.ui.Settings;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Paint.Align;
import android.os.Bundle;
import android.os.Environment;
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
 * component demo pie chart.
 */
public class ComponentsPieChartActivity extends Activity {

	private GraphicalView mChartView;

	private DefaultRenderer mRenderer;
	private CategorySeries mDataset;

	private ImageButton mSave;
	private int index = 0;
	
	private double[] values;
	private int[] colors;
	private static final boolean DEBUG = false;
	
	AbstractChart ComponentsPieChart = new AbstractChart();

	@Override
	protected void onRestoreInstanceState(Bundle savedState) {
		super.onRestoreInstanceState(savedState);

		mRenderer = (DefaultRenderer) savedState.getSerializable("renderer");

	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putSerializable("renderer", mRenderer);

	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.xy_chart);


		//values = new double[] { 1, 1, 1, 1, 1,1 };
		colors = new int[] { Color.BLUE, Color.GREEN, Color.MAGENTA,
				Color.YELLOW, Color.CYAN,Color.RED };
		mRenderer = ComponentsPieChart.buildCategoryRenderer(colors);
		mRenderer.setZoomButtonsVisible(true);
		mRenderer.setZoomEnabled(true);
		mRenderer.setChartTitleTextSize(20);
		PULLPowerDataParser parser = new PULLPowerDataParser(this);
		
		values = parser.readComponent("ComponentData.xml");
		
		if (values==null) {
			if(DEBUG) Log.v("BatteryStatistics", "无数据");
			Toast.makeText(getApplicationContext(), "无数据", Toast.LENGTH_SHORT)
					.show();
		    this.finish();
		    return; 
		}
		
		for(double i:values)
		{
			if(DEBUG) Log.v("ComponentPie",String.valueOf(i));
		}
		if(DEBUG) Log.v("ComponentPie",String.valueOf(values.length));
		
		
		//生成数据集
		mDataset= ComponentsPieChart.buildCategoryDataset("Power consumption", values);
		
		if (mChartView != null) {
			mChartView.repaint();
		}

		//保存图片
		mSave = (ImageButton) findViewById(R.id.save);

		mSave.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				Bitmap bitmap = mChartView.toBitmap();
				try {
					
					File file = new File(Environment
							.getExternalStorageDirectory(),
							"ComponetsPieChart" + index++ + ".jpg");
					FileOutputStream output = new FileOutputStream(file);
					bitmap.compress(CompressFormat.JPEG, 100, output);
					Toast.makeText(ComponentsPieChartActivity.this, "保存成功",
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
		if (mChartView == null) {
			LinearLayout layout = (LinearLayout) findViewById(R.id.chart);
			mChartView = ChartFactory.getPieChartView(this, mDataset, mRenderer);

			layout.addView(mChartView, new LayoutParams(
					LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));

		} else {
			mChartView.repaint();
		}
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
