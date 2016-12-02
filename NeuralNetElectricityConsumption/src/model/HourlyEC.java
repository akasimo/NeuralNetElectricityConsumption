package model;

public class HourlyEC {
	
	Double electricityConsumption;
	String date;
	
	/**
	 * A constructor of HourlyEC.
	 * Stores the hourly electricity consumption measurement.
	 * 
	 * @param electricityConsumption	the hourly electricity consumption measurement
	 * @param date						the date of hourly electricity consumption measurement
	 */
	public HourlyEC(Double electricityConsumption, String date) 
	{
		this.electricityConsumption = electricityConsumption;
		this.date = date;
	}
	
}
