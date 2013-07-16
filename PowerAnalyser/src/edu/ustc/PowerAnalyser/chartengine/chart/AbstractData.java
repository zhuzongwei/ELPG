package edu.ustc.PowerAnalyser.chartengine.chart;

import java.util.Date;
import java.util.List;

public class AbstractData {
	private String[] titles;
	private List<double[]> values;
	private List<Date[]> dates;

	public String[] getTitles() {
		return titles;
	}

	public void setTitles(String[] titles) {
		this.titles = titles;
	}

	public List<double[]> getValues() {
		return values;
	}

	public void setValues(List<double[]> values) {
		this.values = values;
	}

	public List<Date[]> getDates() {
		return dates;
	}

	public void setDates(List<Date[]> dates) {
		this.dates = dates;
	}

}
