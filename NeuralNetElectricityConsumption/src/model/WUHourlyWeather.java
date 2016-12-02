package model;

public class WUHourlyWeather {
	
	Integer hour;
	Double temperature;
	Double humidity;
	Double seaLevelPressure;
	Double windSpeed;
	Double precipitation;
	String date;
	
	/**
	 * A constructor of WUHourlyWeather.
	 * Stores the hourly weather measurements.
	 * 
	 * @param hour				the hour in range [0;23]
	 * @param temperature		the temperature in C degrees
	 * @param humidity			the humidity measured in %
	 * @param seaLevelPressure	the sea level pressure in hPa
	 * @param windSpeed			the wind speed in km/h
	 * @param precipitation		the precipitation in mm
	 * @param date				the UTC date
	 */
	public WUHourlyWeather(Integer hour, Double temperature, Double humidity, Double seaLevelPressure, Double windSpeed, Double precipitation, String date) 
	{
		this.hour = hour;
		this.temperature = temperature;
		this.humidity = humidity;
		this.seaLevelPressure = seaLevelPressure;
		this.windSpeed = windSpeed;
		this.precipitation = precipitation;
		this.date = date;
	}

	/**
	 * Gets the hour.
	 * 
	 * @return	the hour
	 */
	public Integer getHour() {
		return hour;
	}

	/**
	 * Gets the date.
	 * 
	 * @return	the date
	 */
	public String getDate() {
		return date;
	}
	
	/**
	 * Sets the date.
	 * 
	 * @param date	the date
	 */
	public void setDate(String date) {
		this.date = date;
	}
	
}
