package model;

import java.util.List;

public class DailyEC {

	String date;
	List<HourlyEC> dailyElectricityConsumption;
	
	/**
	 * A constructor of DailyEC.
	 * Stores the daily electricity consumption measurements.
	 * 
	 * @param date							the date of electricity consumption measurements
	 * @param dailyElectricityConsumption	the list which contains hourly electricity consumption measurements
	 */
	public DailyEC(String date, List<HourlyEC> dailyElectricityConsumption)	
	{
		this.date = date;
		this.dailyElectricityConsumption = dailyElectricityConsumption;
	}
		
}
