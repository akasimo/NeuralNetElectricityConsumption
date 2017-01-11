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

import com.NeuralNet.service.NeuralNetworkService;

public class DataLoaderService {
	
	NeuralNetworkService nns;
	
	public DataLoaderService()
	{
		nns = new NeuralNetworkService();
	}
	
	/**
	 * Method which gets all weather measurements from selected day.
	 * 
	 * @param weatherDate	the date of weather
	 * @param filePath		the path to file which contains all measurements
	 * @return				the list of Objects. Each Object contains hourly weather measurements such as: temperature, humidity, sea level pressure, wind speed and precipitation
	 */
	private List<Object[]> getDailyWeather(String weatherDate, String filePath)
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
						
						dailyWeatherList.add(new Object[]{temperature, humidity, seaLevelPressure, windSpeed, precipitation});
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
	private Object[] getDailyECHistory(String weatherDate, String filePath)
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

	/**
	 * Method which loads weather history data for selected cities.
	 * 
	 * @param weatherStartYear	the year which should be parsed as first
	 * @param numberOfYears		the number of years which will be parsed backwards
	 * @param weatherDate		the date of the weather history
	 * @param citiesArray		the array which contains cities names used to load weather history
	 * @return					the ArrayList (which contains collected weather history data for each city included in String[] citiesArray) of ArrayList (which contains collected data for each selected year of one of selected city) of List (which contains collected data for each hour in selected day) of Object[] (which contains data of weather history for specified hour of a day)
	 */
	public ArrayList<ArrayList<List<Object[]>>> loadWeatherHistoryData(Integer weatherStartYear, Integer numberOfYears, String weatherDate, String[] citiesArray)
	{	
		ArrayList<ArrayList<List<Object[]>>> allCitiesDailyWeatherHistoryAllYears = new ArrayList<ArrayList<List<Object[]>>>();	
		String[] weatherDateArray = weatherDate.split("-");
		Integer weatherYear = weatherStartYear;
		
		for(int i = 0; i < citiesArray.length; i++)
		{
			ArrayList<List<Object[]>> dailyWeatherHistoryAllYears = new ArrayList<List<Object[]>>();
					
			for(int j = 0; j < numberOfYears; j++)
			{
				dailyWeatherHistoryAllYears.add(getDailyWeather(weatherYear + "-" + weatherDateArray[1] + "-" + weatherDateArray[2], "WeatherHistory/" + citiesArray[i] + "/" + citiesArray[i] + "_" + weatherYear + "_HistoricWeather.json"));
				weatherYear--;
			}
			
			allCitiesDailyWeatherHistoryAllYears.add(dailyWeatherHistoryAllYears);
			weatherYear = weatherStartYear;
		}
		
		return allCitiesDailyWeatherHistoryAllYears;
	}
	
	/**
	 * Method which loads weather forecast data for selected cities.
	 * 
	 * @param weatherForecastLink	the array which contains links to weather forecast
	 * @param citiesArray			the array which contains cities names used to load weather forecast - same names as in weather forecast links
	 * @param weatherDate			the date of the weather forecast
	 * @param fileSaveDestination	the array which contains files save destinations
	 * @return						the ArrayList (which contains collected weather forecast data for each city included in String[] citiesArray) of List (which contains collected data for each hour in selected day) of Object[] (which contains data of weather forecast for specified hour of a day)
	 */
	public ArrayList<List<Object[]>> loadWeatherForecastData(String[] weatherForecastLink, String[] citiesArray, String weatherDate, String[] fileSaveDestination)
	{
		try
		{
			if(weatherForecastLink.length == citiesArray.length && weatherForecastLink.length == fileSaveDestination.length)
			{
				ArrayList<List<Object[]>> allCitiesDailyWeatherForecast = new ArrayList<List<Object[]>>();
				WUForecastParserService wufps = new WUForecastParserService();
				
				for(int i = 0; i < weatherForecastLink.length; i++)
				{															
					wufps.parseWeatherForecast(weatherForecastLink[i], fileSaveDestination[i]);
					allCitiesDailyWeatherForecast.add(getDailyWeather(weatherDate, "WeatherForecast/" + citiesArray[i] + "_WeatherForecast.json"));
					
					Thread.sleep(500);
				}
			
				return allCitiesDailyWeatherForecast;
			}
			else
			{
				throw new Exception("weatherForecastLink && citiesArray && fileSaveDestination are not the same length.");
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return null;
	}
	
	/**
	 * Method which calculate average weather history parameters. It takes e.g. 3 measurements for specified hour of a selected day of 3 cities and calculates the average weather values for specified hour.
	 * 
	 * @param input	the weather history for a specified amount of years for selected cities of a selected day
	 * @return		the weather history of a selected day with average measurements
	 */
	public ArrayList<List<Object[]>> avgForWeatherHistoryData(ArrayList<ArrayList<List<Object[]>>> input)
	{
		ArrayList<List<Object[]>> avgForWeatherHistoryData = new ArrayList<List<Object[]>>();
		
		for(int i = 0; i < input.get(0).size(); i++)
		{
			ArrayList<List<Object[]>> allCitiesDailyWeatherHistoryData = new ArrayList<List<Object[]>>();
			
			for(int j = 0; j < input.size(); j++)
			{
				allCitiesDailyWeatherHistoryData.add(input.get(j).get(i));
			}
			
			avgForWeatherHistoryData.add(avgForWeatherForecastData(allCitiesDailyWeatherHistoryData));
		}
						
		return avgForWeatherHistoryData;
	}
	
	/**
	 * Method which calculate average weather forecast parameters. It takes e.g. 3 measurements for specified hour of a selected day of 3 cities and calculates the average weather values for specified hour.
	 * 
	 * @param input	the weather forecast for selected cities of a selected day
	 * @return		the weather forecast of a selected day with average measurements
	 */
	public List<Object[]> avgForWeatherForecastData(ArrayList<List<Object[]>> input)
	{
		List<Object[]> avgDailyWeatherForecast = new ArrayList<Object[]>();
		
		for(int i = 0; i < input.get(0).size(); i++)
		{
			List<Object[]> hourlyWeatherForecastList = new ArrayList<Object[]>();
			
			for(int j = 0; j < input.size(); j++)
			{
				Object[] hourlyWeatherForecast = input.get(j).get(i);
				
				for(Object o : hourlyWeatherForecast)
				{
					if(nns.getOcs().equalsZeroObject(o) == false)
					{
						hourlyWeatherForecastList.add(hourlyWeatherForecast);
						break;
					}
				}
				
				if(j == input.size() - 1)
				{
					if(hourlyWeatherForecastList.size() == 0)
					{
						hourlyWeatherForecastList.add(hourlyWeatherForecast);
					}
				}
			}
			
			if(hourlyWeatherForecastList.size() == 1)
			{
				avgDailyWeatherForecast.add(hourlyWeatherForecastList.get(0));
			}
			else
			{							
				Object[] avgHourlyWeatherForecast = new Object[hourlyWeatherForecastList.get(0).length];
				
				for(int j = 0; j < hourlyWeatherForecastList.get(0).length; j++)
				{
					Object avg = 0.;
					
					for(int k = 0; k < hourlyWeatherForecastList.size(); k++)
					{
						avg = nns.getOcs().addObjects(avg, hourlyWeatherForecastList.get(k)[j]);
					}
					
					avgHourlyWeatherForecast[j] = nns.getOcs().divideObjects(avg, hourlyWeatherForecastList.size());
				}
				
				avgDailyWeatherForecast.add(avgHourlyWeatherForecast);
			}
		}
		
		return avgDailyWeatherForecast;
	}
	
	/**
	 * Method which loads electricity consumption data for selected cities.
	 * 
	 * @param electricityConsumptionStartYear	the year which should be parsed as first
	 * @param numberOfYears						the number of years which will be parsed backwards
	 * @param electricityConsumptionDate		the date of the electricity consumption history
	 * @param electricCompanyName				the shortcut of a name of electric company used to load measurements
	 * @return									the List (which contains collected electricity consumption data for each selected year) of Object[] (which contains data for each hour in selected day)
	 */
	public List<Object[]> loadElectricityConsumptionData(Integer electricityConsumptionStartYear, Integer numberOfYears, String electricityConsumptionDate, String electricCompanyName)
	{
		List<Object[]> dailyECHistoryAllYears = new ArrayList<Object[]>();
		String[] weatherDateArray = electricityConsumptionDate.split("-");
		
		for(int i = 0; i < numberOfYears; i++)
		{
			dailyECHistoryAllYears.add(getDailyECHistory(electricityConsumptionStartYear + "-" + weatherDateArray[1] + "-" + weatherDateArray[2], "ElectricityConsumption/" + electricCompanyName + "_" + electricityConsumptionStartYear + "ElectricityConsumption.json"));
			electricityConsumptionStartYear--;
		}
		
		return dailyECHistoryAllYears;
	}
	
	/**
	 * Method which checks if all values in input vector are the same.
	 * 
	 * @param inputVector	the vector which should be checked
	 * @return				true if the all objects in input vector are equal, false if the opposite is true
	 */
	private Boolean equalityTest(Object[] inputVector)
	{		
		Boolean result = false;
		
		for(int i = 1; i < inputVector.length; i++)
		{
			if(inputVector[0].equals(inputVector[i]))
			{
				result = true;
			}
			else
			{
				return false;
			}
		}
		
		return result;
	}
	
	/**
	 * Method which normalize input for weather history data.
	 * 
	 * @param input				the weather history data of a selected day with average measurements
	 * @param normalizationType	the mode which is responsible for choosing an appropriate normalization type, 
     * 							when it is set to "1" graduation method will be activated (in range [0;1]), 
     * 							when it is set to "2" square root method will be activated (in range [-1;1])
	 * @return					the normalized weather history data of a selected day with average measurements
	 */
	public ArrayList<List<Object[]>> normalizeInputForWeatherHistoryData(ArrayList<List<Object[]>> input, Integer normalizationType)
	{
		try
		{
			ArrayList<List<Object[]>> normalizedInput = new ArrayList<List<Object[]>>();
			
			for(int i = 0; i < input.get(0).get(0).length; i++)
			{
				List<Object[]> normalizedDailyInput = new ArrayList<Object[]>();
								
				for(int j = 0; j < input.get(0).size(); j++)
				{
					Object[] inputVector = new Object[input.size()];
					
					for(int k = 0; k < input.size(); k++)
					{
						inputVector[k] = input.get(k).get(j)[i];
					}
					
					if(inputVector.length > 1)
					{
						if(equalityTest(inputVector) == false)
						{
							switch(normalizationType)
							{
								case 1:
								{
									// in range [0;1]
									normalizedDailyInput.add(nns.inputNormalizationGraduation(inputVector));
									break;
								}
								case 2:
								{
									// in range [-1;1]
									normalizedDailyInput.add(nns.inputNormalizationSquareRoot(inputVector));
									break;
								}
								default:
								{
									throw new Exception("Selected normalization type is incorrect.");
								}
							}
						}
						else
						{
							for(int k = 0; k < inputVector.length; k++)
							{
								inputVector[k] = 1.;
							}
							
							normalizedDailyInput.add(inputVector);
						}
					}
					else
					{
						normalizedDailyInput.add(inputVector);
					}
				}
				
				normalizedInput.add(normalizedDailyInput);
			}
									
			return normalizedInput;
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
				
		return null;
	}
	
	/**
	 * Method which normalize input for weather history data. The only difference between normalizeInputForWeatherHistoryData() method is that the weather forecast is normalized with weather history data.
	 * 
	 * @param input					the weather history data of a selected day with average measurements
	 * @param weatherForecastInput	the weather forecast data of a selected day with average measurements
	 * @param normalizationType		the mode which is responsible for choosing an appropriate normalization type, 
     * 								when it is set to "1" graduation method will be activated (in range [0;1]), 
     * 								when it is set to "2" square root method will be activated (in range [-1;1])
	 * @return						the normalized weather history and weather forecast data of a selected day with average measurements
	 */
	public ArrayList<List<Object[]>> normalizeInputForWeatherHistoryWithWeatherForecastData(ArrayList<List<Object[]>> input, List<Object[]> weatherForecastInput, Integer normalizationType)
	{
		try
		{
			ArrayList<List<Object[]>> normalizedInput = new ArrayList<List<Object[]>>();
			input.add(weatherForecastInput);			
			
			for(int i = 0; i < input.get(0).get(0).length; i++)
			{
				List<Object[]> normalizedDailyInput = new ArrayList<Object[]>();
								
				for(int j = 0; j < input.get(0).size(); j++)
				{
					Object[] inputVector = new Object[input.size()];
					
					for(int k = 0; k < input.size(); k++)
					{
						inputVector[k] = input.get(k).get(j)[i];
					}
					
					if(inputVector.length > 1)
					{
						if(equalityTest(inputVector) == false)
						{
							switch(normalizationType)
							{
								case 1:
								{
									// in range [0;1]
									normalizedDailyInput.add(nns.inputNormalizationGraduation(inputVector));
									break;
								}
								case 2:
								{
									// in range [-1;1]
									normalizedDailyInput.add(nns.inputNormalizationSquareRoot(inputVector));
									break;
								}
								default:
								{
									throw new Exception("Selected normalization type is incorrect.");
								}
							}
						}
						else
						{
							for(int k = 0; k < inputVector.length; k++)
							{
								inputVector[k] = 1.;
							}
							
							normalizedDailyInput.add(inputVector);
						}
					}
					else
					{
						normalizedDailyInput.add(inputVector);
					}
				}
				
				normalizedInput.add(normalizedDailyInput);
			}
									
			return normalizedInput;
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
				
		return null;
	}
	
	/**
	 * Method which normalize input for electricity consumption data.
	 * 
	 * @param input				the electricity consumption data of a selected day
	 * @param normalizationType	the mode which is responsible for choosing an appropriate normalization type, 
     * 							when it is set to "1" graduation method will be activated (in range [0;1]), 
     * 							when it is set to "2" square root method will be activated (in range [-1;1])
	 * @return					the normalized electricity consumption data of a selected day
	 */
	public List<Object[]> normalizeInputForElectricityConsumptionData(List<Object[]> input, Integer normalizationType)
	{
		try
		{
			List<Object[]> normalizedInput = new ArrayList<Object[]>();
			
			for(int i = 0; i < input.get(0).length; i++)
			{
				Object[] inputVector = new Object[input.size()];
				
				for(int j = 0; j < input.size(); j++)
				{
					inputVector[j] = input.get(j)[i];							
				}				
				
				if(inputVector.length > 1)
				{
					if(equalityTest(inputVector) == false)
					{
						switch(normalizationType)
						{
							case 1:
							{
								// in range [0;1]
								normalizedInput.add(nns.inputNormalizationGraduation(inputVector));
								break;
							}
							case 2:
							{
								// in range [-1;1]
								normalizedInput.add(nns.inputNormalizationSquareRoot(inputVector));
								break;
							}
							default:
							{
								throw new Exception("Selected normalization type is incorrect.");
							}
						}
					}
					else
					{
						for(int k = 0; k < inputVector.length; k++)
						{
							inputVector[k] = 1.;
						}
						
						normalizedInput.add(inputVector);
					}
				}
				else
				{
					normalizedInput.add(inputVector);
				}
			}
								
			return normalizedInput;
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return null;
	}
	
}
