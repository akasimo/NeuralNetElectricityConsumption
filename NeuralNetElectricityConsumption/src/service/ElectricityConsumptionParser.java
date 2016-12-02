package service;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import com.google.gson.GsonBuilder;

import model.DailyEC;
import model.HourlyEC;

public class ElectricityConsumptionParser {
	
	/**
	 * Method which parses electricity consumption measurements in selected year and saves it to .json file.
	 * 
	 * @param electricityConsumptionYear	the year of parsed electricity consumption measurements
	 * @param filePath						the path to file which contains all measurements
	 * @param fileSaveDestination			the path to save the file
	 */
	public void parseBCYearlyEC(Integer electricityConsumptionYear, String filePath, String fileSaveDestination)
	{
		try 
		{
			saveToJSONFile(getBCYearlyEC(filePath, electricityConsumptionYear), fileSaveDestination, electricityConsumptionYear);
		} 
		catch (FileNotFoundException e) 
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * Method which parses all electricity consumption measurements from entire year.
	 * 
	 * @param filePath						the path to file which contains all measurements
	 * @param electricityConsumptionYear	the year of parsed electricity consumption measurements
	 * @return								the list which contains daily electricity consumption measurements from entire year
	 */
	private List<DailyEC> getBCYearlyEC(String filePath, Integer electricityConsumptionYear)
	{
		try 
		{
			List<String> dailyECFileLinesList = readFileLines(filePath);
			List<String> allYearDatesList = getAllYearDates(electricityConsumptionYear);
			List<DailyEC> dailyECList = new ArrayList<DailyEC>();
			
			for(int i = 0; i < dailyECFileLinesList.size(); i++)
			{
				String[] dailyECArray = dailyECFileLinesList.get(i).replaceAll(",",".").split("\\|");
				List<HourlyEC> hourlyECList = new ArrayList<HourlyEC>();
				
				for(int j = 0; j < dailyECArray.length; j++)
				{
					if(dailyECArray[j].length() == 0)
					{
						if(j < 10)
						{
							hourlyECList.add(new HourlyEC(0., allYearDatesList.get(i) + " 0" + j + ":00:00"));
						}
						else
						{
							hourlyECList.add(new HourlyEC(0., allYearDatesList.get(i) + " " + j + ":00:00"));
						}
					}
					else
					{
						if(j < 10)
						{
							hourlyECList.add(new HourlyEC(Double.parseDouble(dailyECArray[j]), allYearDatesList.get(i) + " 0" + j + ":00:00"));
						}
						else
						{
							hourlyECList.add(new HourlyEC(Double.parseDouble(dailyECArray[j]), allYearDatesList.get(i) + " " + j + ":00:00"));
						}
					}
				}
				
				dailyECList.add(new DailyEC(allYearDatesList.get(i), hourlyECList));
			}
			
			System.out.println("Done.");
			
			return dailyECList;						
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
				
		return null;
	}
	
	/**
	 * Method which gets all dates from selected year.
	 * 
	 * @param electricityConsumptionYear	the year which will be parsed
	 * @return								the list which contains all dates of the year
	 * @throws ParseException				the Parse Exception when the date has incorrect format
	 */
	private List<String> getAllYearDates(Integer electricityConsumptionYear) throws ParseException
	{
		List<String> datesList = new ArrayList<String>();
	    Calendar calendar = new GregorianCalendar();
	    Date startdate = new SimpleDateFormat("yyyy/MM/dd").parse(electricityConsumptionYear + "/01/01");
		Date enddate = new SimpleDateFormat("yyyy/MM/dd").parse((electricityConsumptionYear + 1) + "/01/01");
	    calendar.setTime(startdate);

	    while (calendar.getTime().before(enddate))
	    {
	        Date result = calendar.getTime();
	        datesList.add(new SimpleDateFormat("yyyy-MM-dd").format(result));
	        calendar.add(Calendar.DATE, 1);
	    }
	    
	    return datesList;
	}
	
	/**
	 * Method which reads the file line by line to list.
	 * 
	 * @param filePath		the path to file which contains all measurements
	 * @return				the list which contains all file lines
	 */
	public static List<String> readFileLines(String filePath)
	{		
		try 
		(
		    InputStream fis = new FileInputStream(filePath);
		    InputStreamReader isr = new InputStreamReader(fis, Charset.forName("UTF-8"));
		    BufferedReader br = new BufferedReader(isr);
		)
		{
			List<String> lines = new ArrayList<String>();
			String line;
			
		    while ((line = br.readLine()) != null) 
		    {		
		    	lines.add(line);
		    }
		    
		    if(lines.size() > 0)
		    {
		    	return lines;
		    }
		    else
			{
				throw new Exception("Yearly electricity consumption file is empty.");
			}		 		    
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return null;
	}
	
	/**
	 * Method which converts list of parsed electricity consumption measurements from selected year to .json file.
	 * 
	 * @param yearlyElectricityConsumptionList	the list which contains parsed electricity consumption measurements from selected year
	 * @param fileSaveDestination				the path to save the file
	 * @param electricityConsumptionYear		the year of parsed electricity consumption measurements		
	 * @throws FileNotFoundException			the File Not Found Exception when path to file is incorrect
	 */
	private void saveToJSONFile(List<DailyEC> yearlyElectricityConsumptionList, String fileSaveDestination, Integer electricityConsumptionYear) throws FileNotFoundException
	{
		PrintWriter pw = new PrintWriter(new FileOutputStream(fileSaveDestination + "_" + electricityConsumptionYear + "ElectricityConsumption.json"));
		pw.print(new GsonBuilder().setDateFormat("yyyy/MM/dd").create().toJson(yearlyElectricityConsumptionList));
	    pw.close();
	    
	    System.out.println("\nDone. Result was saved to specified file.");
	}
}
