package edu.ustc.PowerAnalyser.tools;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PowerLists {

	private List<double[]> power;
	private List<Date[]> time;
	
	public PowerLists() {
		super();
		this.power = new ArrayList<double[]>();
		this.time = new ArrayList<Date[]>();
	}
	public List<double[]> getPower() {
		return power;
	}
	public void setPower(List<double[]> power) {
		this.power = power;
	}
	public List<Date[]> getTime() {
		return time;
	}
	public void setTime(List<Date[]> time) {
		this.time = time;
	}
	
	
}
