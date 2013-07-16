package edu.ustc.PowerAnalyser.tools;


import java.io.InputStream;
import java.util.HashMap;
import java.util.List;


public interface IPowerDataParser {
	static String File_Base_Path = "./PowerData/"; 
	
	static int BATTERY_LIST = 1;
	static int MODULE_TOTAL_LIST = 2;
	static int APP_UID_LIST = 3;
	static int BATTERY_CMP_LIST = 4;
	static int COMPONENT_LIST = 5;
	static int HW_COMPONENET = 6;
	static int APP_COMPONENT = 7;
	public List<PowerData> parse(InputStream is,int type) throws Exception;
	public String serialize(List<PowerData> data)throws Exception;
	public double[] readComponent(String path);
	public HashMap<String , String> readApps(String path);
	public PowerLists readList(int type,double TotalBattery);
	public void writeComponent(List<PowerData> data,String time,int type);
	public void writeLists(String power,String time,int type);
	public void writeLists(String power,String time,int type,String mark);
	public PowerLists readList(int type,String mark);
	
	
}
