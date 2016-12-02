package service;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;

public class WeatherLoaderService {

	/**
	 * Method which gets all weather measurements from selected day.
	 * 
	 * @param weatherDate	the date of weather
	 * @param filePath		the path to file which contains all measurements
	 * @return				the list of Objects. Each Object contains hourly weather measurements such as: temperature, humidity, sea level pressure, wind speed and precipitation
	 */
	public List<Object[]> getDailyWeather(String weatherDate, String filePath)
	{
		try 
		{
			JSONArray yearlyWeatherArray = new JSONArray(readFile(filePath, StandardCharsets.UTF_8));
			
			for(int i = 0; i < yearlyWeatherArray.length(); i++)
			{
				if(yearlyWeatherArray.getJSONObject(i).getString("date").equals(weatherDate))
				{
					JSONArray dailyWeatherArray = yearlyWeatherArray.getJSONObject(i).getJSONArray("dailyWeather");
					Object temperature = 0., humidity = 0, seaLevelPressure = 0., windSpeed = 0., precipitation = 0.;
					List<Object[]> dailyWeatherList = new ArrayList<Object[]>();
					
					for(int j = 0; j < dailyWeatherArray.length(); j++)
					{
						if(dailyWeatherArray.getJSONObject(j).has("temperature"))
						{
							temperature = dailyWeatherArray.getJSONObject(j).get("temperature");
						}
						
						if(dailyWeatherArray.getJSONObject(j).has("humidity"))
						{
							humidity = dailyWeatherArray.getJSONObject(j).get("humidity");
						}
						
						if(dailyWeatherArray.getJSONObject(j).has("seaLevelPressure"))
						{
							seaLevelPressure = dailyWeatherArray.getJSONObject(j).get("seaLevelPressure");
						}
						
						if(dailyWeatherArray.getJSONObject(j).has("windSpeed"))
						{
							windSpeed = dailyWeatherArray.getJSONObject(j).get("windSpeed");
						}

						if(dailyWeatherArray.getJSONObject(j).has("precipitation"))
						{
							precipitation = dailyWeatherArray.getJSONObject(j).get("precipitation");
						}
						
						dailyWeatherList.add( new Object[]{temperature, humidity, seaLevelPressure, windSpeed, precipitation});
					}
					
					return dailyWeatherList;
				}				
			}					
		} 
		catch (JSONException | IOException e) 
		{
			e.printStackTrace();
		}
				
		return null;
	}
		
	/**
	 * Method which gets all electricity consumption measurements from selected day.
	 * 
	 * @param weatherDate	the date of electricity consumption measurements
	 * @param filePath		the path to file which contains all measurements
	 * @return				the array of Objects which contains hourly electricity consumption measurements
	 */
	public Object[] getDailyECHistory(String weatherDate, String filePath)
	{
		try 
		{
			JSONArray yearlyECHistoryArray = new JSONArray(readFile(filePath, StandardCharsets.UTF_8));
			
			for(int i = 0; i < yearlyECHistoryArray.length(); i++)
			{
				if(yearlyECHistoryArray.getJSONObject(i).getString("date").equals(weatherDate))
				{
					JSONArray dailyECHistoryArray = yearlyECHistoryArray.getJSONObject(i).getJSONArray("dailyElectricityConsumption");
					Object[] dailyECHistoryObjectArray = new Object[dailyECHistoryArray.length()];
					
					for(int j = 0; j < dailyECHistoryArray.length(); j++)
					{
						dailyECHistoryObjectArray[j] = dailyECHistoryArray.getJSONObject(j).get("electricityConsumption");
					}
					
					return dailyECHistoryObjectArray;
				}				
			}	
		} 
		catch (JSONException | IOException e) 
		{
			e.printStackTrace();
		}
		
		return null;
	}
	
	/**
	 * Method which loads entire file content.
	 * 
	 * @param filePath		the path to file
	 * @param encoding		the type of file encoding, e.g. StandardCharsets.UTF_8
	 * @return				the content of a file
	 * @throws IOException	the I/O exception of loading a file
	 */
	private String readFile(String filePath, Charset encoding) throws IOException 
	{
		byte[] encoded = Files.readAllBytes(Paths.get(filePath));
		
		return new String(encoded, encoding);
	}
		
}
