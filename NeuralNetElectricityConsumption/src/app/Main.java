package app;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.NeuralNet.model.TrainingData;
import com.NeuralNet.service.LoadSaveNNSService;
import com.NeuralNet.service.NeuralNetworkService;

import model.Timer;
import service.ElectricityConsumptionParser;
import service.WUForecastParserService;
import service.WUParserService;

public class Main {

	public static void main(String[] args) throws FileNotFoundException
	{
		Timer timer = new Timer();
	  	timer.startMeasure();
		
		/*NeuralNetworkService nns = new NeuralNetworkService(3,3);
		nns.appendLayer(5);
		
		System.out.println("Calculating ../..");
		
		for(int i = 0; i < 10000; i++)
		{
			nns.backpropagate(new TrainingData(new Object[]{3,4.,5}, new Object[]{0.1,0.2,0.3}), 2, 3, false);
		}		
		
		System.out.println("Result of backropagation:");
		
		for(Object o : nns.propagate(new TrainingData(new Object[]{3.,4.,5.}), 2))
		{
			System.out.print(o + " ");			
		}*/
	  	
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
	
	private static String readFile(String path, Charset encoding) throws IOException 
	{
		byte[] encoded = Files.readAllBytes(Paths.get(path));
		
		return new String(encoded, encoding);
	}
	
	private static void parseWeatherHistory()
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
	
	private static void parseElectricityConsumption()
	{
		ElectricityConsumptionParser ecp = new ElectricityConsumptionParser();
		ecp.parseBCYearlyEC(2011, "PEPCOEC2011.csv", "ElectricityConsumption/PEP");
	}
	
	private static void parseWeatherForecast()
	{
		WUForecastParserService wufps = new WUForecastParserService();
		
		// String forecastLink = "https://api-ak-aws.wunderground.com/api/c991975b7f4186c0/forecast10day/hourly10day/lang:EN/units:metric/v:2.0/bestfct:1/q/zmw:21201.1.99999.json"; // Baltimore
		// String forecastLink = "https://api-ak-aws.wunderground.com/api/c991975b7f4186c0/forecast10day/hourly10day/lang:EN/units:metric/v:2.0/bestfct:1/q/zmw:21044.1.99999.json"; // Columbia		
		String forecastLink = "https://api-ak-aws.wunderground.com/api/c991975b7f4186c0/forecast10day/hourly10day/lang:EN/units:metric/v:2.0/bestfct:1/q/zmw:21146.1.99999.json"; // Severna Park
		
		wufps.parseWeatherForecast(forecastLink, "WeatherForecast/SevernaPark");
	}
	

}
