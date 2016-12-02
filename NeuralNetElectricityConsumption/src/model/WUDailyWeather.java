package model;

import java.util.List;

public class WUDailyWeather {
	
	String date;
	List<WUHourlyWeather> dailyWeather;
	
	/**
	 * A constructor of WUDailyWeather.
	 * Stores the daily weather measurements.
	 * 
	 * @param date			the date of weather
	 * @param dailyWeather	the list which contains hourly weather measurements
	 */
	public WUDailyWeather(String date, List<WUHourlyWeather> dailyWeather) 
	{
		this.date = date;
		this.dailyWeather = dailyWeather;
	}
	
}
