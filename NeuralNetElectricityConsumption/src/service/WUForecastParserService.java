package service;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;

import com.google.gson.GsonBuilder;

import model.WUDailyWeather;
import model.WUHourlyWeather;

public class WUForecastParserService {

	/**
	 * Method which downloads weather forecast for the next nine days and saves it to .json file.
	 * 
	 * @param forecastLink			the link to weather forecast
	 * @param fileSaveDestination	the path to save the file
	 */
	public void parseWeatherForecast(String forecastLink, String fileSaveDestination)
	{
		try 
		{
			saveToJSONFile(getWeatherForecast(forecastLink), fileSaveDestination);
		} 
		catch (FileNotFoundException e) 
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * Method which downloads and parse weather forecast for the next nine days which is located on Weather Underground servers.
	 * 
	 * @param forecastLink	the link to weather forecast
	 * @return				the list which contains daily weather forecast for the next nine days
	 */
	private List<WUDailyWeather> getWeatherForecast(String forecastLink)
	{	
		try
		{					
			String weatherForecast = Jsoup
					.connect(forecastLink).userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:40.0) Gecko/20100101 Firefox/40.1")
					.ignoreContentType(true).timeout(5000).ignoreHttpErrors(true).execute().body();			
			
			System.out.println(forecastLink);
						
			if(isJSON(weatherForecast) == true)
			{				
				JSONObject jsonObject = new JSONObject(weatherForecast);
				JSONObject forecastObject = jsonObject.getJSONObject("forecast");
				JSONArray daysArray = (JSONArray) forecastObject.get("days");
				
				if(daysArray.length() > 0)
				{
					List<WUDailyWeather> weatherForecastList = new ArrayList<WUDailyWeather>();
					Double temperature, humidity, seaLevelPressure, windSpeed, precipitation;
					String date, dailyDate, dayDate, hourDate, iso8601Date;
					Integer hour, offset, hourDateInt;
					Calendar calendar = new GregorianCalendar();
					JSONArray hoursArray;
					List<WUHourlyWeather> hourlyWeatherForecastList;
					
					for(int i = 1; i < daysArray.length(); i++)
					{
						hoursArray = (JSONArray) daysArray.getJSONObject(i).get("hours");
						hourlyWeatherForecastList = new ArrayList<WUHourlyWeather>();
						dailyDate = null;
						
						for(int j = 0; j < hoursArray.length(); j++)
						{													
							temperature = 0.; humidity = 0.; seaLevelPressure = 0.; windSpeed = 0.; precipitation = 0.; hour = 0; offset = 0; hourDateInt = 0; date = null; dayDate = null; hourDate = null; iso8601Date = null;
														
							hour = j;
							iso8601Date = hoursArray.getJSONObject(j).getJSONObject("date").getString("iso8601");							
							offset = hoursArray.getJSONObject(j).getJSONObject("date").getInt("tz_offset_hours");
							temperature =  hoursArray.getJSONObject(j).getDouble("temperature");
							windSpeed = hoursArray.getJSONObject(j).getDouble("wind_speed");
							humidity  =  hoursArray.getJSONObject(j).getDouble("humidity");
							precipitation = hoursArray.getJSONObject(j).getDouble("liquid_precip");
							seaLevelPressure = hoursArray.getJSONObject(j).getDouble("pressure");
							
							dayDate = iso8601Date.substring(0, iso8601Date.indexOf("T"));
							
							if(j == 12)
							{
								dailyDate = dayDate;
							}
							
							hourDate = iso8601Date.substring(iso8601Date.indexOf("T") + 1);
							hourDate = hourDate.substring(0, hourDate.lastIndexOf("-"));
							hourDateInt = Integer.parseInt(hourDate.split(":")[0]);

							if(hourDateInt >= 24 + offset)
							{
								calendar.setTime(new SimpleDateFormat("yyyy-MM-dd").parse(dayDate));
								calendar.add(Calendar.DATE, 1);
								dayDate = new SimpleDateFormat("yyyy-MM-dd").format(calendar.getTime());
							}
							else if(hourDateInt - offset < 0)
							{
								calendar.setTime(new SimpleDateFormat("yyyy-MM-dd").parse(dayDate));
								calendar.add(Calendar.DATE, -1);
								dayDate = new SimpleDateFormat("yyyy-MM-dd").format(calendar.getTime());
							}
							
							if((hourDateInt - offset) % 24 < 10)
							{
								date = dayDate + " 0" + ((hourDateInt - offset) % 24) + ":00:00";
							}
							else
							{
								date = dayDate + " " + ((hourDateInt - offset) % 24) + ":00:00";
							}
							
							hourlyWeatherForecastList.add(new WUHourlyWeather(hour, temperature, humidity, seaLevelPressure, windSpeed, precipitation, date));
						}
						
						weatherForecastList.add(new WUDailyWeather(dailyDate, hourlyWeatherForecastList));
					}
					
					System.out.println("Done.");
					
					return weatherForecastList;
				}
				else
				{
					throw new Exception("Downloaded weather forecast days array is corrupted.");
				}
			}
			else
			{
				throw new Exception("Downloaded weather forecast is corrupted.");
			}
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}
								
		return null;
	}
	
	/**
	 * Method which converts list of parsed weather measurements from selected year to .json file.
	 * 
	 * @param yearlyWeatherList			the list which contains parsed weather measurements from selected year
	 * @param fileSaveDestination		the path to save the file
	 * @param weatherYear				the year of parsed weather measurements
	 * @throws FileNotFoundException	the File Not Found Exception when path to file is incorrect
	 */
	private void saveToJSONFile(List<WUDailyWeather> weatherForecastList, String fileSaveDestination) throws FileNotFoundException
	{
		PrintWriter pw = new PrintWriter(new FileOutputStream(fileSaveDestination + "_WeatherForecast.json"));
		pw.print(new GsonBuilder().setDateFormat("yyyy/MM/dd").create().toJson(weatherForecastList));
	    pw.close();
	    
	    System.out.println("\nDone. Result was saved to specified file.");
	}
	
	/**
	 * Method which checks if String variable has a structure of JSONObject or JSONArray.
	 * 
	 * @param jsonToTest	the String variable to check
	 * @return				true if input variable has a structure of JSONObject or JSONArray, false otherwise
	 */
	private boolean isJSON(String jsonToTest) 
	{
	    try 
	    {
	        new JSONObject(jsonToTest);
	    } 
	    catch (JSONException ex) 
	    {
	        try 
	        {
	            new JSONArray(jsonToTest);
	        } 
	        catch (JSONException ex1) 
	        {
	            return false;
	        }
	    }
	    
	    return true;
	}
	
}
