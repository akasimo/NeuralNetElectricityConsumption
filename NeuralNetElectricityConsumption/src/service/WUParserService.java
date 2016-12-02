package service;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.jsoup.Jsoup;

import com.google.gson.GsonBuilder;

import model.WUDailyWeather;
import model.WUHourlyWeather;

public class WUParserService {
	
	private PrintWriter pw;
	
	/**
	 * Method which downloads weather measurements in selected year and saves it to .json file.
	 * 
	 * @param weatherYear			the year of parsed weather measurements
	 * @param weatherLink			the array which contains two parts of link which is obligatory to parse weather measurements
	 * @param fileSaveDestination	the path to save the file
	 */
	public void parseYearlyWeather(Integer weatherYear, String[] weatherLink, String fileSaveDestination)
	{
		try 
		{
			saveToJSONFile(WUYearlyWeatherParser(weatherYear, weatherLink, fileSaveDestination), fileSaveDestination, weatherYear);
		} 
		catch (FileNotFoundException e) 
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * Method which parses weather measurements from entire year. 
	 * Works on thread and has set parse delay in order to not flood Weather Underground servers.
	 * Default delay is set to 5000ms.
	 * 
	 * @param weatherYear			the year of parsed weather measurements
	 * @param weatherLink			the array which contains two parts of link which is obligatory to parse weather measurements
	 * @param fileSaveDestination	the path to save the file
	 * @return						the list which contains daily weather measurements from entire year
	 */
	private List<WUDailyWeather> WUYearlyWeatherParser(Integer weatherYear, String[] weatherLink, String fileSaveDestination)
	{
		List<WUDailyWeather> yearlyForecastList = new ArrayList<WUDailyWeather>();
		List<String> datesList = null;
		
		try 
		{
			datesList = getAllYearDates(weatherYear);
			pw = new PrintWriter(new FileOutputStream(fileSaveDestination + "_" + weatherYear + "_HistoricWeatherLog.txt"));
		} 
		catch (ParseException | FileNotFoundException e) 
		{
			e.printStackTrace();
		}
				
		ExecutorService threadPool = Executors.newFixedThreadPool(1);
		
	    		
		for(String s : datesList)
		{
		    threadPool.execute(new Runnable() 
		    {
		         public void run()
		         {	
					try
					{							
						yearlyForecastList.add(getDailyWeather(weatherLink[0] + s + weatherLink[1], s.replaceAll("/", "-")));						
						Thread.sleep(5000);
					}
					catch (Exception e) 
					{
						e.printStackTrace();
					}
		         }
		    });
		}
	
		threadPool.shutdown();	
		
		try 
		{
			threadPool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
			pw.close();
			System.out.println("\nDone. Logs were saved to specified file.");
		} 
		catch (InterruptedException e)
		{
			 e.printStackTrace();
		}
		
		return yearlyForecastList;
	}
	
	/**
	 * Method which downloads and parse daily weather measurements located on Weather Underground servers.
	 * 
	 * @param link		the link to weather of selected day
	 * @param dailyDate	the date of selected day
	 * @return			WUDailyWeather Object which contains all daily weather measurements
	 */
	private WUDailyWeather getDailyWeather(String link, String dailyDate)
	{	
		try
		{					
			String weather = Jsoup
					.connect(link).userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:40.0) Gecko/20100101 Firefox/40.1")
					.ignoreContentType(true).timeout(10000).ignoreHttpErrors(true).execute().body();
			
			System.out.println(link);
			pw.println(link);
						
			if(!weather.isEmpty())
			{				
				List<WUHourlyWeather> dailyWeatherList = new ArrayList<WUHourlyWeather>();
				Double temperature, humidity, seaLevelPressure, windSpeed, precipitation;
				String date, tempHour = null;
				WUHourlyWeather wuhw = null;
				Integer hour;
								
				String[] dailyWeatherArray = weather.split("\n");
				
				if(dailyWeatherArray.length > 2)
				{
					if(dailyWeatherArray[2].equals("No daily or hourly history data available<br />"))
					{
						System.out.println("Amount of measurements: 0");
						pw.println("Amount of measurements: 0");
					}
					else
					{
						System.out.println("Amount of measurements: " + (dailyWeatherArray.length - 2));
						pw.println("Amount of measurements: " + (dailyWeatherArray.length - 2));
					}
				}
				else
				{
					throw new Exception("Weather information does not contain any sort of data.");
				}
								
				if(dailyWeatherArray.length == 3 && dailyWeatherArray[2].equals("No daily or hourly history data available<br />"))
				{
					System.out.println("No daily or hourly history data available error.");
					pw.println("No daily or hourly history data available error.");
					
					for(int i = 0; i < 24; i++)
					{
						if(i<10)
						{
							dailyWeatherList.add(new WUHourlyWeather(i, 0., 0., 0., 0., 0., dailyDate + " 0" + i + ":00:00"));
						}
						else
						{
							dailyWeatherList.add(new WUHourlyWeather(i, 0., 0., 0., 0., 0., dailyDate + " " + i + ":00:00"));
						}
					}					
				}
				else
				{
					// Ommitting first two lines without any sort of weather data
					for(int i = 2; i < dailyWeatherArray.length; i++)
					{
						temperature = 0.; humidity = 0.; seaLevelPressure = 0.; windSpeed = 0.; precipitation = 0.; hour = 0; date = null;					
						String[] hourlyWeatherArray = dailyWeatherArray[i].split(",");
						String[] hourArray = hourlyWeatherArray[0].replace(":", " ").split(" ");
						
						if(!hourArray[0].equals(tempHour) && i != 2)
						{
							dailyWeatherList.add(wuhw);
						}
						
						tempHour = hourArray[0];
						hour = Integer.parseInt(hourArray[0]);
						
						if(hour == 12 && hourArray[2].equals("AM"))
						{
							hour -= 12;
						}
						else if(hourArray[2].equals("PM") && hour != 12)
						{
							hour += 12;
						}						
						
						if(isNumeric(hourlyWeatherArray[1]) == true)
						{
							if(Double.parseDouble(hourlyWeatherArray[1]) > 0.)
							{
								temperature = Double.parseDouble(hourlyWeatherArray[1]);
							}
						}
						
						if(isNumeric(hourlyWeatherArray[3]) == true)
						{
							if(Double.parseDouble(hourlyWeatherArray[3]) > 0.)
							{
								humidity = Double.parseDouble(hourlyWeatherArray[3]);
							}
						}
						
						if(isNumeric(hourlyWeatherArray[4]) == true)
						{
							if(Double.parseDouble(hourlyWeatherArray[4]) > 0.)
							{
								seaLevelPressure = Double.parseDouble(hourlyWeatherArray[4]);
							}
						}
						
						if(isNumeric(hourlyWeatherArray[7]) == true)
						{
							if(Double.parseDouble(hourlyWeatherArray[7]) > 0.)
							{
								windSpeed = Double.parseDouble(hourlyWeatherArray[7]);
							}
						}
						
						if(isNumeric(hourlyWeatherArray[9]) == true)
						{
							precipitation = Double.parseDouble(hourlyWeatherArray[9]);
						}
						
						date = hourlyWeatherArray[13].substring(0, hourlyWeatherArray[13].lastIndexOf("<"));
						
						wuhw = new WUHourlyWeather(hour, temperature, humidity, seaLevelPressure, windSpeed, precipitation, date);
						
						if(i == dailyWeatherArray.length - 1)
						{
							dailyWeatherList.add(wuhw);
						}
					}
				}
				
				if(dailyWeatherList.isEmpty() == true)
				{
					throw new Exception("Weather information is empty.");
				}
				else if(dailyWeatherList.size() < 24 && dailyWeatherList.size() > 0)
				{
					System.out.println("Weather information is not complete.");
					pw.println("Weather information is not complete.");
					
					List<WUHourlyWeather> dailyWeatherFilledList = new ArrayList<WUHourlyWeather>();
					Calendar calendar = new GregorianCalendar();
					Integer first = 0, last = 0, offset = 0, counter = 0, timeFilledDate = 0, timeTempFilledDate = 0;
					String hourFilledDate = null, lastFilledDate = null;
					String[] hourFilledDateArray;
					Boolean firstRepeat = true;
									
					offset = calculateOffset(dailyWeatherArray[2]);
					
					// Add missing hours - complement to 24h
					for(int i = 0; i < dailyWeatherList.size(); i++)
					{
						last = dailyWeatherList.get(i).getHour();
						
						if(last - first >=1 && first == 0 && firstRepeat == true)
						{
							for(int j = 0; j < last - first; j++)
							{
								if(((j - offset) % 24) < 10)
								{
									dailyWeatherFilledList.add(new WUHourlyWeather(j, 0., 0., 0., 0., 0., "N/A" + " 0" + ((j - offset) % 24) + ":00:00"));
								}
								else
								{
									dailyWeatherFilledList.add(new WUHourlyWeather(j, 0., 0., 0., 0., 0., "N/A" + " " + ((j - offset) % 24) + ":00:00"));
								}
							}
							
							dailyWeatherFilledList.add(dailyWeatherList.get(i));
						}
						else if(last - first > 1)
						{
							for(int j = 0; j < last - first - 1; j++)
							{
								if(((first + j + 1 - offset) % 24) < 10)
								{
									dailyWeatherFilledList.add(new WUHourlyWeather(first + j + 1, 0., 0., 0., 0., 0., "N/A" + " 0" + ((first + j + 1 - offset) % 24) + ":00:00"));
								}
								else
								{
									dailyWeatherFilledList.add(new WUHourlyWeather(first + j + 1, 0., 0., 0., 0., 0., "N/A" + " " + ((first + j + 1 - offset) % 24) + ":00:00"));
								}
							}
							
							dailyWeatherFilledList.add(dailyWeatherList.get(i));
						}
						else if(i == dailyWeatherList.size() - 1 && last < 23)
						{
							dailyWeatherFilledList.add(dailyWeatherList.get(i));
							
							for(int j = 0; j < 23 - last; j++)
							{
								if(((last + j + 1 - offset) % 24) < 10)
								{
									dailyWeatherFilledList.add(new WUHourlyWeather(last + j + 1, 0., 0., 0., 0., 0., "N/A" + " 0" + ((last + j + 1 - offset) % 24) + ":00:00"));
								}
								else
								{
									dailyWeatherFilledList.add(new WUHourlyWeather(last + j + 1, 0., 0., 0., 0., 0., "N/A" + " " + ((last + j + 1 - offset) % 24) + ":00:00"));
								}
							}
						}
						else
						{
							dailyWeatherFilledList.add(dailyWeatherList.get(i));
						}
												
						first = last;
						
						if(firstRepeat == true)
						{
							firstRepeat = false;
						}
					}
					
					// Set appropriate date by taking time offset into account
					for(int i = 0; i < dailyWeatherFilledList.size(); i++)
					{
						hourFilledDate = dailyWeatherFilledList.get(i).getDate();
						hourFilledDateArray = hourFilledDate.split(" ");
						
						if(hourFilledDateArray[0].equals("N/A"))
						{
							counter++;
						}
						else if(!hourFilledDateArray[0].equals("N/A"))
						{
							lastFilledDate = hourFilledDateArray[0];
							timeFilledDate = Integer.parseInt(hourFilledDateArray[1].split(":")[0]);
						}
						
						if(!hourFilledDateArray[0].equals("N/A") && counter > 0)
						{
							for(int j = i - counter; j < i; j++)
							{
								timeTempFilledDate = Integer.parseInt(dailyWeatherFilledList.get(j).getDate().split(" ")[1].split(":")[0]);
								
								if(timeTempFilledDate < timeFilledDate)
								{
									dailyWeatherFilledList.get(j).setDate(lastFilledDate + " " + dailyWeatherFilledList.get(j).getDate().split(" ")[1]);
								}
								else if(timeTempFilledDate > timeFilledDate)
								{
									calendar.setTime(new SimpleDateFormat("yyyy-MM-dd").parse(lastFilledDate));
									calendar.add(Calendar.DATE, -1);
									dailyWeatherFilledList.get(j).setDate(new SimpleDateFormat("yyyy-MM-dd").format(calendar.getTime()) + " " + dailyWeatherFilledList.get(j).getDate().split(" ")[1]);
								}
								else
								{
									// For any other weird cases
									dailyWeatherFilledList.get(j).setDate(lastFilledDate + " " + dailyWeatherFilledList.get(j).getDate().split(" ")[1]);
								}
							}
							
							counter = 0;
						}
						else if(i == dailyWeatherFilledList.size() - 1 && counter > 0 && hourFilledDateArray[0].equals("N/A"))
						{
							for(int j = dailyWeatherFilledList.size() - 1 - counter; j < dailyWeatherFilledList.size(); j++)
							{
								timeTempFilledDate = Integer.parseInt(dailyWeatherFilledList.get(j).getDate().split(" ")[1].split(":")[0]);
								
								if(timeTempFilledDate > timeFilledDate)
								{
									dailyWeatherFilledList.get(j).setDate(lastFilledDate + " " + dailyWeatherFilledList.get(j).getDate().split(" ")[1]);
								}
								else if(timeTempFilledDate < timeFilledDate)
								{
									calendar.setTime(new SimpleDateFormat("yyyy-MM-dd").parse(lastFilledDate));
									calendar.add(Calendar.DATE, 1);
									dailyWeatherFilledList.get(j).setDate(new SimpleDateFormat("yyyy-MM-dd").format(calendar.getTime()) + " " + dailyWeatherFilledList.get(j).getDate().split(" ")[1]);
								}
								else
								{
									// For any other weird cases
									dailyWeatherFilledList.get(j).setDate(lastFilledDate + " " + dailyWeatherFilledList.get(j).getDate().split(" ")[1]);
								}
							}
						}
					}
					
					System.out.println("Done.");
					pw.println("Done.");
					
					return new WUDailyWeather(dailyDate, dailyWeatherFilledList);
				}
				
				System.out.println("Done.");
				pw.println("Done.");
									
				return new WUDailyWeather(dailyDate, dailyWeatherList);
			}
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}
								
		return null;
	}
	
	/**
	 * Method which calculates time offset.
	 * 
	 * @param hourlyWeather	the hourly weather measurement
	 * @return				the time offset
	 */
	private Integer calculateOffset(String hourlyWeather)
	{
		Integer firstHour = 0, secondHour = 0;
		
		String[] hourlyWeatherArray = hourlyWeather.split(",");
		String[] hourlyWeatherFirstHourArray = hourlyWeatherArray[0].replaceAll(" ", ":").split(":");
		firstHour = Integer.parseInt(hourlyWeatherFirstHourArray[0]);
		
		if(firstHour == 12 && hourlyWeatherFirstHourArray[2].equals("AM"))
		{
			firstHour -= 12;
		}
		else if(hourlyWeatherFirstHourArray[2].equals("PM") && firstHour != 12)
		{
			firstHour += 12;
		}
		
		String[] hourlyWeatherSecondHourArray = hourlyWeatherArray[hourlyWeatherArray.length - 1].substring(hourlyWeatherArray[hourlyWeatherArray.length - 1].indexOf(" ") + 1).split(":");
		secondHour = Integer.parseInt(hourlyWeatherSecondHourArray[0]);
		
		if(firstHour > secondHour)
		{
			return - (24 - firstHour + secondHour);
		}
		else
		{
			return firstHour - secondHour;
		}
	}
	
	/**
	 * Method which gets all dates from selected year.
	 * 
	 * @param weatherYear		the year which will be parsed
	 * @return					the list which contains all dates of the year
	 * @throws ParseException	the Parse Exception when the date has incorrect format
	 */
	private List<String> getAllYearDates(Integer weatherYear) throws ParseException
	{
		List<String> datesList = new ArrayList<String>();
	    Calendar calendar = new GregorianCalendar();
	    Date startdate = new SimpleDateFormat("yyyy/MM/dd").parse(weatherYear + "/01/01");
	    // Date enddate = new SimpleDateFormat("yyyy/MM/dd").parse(weatherYear + "/10/18");
		Date enddate = new SimpleDateFormat("yyyy/MM/dd").parse((weatherYear + 1) + "/01/01");
	    calendar.setTime(startdate);

	    while (calendar.getTime().before(enddate))
	    {
	        Date result = calendar.getTime();
	        datesList.add(new SimpleDateFormat("yyyy/MM/dd").format(result));
	        calendar.add(Calendar.DATE, 1);
	    }
	    
	    return datesList;
	}
	
	/**
	 * Method which converts list of parsed weather measurements from selected year to .json file.
	 * 
	 * @param yearlyWeatherList			the list which contains parsed weather measurements from selected year
	 * @param fileSaveDestination		the path to save the file
	 * @param weatherYear				the year of parsed weather measurements
	 * @throws FileNotFoundException	the File Not Found Exception when path to file is incorrect
	 */
	private void saveToJSONFile(List<WUDailyWeather> yearlyWeatherList, String fileSaveDestination, Integer weatherYear) throws FileNotFoundException
	{
		PrintWriter pw = new PrintWriter(new FileOutputStream(fileSaveDestination + "_" + weatherYear + "_HistoricWeather.json"));
		pw.print(new GsonBuilder().setDateFormat("yyyy/MM/dd").create().toJson(yearlyWeatherList));
	    pw.close();
	    
	    System.out.println("\nDone. Result was saved to specified file.");
	}
	
	/**
	 * Method which checks if String variable is Number.
	 * 
	 * @param s	the String variable to check
	 * @return	true if input variable is Number, false otherwise
	 */
	private boolean isNumeric(String s)  
	{  
		try
		{  
			Double.parseDouble(s);  
		}  
		catch(NumberFormatException e)  
		{  
			return false;
		}  
		
		return true;  
	}

}
