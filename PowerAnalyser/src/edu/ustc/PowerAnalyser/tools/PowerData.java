package edu.ustc.PowerAnalyser.tools;


public class PowerData  {

	private String Name;
	private String Mark; //Uid or others
	private String Type;
	private String Power;
	private String Description;
	private String Time;
	private String percent;
	public PowerData(String name, String mark, String type, String power,
			String description, String time) {
		super();
		Name = name;
		Mark = mark;
		Type = type;
		Power = power;
		Description = description;
		Time = time;
	}

	public PowerData() {
		super();
		Name = "";
		Mark = "";
		Type = "";
		Power = "";
		Description = "";
		Time = "";
	}


	public String getName() {
		return Name;
	}

	public void setName(String name) {
		Name = name;
	}

	public String getMark() {
		return Mark;
	}

	public void setMark(String mark) {
		Mark = mark;
	}

	public String getType() {
		return Type;
	}

	public void setType(String type) {
		Type = type;
	}

	public String getPower() {
		return Power;
	}

	public void setPower(String power) {
		this.Power = power;
	}

	public String getDescription() {
		return Description;
	}

	public void setDescription(String description) {
		this.Description = description;
	}

	public String getTime() {
		return Time;
	}

	public void setTime(String time) {
		Time = time;
	}

	public String getPercent() {
		return percent;
	}

	public void setPercent(String percent) {
		this.percent = percent;
	}

	public enum PowerType
	{
		CPU,
		GPU,
		Phone,
		Screen,
		WIFI,
		Bluetooth,
		Sensor,
		App
	};
	
}
