package edu.ustc.PowerAnalyser.tools;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlSerializer;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Environment;
import android.util.Log;
import android.util.Xml;

public class PULLPowerDataParser implements IPowerDataParser {

	private static boolean DEBUG = false;
	private static boolean UBDEBUG = true;
	private List<PowerData> data;
	static String TAG = "PULLPowerDataParser";
	private Context mcontext;
	private String path;
	private static final String APP_SD_CACHE = "PowerAnalyserCache";
	private static final int NORMAL = 1;
	private static final int DETAIL = 2;

	public PULLPowerDataParser(Context mcontext, String path) {
		super();
		this.mcontext = mcontext;
		this.path = path;
	}

	public PULLPowerDataParser(Context mcontext) {
		super();
		this.mcontext = mcontext;
		path = "";
	}

	@Override
	public List<PowerData> parse(InputStream is, int type) throws Exception {
		// List<PowerData> powers = null;
		PowerData power = null;

		// XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
		// XmlPullParser parser = factory.newPullParser();

		XmlPullParser parser = Xml.newPullParser();
		parser.setInput(is, "UTF-8");

		int eventType = parser.getEventType();
		while (eventType != XmlPullParser.END_DOCUMENT) {
			switch (eventType) {
			case XmlPullParser.START_DOCUMENT:
				data = new ArrayList<PowerData>();
				break;
			case XmlPullParser.START_TAG:
				if (parser.getName().equals("ComponentItem")) {
					power = new PowerData();
				} else if (parser.getName().equals("Name")) {
					eventType = parser.next();
					power.setName(parser.getText());
					if (DEBUG)
						Log.v("parser", parser.getText());
				} else if (parser.getName().equals("Mark")) {
					eventType = parser.next();
					power.setMark(parser.getText());
					if (DEBUG)
						Log.v("parser", parser.getText());
				} else if (parser.getName().equals("Type")) {
					eventType = parser.next();
					power.setType(parser.getText());
					if (DEBUG)
						Log.v("parser", parser.getText());
				} else if (parser.getName().equals("Power")) {
					eventType = parser.next();
					power.setPower(parser.getText());
					if (DEBUG)
						Log.v("parser", parser.getText());
				} else if (parser.getName().equals("Description")) {
					eventType = parser.next();
					power.setDescription(parser.getText());
					// Log.v("parser",parser.getText());
				} else if (parser.getName().equals("Time")) {
					eventType = parser.next();
					power.setTime(parser.getText());
					if (DEBUG)
						Log.v("parser", parser.getText());
				} else if (parser.getName().equals("Percent")) {
					eventType = parser.next();
					power.setPercent(parser.getText());
					if (DEBUG)
						Log.v("parser", parser.getText());
				}
				break;
			case XmlPullParser.END_TAG:
				if (parser.getName().equals("ComponentItem")) {
					data.add(power);
					power = null;
				}
				break;
			}
			eventType = parser.next();
		}

		return data;
	}

	@Override
	public String serialize(List<PowerData> data) throws Exception {
		XmlSerializer serializer = Xml.newSerializer();
		StringWriter writer = new StringWriter();
		serializer.setOutput(writer);
		serializer.startDocument("UTF-8", true);
		serializer.startTag("", "ComponentData");

		for (PowerData power : data) {
			serializer.startTag("", "ComponentItem");
			serializer.attribute("", "Name", power.getName() + "");

			serializer.startTag("", "Mark");
			serializer.text(power.getMark());
			serializer.endTag("", "Mark");

			serializer.startTag("", "Type");
			serializer.text(power.getType() + "");
			serializer.endTag("", "Type");

			serializer.startTag("", "Power");
			serializer.text(power.getPower() + "");
			serializer.endTag("", "Power");

			serializer.startTag("", "Description");
			serializer.text(power.getDescription() + "");
			serializer.endTag("", "Description");

			serializer.startTag("", "Time");
			serializer.text(power.getTime() + "");
			serializer.endTag("", "Time");

			serializer.startTag("", "Percent");
			serializer.text(power.getPercent() + "");
			serializer.endTag("", "Percent");

			serializer.endTag("", "ComponentItem");
		}

		serializer.endTag("", "ComponentData");
		serializer.endDocument();

		return writer.toString();
	}

	@Override
	public double[] readComponent(String file_path) {
		double com[] = null;
		try {
			int type = 0;
			InputStream is = mcontext.openFileInput(file_path);
			parse(is, type);
			is.close();
			int size = data.size();
			com = new double[size];
			for (int i = 0; i < size; i++) {
				com[i] = Double.parseDouble(data.get(i).getPercent());
				if (DEBUG)
					Log.v("Component", data.get(i).getName());
			}
		} catch (Exception e) {
			Log.e(TAG + "error", e.getMessage());
		}
		return com;
	}

	@Override
	public PowerLists readList(int type, double TotalBattery) {
		PowerLists lists = new PowerLists();
		String path = null;
		String line = null;
		Vector<Double> power = new Vector<Double>();
		Vector<Date> time = new Vector<Date>();
		SimpleDateFormat sdf = new SimpleDateFormat("yy-MM-dd HH:mm:ss");

		switch (type) {
		case BATTERY_LIST:
		case BATTERY_CMP_LIST:
			path = "Battery_Level_List.txt";
			break;
		case MODULE_TOTAL_LIST:
			path = "Module_Level_List.txt";
			break;
		}
		try {
			InputStreamReader isr = new InputStreamReader(
					mcontext.openFileInput(path));
			BufferedReader br = new BufferedReader(isr);
			while (br.ready()) {
				line = br.readLine();
				power.add(Double.parseDouble(line));
				line = br.readLine();
				time.add(sdf.parse(line));

			}
			isr.close();
			br.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Double[] Dou = power.toArray(new Double[0]);
		int size = Dou.length;
		double a[] = new double[size];
		TotalBattery = TotalBattery * 3600;
		for (int i = 0; i < size; i++) {
			if (type == BATTERY_LIST || type == APP_UID_LIST)
				a[i] = Dou[i];
			else if (type == BATTERY_CMP_LIST)
				a[i] = Dou[i] * TotalBattery / 100;
			else if (type == MODULE_TOTAL_LIST)
				// a[i] = TotalBattery - Dou[i];
				a[i] = Dou[i];
			if (DEBUG)
				Log.v("Double to double", String.valueOf(a[i]));
		}
		lists.getPower().add(a);
		lists.getTime().add(time.toArray(new Date[0]));

		return lists;
	}

	@Override
	public void writeComponent(List<PowerData> data, String time, int type) {
		try {

			String xml = serialize(data);
			String path = null;
			if (DEBUG)
				Log.v("WriteComponent", String.valueOf(data.size()));
			if (DEBUG)
				Log.v("WriteComponent", xml);
			// Log.v("XML", xml);
			switch (type) {
			case HW_COMPONENET:
				path = "ComponentData.xml";
				break;
			case APP_COMPONENT:
				path = "AppData.xml";
				break;
			default:
			}
			FileOutputStream fos = mcontext.openFileOutput(path,
					Context.MODE_PRIVATE);
			fos.write(xml.getBytes("UTF-8"));
			fos.close();

		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
		}
	}

	@Override
	public void writeLists(String power, String time, int type) {
		String powerAndTime = power + System.getProperty("line.separator")
				+ time + System.getProperty("line.separator");
		String path = null;
		switch (type) {
		case BATTERY_LIST:
			path = "Battery_Level_List.txt";
			break;
		case MODULE_TOTAL_LIST:
			path = "Module_Level_List.txt";
			break;
		default:
			return;
		}
		writeTxtFile(powerAndTime, path);
	}

	protected void writeTxtFile(String power_time, String path) {

		try {
			FileOutputStream outStream = mcontext.openFileOutput(path,
					Context.MODE_APPEND);
			outStream.write(power_time.getBytes());
			if (DEBUG)
				Log.v("writeTxtFile", path);
			outStream.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Log.v("writeTxtFile", path + ":ERROR");
			e.printStackTrace();
		}
	}

	@Override
	public void writeLists(String power, String time, int type, String mark) {
		// TODO Auto-generated method stub
		String powerAndTime = power + System.getProperty("line.separator")
				+ time + System.getProperty("line.separator");
		String path = null;
		switch (type) {
		case APP_UID_LIST:
			path = mark + "_List.txt";
			break;
		case COMPONENT_LIST:
			path = mark + "COM_List.txt";
			break;
		default:
			return;
		}
		writeTxtFile(powerAndTime, path);
	}

	@Override
	public PowerLists readList(int type, String mark) {
		// TODO Auto-generated method stub
		PowerLists lists = new PowerLists();
		String path = null;
		String line = null;
		Vector<Double> power = new Vector<Double>();
		Vector<Date> time = new Vector<Date>();
		SimpleDateFormat sdf = new SimpleDateFormat("yy-MM-dd HH:mm:ss");

		switch (type) {
		case APP_UID_LIST:
			path = mark + "_List.txt";
			break;
		case COMPONENT_LIST:
			path = mark + "COM_List.txt";
			break;
		default:
		}
		Log.d(TAG," null pointer test1");
		try {
			InputStreamReader isr = new InputStreamReader(
					mcontext.openFileInput(path));
			Log.d(TAG," null pointer test2");
			BufferedReader br = new BufferedReader(isr);
			Log.d(TAG," null pointer test3");
			while (br.ready()) {
				line = br.readLine();
				power.add(Double.parseDouble(line));
				line = br.readLine();
				time.add(sdf.parse(line));

			}
			Log.d(TAG," null pointer test4");
			isr.close();
			br.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		Double[] Dou = power.toArray(new Double[0]);
		int size = Dou.length;
		double a[] = new double[size];
		for (int i = 0; i < size; i++) {
			a[i] = Dou[i];
		}
		lists.getPower().add(a);
		lists.getTime().add(time.toArray(new Date[0]));

		return lists;
	}

	@Override
	public HashMap<String, String> readApps(String path) {
		// TODO Auto-generated method stub
		HashMap<String, String> apps = new HashMap<String, String>();
		try {
			int type = 0;
			InputStream is = mcontext.openFileInput(path);
			parse(is, type);
			is.close();
			int size = data.size();
			for (int i = 0; i < size; i++) {
				apps.put(data.get(i).getMark(), data.get(i).getPercent());
				if (DEBUG)
					Log.v("Component", data.get(i).getName());
			}
		} catch (Exception e) {
			Log.e(TAG + "error", e.getMessage());
		}
		return apps;
	}

	private String[] SegLog(String line, int type) {
		String data[] = new String[3];
		String time = null;
		String name = null;
		String pkname = null;
		String str1[];
		String str2[];
		// String str3[];
		PackageManager pkmamager = mcontext.getPackageManager();
		if (type == 1) {
			str1 = line.split("\\.");
			time = str1[0];
			name = "";
		} else if (type == 2) {
			str1 = line.split("\\.");
			time = str1[0];
			str2 = line.split("cmp=");
			pkname = str2[1].split("/")[0];
			try {
				name = String.valueOf(pkmamager.getApplicationLabel(pkmamager
						.getApplicationInfo(pkname,
								PackageManager.GET_META_DATA)));
			} catch (NameNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				name = "";
			}
		}
		data[0] = time;
		data[1] = name;
		data[2] = pkname;
		if (UBDEBUG)
			Log.d(TAG, "time: " + time + "  name: " + name);
		return data;
	}
	private String[] SegLogDetail(String line,int type)
	{
		String data[] = new String[3];
		String str1[],str2[];
		String time=null,name=null,typeStr=null;
		str1 = line.split("\\.");
		time = str1[0];
		if (type == 1) {
			str2 = line.split(" ");
			if (str2[8].equals("for")) {
				if (line.contains("content provider")) {
					if (str2.length >= 11) {
						typeStr = str2[9] + " " + str2[10];
						name = str2[11].split(":")[0];
					}
				} else {
					if (str2.length >= 11) {
						typeStr = str2[9];
						name = str2[10].split(":")[0];
					}
				}
			} else {
				if (line.contains("content provider")) {
					if (str2.length >= 10) {
						typeStr = str2[8] + " " + str2[9];
						name = str2[10].split(":")[0];
					}
				} else {
					if (str2.length >= 10) {
						typeStr = str2[8];
						name = str2[9].split(":")[0];
					}
				}
			}
		}
		else if(type == 2)
		{
			str2 = line.split("cmp=")[1].split(" ");
			typeStr="activity";
			name = str2[0];
		}
		
		data[0] = time;
		data[1] = "类型"+typeStr+" 类名"+name;
		data[2] = "";
		return data;
	}

	public List<Map<String, String>> readUB(int ubtype, String pkname)
			throws IOException, NameNotFoundException {
		List<Map<String, String>> UserBData = new LinkedList<Map<String, String>>();
		boolean sdCardExist = Environment.getExternalStorageState().equals(
				android.os.Environment.MEDIA_MOUNTED);
		if (!sdCardExist)
			return UserBData;
		String SD_PATH = Environment.getExternalStorageDirectory().toString();
		File[] files = new File(SD_PATH + "/" + APP_SD_CACHE).listFiles();
		int size = files.length;
		if (UBDEBUG)
			Log.d(TAG, "Log file numbers:" + String.valueOf(size));
		String line = null;
		String data[] = null;
		String op = null;
		int type = 0;
		for (int i = 0; i < size; i++) {
			FileInputStream input = new FileInputStream(files[i]);
			DataInputStream dataIO = new DataInputStream(input);
			while ((line = dataIO.readLine()) != null) {
				if (UBDEBUG)
					Log.d(TAG, "before filter: " + line);
				if (ubtype == NORMAL) {
					if (line.contains("ActivityManager")
							&& line.contains("android.intent.action.MAIN")
							&& line.contains("com.miui.home/.launcher.Launcher")) {
						op = "回到桌面";
						type = 1;
					} else if (line.contains("ActivityManager")
							&& line.contains("android.intent.action.MAIN")) {
						op = "打开  ";
						type = 2;
					} else
						continue;
					data = SegLog(line, type);
				} else if (ubtype == DETAIL) {
					if(line.contains(pkname)&&line.contains("Start proc"))
					{
						type = 1;
					}
					else if(line.contains(pkname)&&line.contains("Starting"))
					{
						type = 2;
					}
					else
						continue;
					op = " ";
					data = SegLogDetail(line,type);
				}
				if (UBDEBUG)
					Log.d(TAG, "after filter: " + line);
				
				Map<String, String> map = new HashMap<String, String>();
				map.put("time", data[0]);
				map.put("op", op);
				map.put("name", data[1]);
				map.put("pkname",data[2]);
				UserBData.add(map);

			}
			dataIO.close();
			input.close();
			UserBData = removeDuplicateWithOrder(UserBData);
		}

		return UserBData;
	}
	public static List<Map<String, String>> removeDuplicateWithOrder(List<Map<String, String>> list) {
		        Set<Map<String, String>> set = new HashSet<Map<String, String>>();
		        List<Map<String, String>> newList = new LinkedList<Map<String, String>>();
		        for (Iterator<Map<String, String>> iter = list.iterator(); iter.hasNext();) {
		        	Map<String, String> element = iter.next();
		            if (set.add(element))
		                newList.add(element);
		        }
		        set.clear();
		        list.clear();
		        return newList;
		    }


}
