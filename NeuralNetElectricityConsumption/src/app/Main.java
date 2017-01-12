package app;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import com.NeuralNet.model.TrainingData;
import com.NeuralNet.service.LoadSaveNNSService;
import com.NeuralNet.service.NeuralNetworkService;

import model.Timer;
import service.DataLoaderService;
import service.ElectricityConsumptionParser;
import service.WUParserService;

public class Main {

	public static void main(String[] args) throws FileNotFoundException
	{
		// predictElectricityConsumption1("2017-01-16", 2015, 5, 12);
		// predictElectricityConsumption2("2017-01-16", 2015, 5, 12);
		predictElectricityConsumption3("2017-01-16", 2015, 5, 12);
	}
	
	public static void predictElectricityConsumption1(String date, Integer weatherStartYear, Integer numberOfYears, Integer hourOfDay)
	{
		/*********************************************LOADING DATA***********************************************************/
		
		DataLoaderService dls = new DataLoaderService();
		ArrayList<ArrayList<List<Object[]>>> allCitiesDailyWeatherHistoryAllYears = dls.loadWeatherHistoryData(weatherStartYear, numberOfYears, date, new String[]{"Baltimore", "Columbia", "SevernaPark"});
		
		String[] weatherForecastLink = new String[]{"https://api-ak-aws.wunderground.com/api/c991975b7f4186c0/forecast10day/hourly10day/lang:EN/units:metric/v:2.0/bestfct:1/q/zmw:21201.1.99999.json", // Baltimore
													"https://api-ak-aws.wunderground.com/api/c991975b7f4186c0/forecast10day/hourly10day/lang:EN/units:metric/v:2.0/bestfct:1/q/zmw:21044.1.99999.json", // Columbia
													"https://api-ak-aws.wunderground.com/api/c991975b7f4186c0/forecast10day/hourly10day/lang:EN/units:metric/v:2.0/bestfct:1/q/zmw:21146.1.99999.json" //Severna Park
													};
		
		ArrayList<List<Object[]>> allCitiesDailyWeatherForecast = dls.loadWeatherForecastData(weatherForecastLink, new String[]{"Baltimore", "Columbia", "SevernaPark"}, date, new String[]{"WeatherForecast/Baltimore", "WeatherForecast/Columbia", "WeatherForecast/SevernaPark"});
		List<Object[]> dailyECHistoryAllYears = dls.loadElectricityConsumptionData(weatherStartYear, numberOfYears, date, "BC");
		
		/*********************************************COMPUTE DATA AVERAGE****************************************************/
		
		List<Object[]> avgDailyWeatherForecast = dls.avgForWeatherForecastData(allCitiesDailyWeatherForecast);
		ArrayList<List<Object[]>> avgForWeatherHistoryData = dls.avgForWeatherHistoryData(allCitiesDailyWeatherHistoryAllYears);
		
		/*********************************************NORMALIZE DATA**********************************************************/
		
		ArrayList<List<Object[]>> normalizedInputWeather = dls.normalizeInputForWeatherHistoryWithWeatherForecastData(avgForWeatherHistoryData, avgDailyWeatherForecast, 1);
		List<Object[]> normalizedInputElectricityConsumption = dls.normalizeInputForElectricityConsumptionData(dailyECHistoryAllYears, 1);
		
		/***********************************************PRINT EC DATA*********************************************************/
		
		System.out.println("\nElectricity consumption data:\n");
		
		for(int i = 0; i < dailyECHistoryAllYears.get(0).length; i++)
		{
			for(int j = 0; j < dailyECHistoryAllYears.size(); j++)
			{
				System.out.print(dailyECHistoryAllYears.get(j)[i] + ", ");
			}
			
			System.out.println();
		}
		
		// Electricity - 24 (24 hours) size List with 5 objects (5 years).
		System.out.println("\nNormalized electricity consumption data:\n");
		
		for(Object[] oa : normalizedInputElectricityConsumption)
		{
			for(Object o : oa)
			{
				System.out.print(o + ", ");
			}
			
			System.out.println();
		}
		
		/***********************************************PRINT WF DATA*********************************************************/
		
		System.out.println("\nAverage weather forecast data:\n");
		
		for(Object[] oa : avgDailyWeatherForecast)
		{
			for(Object o : oa)
			{
				System.out.print(o + ", ");
			}
			
			System.out.println();
		}
		
		/***********************************************PRINT WH DATA*********************************************************/
		
		Integer avgWeatherYear = weatherStartYear;
		System.out.println("\nAverage weather history data:\n");
		
		for(List<Object[]> loa : avgForWeatherHistoryData)
		{
			System.out.println("Year: " + avgWeatherYear + "\n");
			avgWeatherYear--;
			
			for(Object[] oa : loa)
			{
				for(Object o : oa)
				{
					System.out.print(o + ", ");
				}
				
				System.out.println();
			}
			
			System.out.println();
		}
		
		System.out.println("\nNormalized weather history data:\n");
		
		for(int i = 0; i < normalizedInputWeather.get(0).size(); i++)
		{
			System.out.println("Hour: " + (i + 1));
			
			for(int j = 0; j < normalizedInputWeather.get(0).get(0).length; j++)
			{
				
				for(int k = 0; k < normalizedInputWeather.size(); k++)
				{
					System.out.print(normalizedInputWeather.get(k).get(i)[j] + ", ");
				}
				
				System.out.println();
			}
			
			System.out.println();			
		}
		
		/*************************************************PREPARE DATA*********************************************************/
		
		List<Object[]> normalizedInputHourlyWeatherHistory = new ArrayList<Object[]>();
		Object[] normalizedInputHourlyWeatherForecast = null;
		
		for(int j = 0; j < normalizedInputWeather.get(0).get(0).length; j++)
		{
			Object[] normalizedInputHourlyWeatherHistoryArray = new Object[normalizedInputWeather.size()];
			
			for(int k = 0; k < normalizedInputWeather.size(); k++)
			{
				normalizedInputHourlyWeatherHistoryArray[k] = normalizedInputWeather.get(k).get(hourOfDay - 1)[j];
			}
			
			if(j == normalizedInputWeather.get(0).get(0).length - 1)
			{
				normalizedInputHourlyWeatherForecast = normalizedInputHourlyWeatherHistoryArray;
			}
			else
			{
				normalizedInputHourlyWeatherHistory.add(normalizedInputHourlyWeatherHistoryArray);
			}
		}
			
		Object[] normalizedInputHourlyElectricityConsumption = normalizedInputElectricityConsumption.get(hourOfDay - 1);
		Object[] nonNormalizedinputHourlyElectricityConsumption = new Object[dailyECHistoryAllYears.size()];
		
		for(int j = 0; j < dailyECHistoryAllYears.size(); j++)
		{
			nonNormalizedinputHourlyElectricityConsumption[j] = dailyECHistoryAllYears.get(j)[hourOfDay -1];
		}
		
		/**********************************************PRINT PREPARED DATA*****************************************************/
		
		System.out.println("\nPrepared hourly weather history data & electricity consumption:\n");
		
		for(int i = 0; i < normalizedInputHourlyWeatherHistory.size(); i++)
		{
			for(int j = 0; j < normalizedInputHourlyWeatherHistory.get(0).length; j++)
			{
				System.out.printf("%-25s", normalizedInputHourlyWeatherHistory.get(i)[j]);
			}
			
			System.out.printf("%-25s", normalizedInputHourlyElectricityConsumption[i]);
			System.out.printf("%-25s\n", nonNormalizedinputHourlyElectricityConsumption[i]);
		}
		
		System.out.println("\nPrepared hourly weather forecast data:\n");
		
		for(Object o : normalizedInputHourlyWeatherForecast)
		{
			System.out.printf("%-25s", o);
		}
		/*********************************************CALCULATE PREDICTION*****************************************************/
		
		System.out.println("\n\n------------------------------------------------------------------------------------------------\n");
		System.out.println("Calculating ../..");
		
		List<Object> predictionResultList = new ArrayList<Object>();
		Object predicitionResult = null;
		Timer timer = new Timer();
					
		NeuralNetworkService nns = new NeuralNetworkService(5,1);
	  	nns.appendLayer(5);
		
		for(int k = 0; k < normalizedInputHourlyWeatherHistory.size(); k++)
		{			
		  	timer.startMeasure();
		  							
			for(int i = 0; i < 5000; i++)
			{
				nns.backpropagate(new TrainingData(normalizedInputHourlyWeatherHistory.get(k), new Object[]{normalizedInputHourlyElectricityConsumption[k]}), 2, 3, false);
			}
			
			System.out.print("\nResult of backpropagation: ");
			
			for(Object o : nns.propagate(new TrainingData(normalizedInputHourlyWeatherForecast), 2))
			{
				predicitionResult = calculateElectricityConsumption(nonNormalizedinputHourlyElectricityConsumption, o);
				System.out.println(o + " ~ " + predicitionResult + " MWh");
				predictionResultList.add(predicitionResult);
			}
			
			timer.endMeasure();
			System.out.println("Enlapsed Time: " + timer.enlapsedTime()/1000.00 + "s");
		}
		
		System.out.println("\n\nAverage electricity consumption result: " + evaluateAveragePredictionValue(predictionResultList) + " MWh");
	}
	
	public static void predictElectricityConsumption2(String date, Integer weatherStartYear, Integer numberOfYears, Integer hourOfDay)
	{
		/*********************************************LOADING DATA***********************************************************/
		
		DataLoaderService dls = new DataLoaderService();
		ArrayList<ArrayList<List<Object[]>>> allCitiesDailyWeatherHistoryAllYears = dls.loadWeatherHistoryData(weatherStartYear, numberOfYears, date, new String[]{"Baltimore", "Columbia", "SevernaPark"});
		ArrayList<ArrayList<List<Object[]>>> allCitiesDailyWeatherHistoryAllYearsTest = dls.loadWeatherHistoryData(weatherStartYear, numberOfYears, date, new String[]{"Clinton", "Rockville", "Waszyngton"});
		
		String[] weatherForecastLink = new String[]{"https://api-ak-aws.wunderground.com/api/c991975b7f4186c0/forecast10day/hourly10day/lang:EN/units:metric/v:2.0/bestfct:1/q/zmw:21201.1.99999.json", // Baltimore
													"https://api-ak-aws.wunderground.com/api/c991975b7f4186c0/forecast10day/hourly10day/lang:EN/units:metric/v:2.0/bestfct:1/q/zmw:21044.1.99999.json", // Columbia
													"https://api-ak-aws.wunderground.com/api/c991975b7f4186c0/forecast10day/hourly10day/lang:EN/units:metric/v:2.0/bestfct:1/q/zmw:21146.1.99999.json" //Severna Park
													};
		
		ArrayList<List<Object[]>> allCitiesDailyWeatherForecast = dls.loadWeatherForecastData(weatherForecastLink, new String[]{"Baltimore", "Columbia", "SevernaPark"}, date, new String[]{"WeatherForecast/Baltimore", "WeatherForecast/Columbia", "WeatherForecast/SevernaPark"});
		List<Object[]> dailyECHistoryAllYears = dls.loadElectricityConsumptionData(weatherStartYear, numberOfYears, date, "BC");
		
		/*********************************************COMPUTE DATA AVERAGE****************************************************/
		
		List<Object[]> avgDailyWeatherForecast = dls.avgForWeatherForecastData(allCitiesDailyWeatherForecast);
		ArrayList<List<Object[]>> avgForWeatherHistoryData = dls.avgForWeatherHistoryData(allCitiesDailyWeatherHistoryAllYears);
		ArrayList<List<Object[]>> avgForWeatherHistoryDataTest = dls.avgForWeatherHistoryData(allCitiesDailyWeatherHistoryAllYearsTest);
		
		/*********************************************NORMALIZE DATA**********************************************************/
		
		ArrayList<List<Object[]>> normalizedInputWeather = dls.normalizeInputForWeatherHistoryData(avgForWeatherHistoryData, 1);
		ArrayList<List<Object[]>> normalizedInputWeatherTest = dls.normalizeInputForWeatherHistoryWithWeatherForecastData(avgForWeatherHistoryDataTest, avgDailyWeatherForecast, 1);
		List<Object[]> normalizedInputElectricityConsumption = dls.normalizeInputForElectricityConsumptionData(dailyECHistoryAllYears, 1);
		
		/***********************************************PRINT EC DATA*********************************************************/
		
		System.out.println("\nElectricity consumption data:\n");
		
		for(int i = 0; i < dailyECHistoryAllYears.get(0).length; i++)
		{
			for(int j = 0; j < dailyECHistoryAllYears.size(); j++)
			{
				System.out.print(dailyECHistoryAllYears.get(j)[i] + ", ");
			}
			
			System.out.println();
		}
		
		// Electricity - 24 (24 hours) size List with 5 objects (5 years).
		System.out.println("\nNormalized electricity consumption data:\n");
		
		for(Object[] oa : normalizedInputElectricityConsumption)
		{
			for(Object o : oa)
			{
				System.out.print(o + ", ");
			}
			
			System.out.println();
		}
		
		/***********************************************PRINT WF DATA*********************************************************/
		
		System.out.println("\nAverage weather forecast data:\n");
		
		for(Object[] oa : avgDailyWeatherForecast)
		{
			for(Object o : oa)
			{
				System.out.print(o + ", ");
			}
			
			System.out.println();
		}
		
		/***********************************************PRINT WH DATA*********************************************************/
		
		Integer avgWeatherYear = weatherStartYear;
		System.out.println("\nAverage weather history data:\n");
		
		for(List<Object[]> loa : avgForWeatherHistoryData)
		{
			System.out.println("Year: " + avgWeatherYear + "\n");
			avgWeatherYear--;
			
			for(Object[] oa : loa)
			{
				for(Object o : oa)
				{
					System.out.print(o + ", ");
				}
				
				System.out.println();
			}
			
			System.out.println();
		}
		
		System.out.println("\nNormalized weather history data:\n");
		
		for(int i = 0; i < normalizedInputWeather.get(0).size(); i++)
		{
			System.out.println("Hour: " + (i + 1));
			
			for(int j = 0; j < normalizedInputWeather.get(0).get(0).length; j++)
			{
				
				for(int k = 0; k < normalizedInputWeather.size(); k++)
				{
					System.out.print(normalizedInputWeather.get(k).get(i)[j] + ", ");
				}
				
				System.out.println();
			}
			
			System.out.println();			
		}
		
		/*************************************************PREPARE DATA*********************************************************/
		
		List<Object[]> normalizedInputHourlyWeatherHistory = new ArrayList<Object[]>();
		Object[] normalizedInputHourlyWeatherForecast = new Object[normalizedInputWeatherTest.size()];		
		
		for(int j = 0; j < normalizedInputWeather.get(0).get(0).length; j++)
		{
			Object[] normalizedInputHourlyWeatherHistoryArray = new Object[normalizedInputWeather.size()];
			
			for(int k = 0; k < normalizedInputWeather.size(); k++)
			{
				normalizedInputHourlyWeatherHistoryArray[k] = normalizedInputWeather.get(k).get(hourOfDay - 1)[j];
			}
			
			normalizedInputHourlyWeatherHistory.add(normalizedInputHourlyWeatherHistoryArray);
		}
		
		for(int k = 0; k < normalizedInputWeatherTest.size(); k++)
		{
			normalizedInputHourlyWeatherForecast[k] = normalizedInputWeatherTest.get(k).get(hourOfDay - 1)[normalizedInputWeatherTest.get(0).get(0).length - 1];
		}

		Object[] normalizedInputHourlyElectricityConsumption = normalizedInputElectricityConsumption.get(hourOfDay - 1);
		Object[] nonNormalizedinputHourlyElectricityConsumption = new Object[dailyECHistoryAllYears.size()];
		
		for(int j = 0; j < dailyECHistoryAllYears.size(); j++)
		{
			nonNormalizedinputHourlyElectricityConsumption[j] = dailyECHistoryAllYears.get(j)[hourOfDay -1];
		}
		
		/**********************************************PRINT PREPARED DATA*****************************************************/
		
		System.out.println("\nPrepared hourly weather history data & electricity consumption:\n");
		
		for(int i = 0; i < normalizedInputHourlyWeatherHistory.size(); i++)
		{
			for(int j = 0; j < normalizedInputHourlyWeatherHistory.get(0).length; j++)
			{
				System.out.printf("%-25s", normalizedInputHourlyWeatherHistory.get(i)[j]);
			}
			
			System.out.printf("%-25s", normalizedInputHourlyElectricityConsumption[i]);
			System.out.printf("%-25s\n", nonNormalizedinputHourlyElectricityConsumption[i]);
		}
		
		System.out.println("\nPrepared hourly weather forecast data:\n");
		
		for(Object o : normalizedInputHourlyWeatherForecast)
		{
			System.out.printf("%-25s", o);
		}
		/*********************************************CALCULATE PREDICTION*****************************************************/
		
		System.out.println("\n\n------------------------------------------------------------------------------------------------\n");
		System.out.println("Calculating ../..");
				
		List<Object> predictionResultList = new ArrayList<Object>();
		Object predicitionResult = null;
		Timer timer = new Timer();
		
		NeuralNetworkService nns = new NeuralNetworkService(5,1);
	  	nns.appendLayer(5);
		
		for(int k = 0; k < normalizedInputHourlyWeatherHistory.size(); k++)
		{			
		  	timer.startMeasure();
		  							
			for(int i = 0; i < 5000; i++)
			{
				nns.backpropagate(new TrainingData(normalizedInputHourlyWeatherHistory.get(k), new Object[]{normalizedInputHourlyElectricityConsumption[k]}), 2, 3, false);
			}
			
			System.out.print("\nResult of backpropagation: ");
			
			for(Object o : nns.propagate(new TrainingData(normalizedInputHourlyWeatherForecast), 2))
			{
				predicitionResult = calculateElectricityConsumption(nonNormalizedinputHourlyElectricityConsumption, o);
				System.out.println(o + " ~ " + predicitionResult + " MWh");
				predictionResultList.add(predicitionResult);
			}
			
			timer.endMeasure();
			System.out.println("Enlapsed Time: " + timer.enlapsedTime()/1000.00 + "s");
		}
		
		System.out.println("\n\nAverage electricity consumption result: " + evaluateAveragePredictionValue(predictionResultList) + " MWh");
	}
	
	public static void predictElectricityConsumption3(String date, Integer weatherStartYear, Integer numberOfYears, Integer hourOfDay)
	{
		/*********************************************LOADING DATA***********************************************************/
		
		DataLoaderService dls = new DataLoaderService();
		ArrayList<ArrayList<List<Object[]>>> allCitiesDailyWeatherHistoryAllYearsBC = dls.loadWeatherHistoryData(weatherStartYear, numberOfYears, date, new String[]{"Baltimore", "Columbia", "SevernaPark"});
		ArrayList<ArrayList<List<Object[]>>> allCitiesDailyWeatherHistoryAllYearsPEP = dls.loadWeatherHistoryData(weatherStartYear, numberOfYears, date, new String[]{"Clinton", "Rockville", "Waszyngton"});
		
		String[] weatherForecastLink = new String[]{"https://api-ak-aws.wunderground.com/api/c991975b7f4186c0/forecast10day/hourly10day/lang:EN/units:metric/v:2.0/bestfct:1/q/zmw:21201.1.99999.json", // Baltimore
													"https://api-ak-aws.wunderground.com/api/c991975b7f4186c0/forecast10day/hourly10day/lang:EN/units:metric/v:2.0/bestfct:1/q/zmw:21044.1.99999.json", // Columbia
													"https://api-ak-aws.wunderground.com/api/c991975b7f4186c0/forecast10day/hourly10day/lang:EN/units:metric/v:2.0/bestfct:1/q/zmw:21146.1.99999.json" //Severna Park
													};
		
		ArrayList<List<Object[]>> allCitiesDailyWeatherForecast = dls.loadWeatherForecastData(weatherForecastLink, new String[]{"Baltimore", "Columbia", "SevernaPark"}, date, new String[]{"WeatherForecast/Baltimore", "WeatherForecast/Columbia", "WeatherForecast/SevernaPark"});
		List<Object[]> dailyECHistoryAllYearsBC = dls.loadElectricityConsumptionData(weatherStartYear, numberOfYears, date, "BC");
		List<Object[]> dailyECHistoryAllYearsPEP = dls.loadElectricityConsumptionData(weatherStartYear, numberOfYears, date, "PEP");
		
		/*********************************************COMPUTE DATA AVERAGE****************************************************/
		
		List<Object[]> avgDailyWeatherForecast = dls.avgForWeatherForecastData(allCitiesDailyWeatherForecast);
		ArrayList<List<Object[]>> avgForWeatherHistoryDataBC = dls.avgForWeatherHistoryData(allCitiesDailyWeatherHistoryAllYearsBC);
		ArrayList<List<Object[]>> avgForWeatherHistoryDataPEP = dls.avgForWeatherHistoryData(allCitiesDailyWeatherHistoryAllYearsPEP);
		
		/*********************************************NORMALIZE DATA**********************************************************/
		
		ArrayList<List<Object[]>> avgForWeatherHistoryDataUnion = avgForWeatherHistoryDataBC;
		avgForWeatherHistoryDataUnion.addAll(avgForWeatherHistoryDataPEP);
		
		ArrayList<List<Object[]>> normalizedInputWeather = dls.normalizeInputForWeatherHistoryWithWeatherForecastData(avgForWeatherHistoryDataUnion, avgDailyWeatherForecast, 1);
		List<Object[]> normalizedInputElectricityConsumption = dls.normalizeInputForElectricityConsumptionData(dailyECHistoryAllYearsBC, 1);
		
		/***********************************************PRINT EC DATA*********************************************************/
		
		System.out.println("\nElectricity consumption data:\n");
		
		for(int i = 0; i < dailyECHistoryAllYearsBC.get(0).length; i++)
		{
			for(int j = 0; j < dailyECHistoryAllYearsBC.size(); j++)
			{
				System.out.print(dailyECHistoryAllYearsBC.get(j)[i] + ", ");
			}
			
			System.out.println();
		}
		
		// Electricity - 24 (24 hours) size List with 5 objects (5 years).
		System.out.println("\nNormalized electricity consumption data:\n");
		
		for(Object[] oa : normalizedInputElectricityConsumption)
		{
			for(Object o : oa)
			{
				System.out.print(o + ", ");
			}
			
			System.out.println();
		}
		
		/***********************************************PRINT WF DATA*********************************************************/
		
		System.out.println("\nAverage weather forecast data:\n");
		
		for(Object[] oa : avgDailyWeatherForecast)
		{
			for(Object o : oa)
			{
				System.out.print(o + ", ");
			}
			
			System.out.println();
		}
		
		/***********************************************PRINT WH DATA*********************************************************/
		
		Integer avgWeatherYear = weatherStartYear;
		System.out.println("\nAverage weather history data:\n");
		
		for(List<Object[]> loa : avgForWeatherHistoryDataBC)
		{
			System.out.println("Year: " + avgWeatherYear + "\n");
			avgWeatherYear--;
			
			for(Object[] oa : loa)
			{
				for(Object o : oa)
				{
					System.out.print(o + ", ");
				}
				
				System.out.println();
			}
			
			System.out.println();
		}
		
		System.out.println("\nNormalized weather history data:\n");
		
		for(int i = 0; i < normalizedInputWeather.get(0).size(); i++)
		{
			System.out.println("Hour: " + (i + 1));
			
			for(int j = 0; j < normalizedInputWeather.get(0).get(0).length; j++)
			{
				
				for(int k = 0; k < normalizedInputWeather.size(); k++)
				{
					System.out.print(normalizedInputWeather.get(k).get(i)[j] + ", ");
				}
				
				System.out.println();
			}
			
			System.out.println();			
		}
		
		/*************************************************PREPARE DATA*********************************************************/
		
		List<Object[]> normalizedInputHourlyWeatherHistoryBC = new ArrayList<Object[]>();
		List<Object[]> normalizedInputHourlyWeatherHistoryPEP = new ArrayList<Object[]>();
		Object[] normalizedInputHourlyWeatherForecast = null;
		
		for(int j = 0; j < normalizedInputWeather.get(0).get(0).length; j++)
		{
			Object[] normalizedInputHourlyWeatherHistoryArray = new Object[normalizedInputWeather.size()];
			
			for(int k = 0; k < normalizedInputWeather.size(); k++)
			{
				normalizedInputHourlyWeatherHistoryArray[k] = normalizedInputWeather.get(k).get(hourOfDay - 1)[j];
			}
			
			if(j == normalizedInputWeather.get(0).get(0).length - 1)
			{
				normalizedInputHourlyWeatherForecast = normalizedInputHourlyWeatherHistoryArray;
			}
			else if(j >= ((normalizedInputWeather.get(0).get(0).length - 1) / 2))
			{
				normalizedInputHourlyWeatherHistoryPEP.add(normalizedInputHourlyWeatherHistoryArray);
			}
			else
			{
				normalizedInputHourlyWeatherHistoryBC.add(normalizedInputHourlyWeatherHistoryArray);
			}
		}
			
		Object[] normalizedInputHourlyElectricityConsumption = normalizedInputElectricityConsumption.get(hourOfDay - 1);
		Object[] nonNormalizedinputHourlyElectricityConsumption = new Object[dailyECHistoryAllYearsBC.size()];
		
		for(int j = 0; j < dailyECHistoryAllYearsBC.size(); j++)
		{
			nonNormalizedinputHourlyElectricityConsumption[j] = dailyECHistoryAllYearsBC.get(j)[hourOfDay -1];
		}
		
		/**********************************************PRINT PREPARED DATA*****************************************************/
		
		System.out.println("\nPrepared hourly weather history data & electricity consumption:\n");
		
		for(int i = 0; i < normalizedInputHourlyWeatherHistoryBC.size(); i++)
		{
			for(int j = 0; j < normalizedInputHourlyWeatherHistoryBC.get(0).length; j++)
			{
				System.out.printf("%-25s", normalizedInputHourlyWeatherHistoryBC.get(i)[j]);
			}
			
			System.out.printf("%-25s", normalizedInputHourlyElectricityConsumption[i]);
			System.out.printf("%-25s\n", nonNormalizedinputHourlyElectricityConsumption[i]);
		}
		
		System.out.println("\nPrepared hourly weather forecast data:\n");
		
		for(Object o : normalizedInputHourlyWeatherForecast)
		{
			System.out.printf("%-25s", o);
		}
		/*********************************************CALCULATE PREDICTION*****************************************************/
		
		System.out.println("\n\n------------------------------------------------------------------------------------------------\n");
		System.out.println("Calculating ../..");
		
		ArrayList<List<Object>> predictionPEPResultList = new ArrayList<List<Object>>();
		List<Object> predictionPEPAverageResultList = new ArrayList<Object>();
		List<Object> predictionResultList = new ArrayList<Object>();
		Object predicitionResult = null;
		Timer timer = new Timer();
					
		NeuralNetworkService nns = new NeuralNetworkService(5,1);
	  	nns.appendLayer(5);
		
		for(int k = 0; k < normalizedInputHourlyWeatherHistoryBC.size(); k++)
		{			
			List<Object> predictionPEPResultTempList = new ArrayList<Object>();
		  	timer.startMeasure();
		  							
			for(int i = 0; i < 5000; i++)
			{
				nns.backpropagate(new TrainingData(normalizedInputHourlyWeatherHistoryBC.get(k), new Object[]{normalizedInputHourlyElectricityConsumption[k]}), 2, 3, false);
				// nns.backpropagate(new TrainingData(normalizedInputHourlyWeatherHistoryBC.get(k), new Object[]{normalizedInputHourlyElectricityConsumption[k]}), 2, 8, false);
			}
									
			// Propagate for PEP weather history - calculate prediction for predictionPEPResultList.size() years for each year, compute average and get result
			for(int l = 0; l < normalizedInputHourlyWeatherHistoryPEP.size(); l++)
			{			
				for(Object o : nns.propagate(new TrainingData(normalizedInputHourlyWeatherHistoryPEP.get(l)), 2))
				{
					predicitionResult = calculateElectricityConsumption(nonNormalizedinputHourlyElectricityConsumption, o);
					predictionPEPResultTempList.add(predicitionResult);
				}				
			}
			
			predictionPEPResultList.add(predictionPEPResultTempList);
			
			System.out.print("\nResult of backpropagation: ");
			
			// Propagate for weather forecast
			for(Object o : nns.propagate(new TrainingData(normalizedInputHourlyWeatherForecast), 2))
			{
				predicitionResult = calculateElectricityConsumption(nonNormalizedinputHourlyElectricityConsumption, o);
				System.out.println(o + " ~ " + predicitionResult + " MWh");
				predictionResultList.add(predicitionResult);
			}
			
			timer.endMeasure();
			System.out.println("Enlapsed Time: " + timer.enlapsedTime()/1000.00 + "s");
		}
		
		for(int i = 0; i < predictionPEPResultList.get(0).size(); i++)
		{
			List<Object> predictionPEPAverageResultTempList = new ArrayList<Object>();
			
			for(int j = 0; j < predictionPEPResultList.size(); j++)
			{
				predictionPEPAverageResultTempList.add(predictionPEPResultList.get(j).get(i));
			}
			
			predictionPEPAverageResultList.add(evaluateAveragePredictionValue(predictionPEPAverageResultTempList));
		}
		
		System.out.println();
		
		for(int i = 0; i < predictionPEPAverageResultList.size(); i++)
		{		
			System.out.print("Year: " + weatherStartYear + ":     ");
			weatherStartYear--;
			System.out.printf("HD(MWh): %-25s", dailyECHistoryAllYearsPEP.get(i)[hourOfDay -1]);
			System.out.printf("PRE(MWh): %-25s", predictionPEPAverageResultList.get(i));
			System.out.println("PERC(%): " + nns.getOcs().multiplyObjects(nns.getOcs().subtractObjects(nns.getOcs().divideObjects(predictionPEPAverageResultList.get(i), dailyECHistoryAllYearsPEP.get(i)[hourOfDay -1]), 1.), 100.));
		}
		
		System.out.println("\nAverage electricity consumption result: " + evaluateAveragePredictionValue(predictionResultList) + " MWh");
	}
	
	private static Object evaluateAveragePredictionValue(List<Object> predictionResultList)
	{
		NeuralNetworkService nns = new NeuralNetworkService();
		Object averagePredictionResult = 0.;
		
		for(Object o : predictionResultList)
		{
			averagePredictionResult = nns.getOcs().addObjects(averagePredictionResult, o);
		}
		
		return nns.getOcs().divideObjects(averagePredictionResult, predictionResultList.size());
	}
	
	private static Object calculateElectricityConsumption(Object[] nonNormalizedinputHourlyElectricityConsumption, Object result)
	{
		NeuralNetworkService nns = new NeuralNetworkService();
		
		if(nns.getOcs().isEqualOrGreaterThanObjects(result, 0.) == true && nns.getOcs().isEqualOrGreaterThanObjects(1., result) == true)
		{		
			Object min = nns.getOcs().minMaxObject(nonNormalizedinputHourlyElectricityConsumption, 1);
			Object max = nns.getOcs().minMaxObject(nonNormalizedinputHourlyElectricityConsumption, 2);
			Object difference = nns.getOcs().subtractObjects(max, min);
			
			return nns.getOcs().addObjects(min, nns.getOcs().multiplyObjects(difference, result));
		}
		
		return null;
	}
	
	public static void exampleSaveState()
	{
		Timer timer = new Timer();
	  	timer.startMeasure();
		
		NeuralNetworkService nns = new NeuralNetworkService(3,3);
		nns.appendLayer(5);
		
		System.out.println("Calculating ../..");
		
		for(int i = 0; i < 5; i++)
		{
			nns.backpropagate(new TrainingData(new Object[]{3,4.,5}, new Object[]{0.1,0.2,0.3}), 2, 3, false);
		}
		
		LoadSaveNNSService lsnnss = new LoadSaveNNSService();
		lsnnss.saveNeuralNetworkService(nns, "");
		
		NeuralNetworkService nns2 = lsnnss.loadNeuralNetworkService("NeuralNetworkServiceState.json");
		
		for(int i = 0; i < 9995; i++)
		{
			nns2.backpropagate(new TrainingData(new Object[]{3,4.,5}, new Object[]{0.1,0.2,0.3}), 2, 3, false);
		}
		
		System.out.println("\nResult of backropagation:");
		
		for(Object o : nns2.propagate(new TrainingData(new Object[]{3.,4.,5.}), 2))
		{
			System.out.print(o + " ");			
		}
		
		timer.endMeasure();
		System.out.println("\nEnlapsed Time: " + timer.enlapsedTime()/1000.00 + "s");
	}
	
	public static void example()
	{
		Timer timer = new Timer();
	  	timer.startMeasure();
	  	
		NeuralNetworkService nns = new NeuralNetworkService(3,3);
		nns.appendLayer(5);
		
		System.out.println("Calculating ../..");
		
		for(int i = 0; i < 10000; i++)
		{
			nns.backpropagate(new TrainingData(new Object[]{3,4,5}, new Object[]{0.1,0.2,0.3}), 2, 3, false);
		}
		
		System.out.println("Result of backropagation:");
		
		for(Object o : nns.propagate(new TrainingData(new Object[]{3,4,5}), 2))
		{
			System.out.print(o + " ");	
		}
		
		timer.endMeasure();
		System.out.println("\nEnlapsed Time: " + timer.enlapsedTime()/1000.00 + "s");
	}
	
	public static void parseWeatherHistory()
	{
		WUParserService wups = new WUParserService();
		String[] weatherLink = new String[2];
		
		// weatherLink[0] = "https://www.wunderground.com/history/airport/KDMH/";
		// weatherLink[1] = "/DailyHistory.html?req_city=Baltimore&req_state=MD&req_statename=Maryland&reqdb.zip=21201&reqdb.magic=1&reqdb.wmo=99999&format=1";
		// weatherLink[0] = "https://www.wunderground.com/history/airport/KFME/";
		// weatherLink[1] = "/DailyHistory.html?req_city=Columbia&req_state=MD&req_statename=Maryland&reqdb.zip=21044&reqdb.magic=1&reqdb.wmo=99999&format=1";
		// weatherLink[0] = "https://www.wunderground.com/history/airport/KNAK/";
		// weatherLink[1] = "/DailyHistory.html?req_city=Severna+Park&req_state=MD&req_statename=Maryland&reqdb.zip=21146&reqdb.magic=1&reqdb.wmo=99999&format=1";
		weatherLink[0] = "https://www.wunderground.com/history/airport/KGAI/";
		weatherLink[1] = "/DailyHistory.html?req_city=Rockville&req_state=MD&req_statename=Maryland&reqdb.zip=20847&reqdb.magic=1&reqdb.wmo=99999&format=1";
		
		wups.parseYearlyWeather(2011, weatherLink, "WeatherHistory/SevernaPark/Rockville");
	}
	
	public static void parseElectricityConsumption()
	{
		ElectricityConsumptionParser ecp = new ElectricityConsumptionParser();
		ecp.parseBCYearlyEC(2011, "PEPCOEC2011.csv", "ElectricityConsumption/PEP");
	}
	
}
