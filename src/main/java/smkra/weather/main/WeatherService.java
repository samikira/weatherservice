package smkra.weather.main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.xml.sax.SAXException;

import smkra.weather.http.WeatherRequest;
import smkra.weather.http.WeatherServiceException;
import smkra.weather.model.WeatherResponse;

/**
 * Main class of the weather service. Takes the user's parameters and requests the weather from the configured weather REST API
 * for the requested city using the given settings. 
 */
public class WeatherService 
{
	/**
	 * Name of the city to get the weather for.
	 */
	private String city = "";
	/**
	 * Name of the file to save the output to.
	 */
	private String filename = "";
	/**
	 * True, if the weather description should be printed.
	 */
	private boolean detail = false;
	/**
	 * Number of trials if the first request fails.
	 */
	private int retryCount = -1;
	/**
	 * Seconds to wait before retrying the request.
	 */
	private int secondsBeforeRetry = -1;
	/**
	 * Maximum number of seconds to wait, if a retry-after header is included in the response.
	 */
	private int maxSecondsBeforeRetry = -1;
	
	protected static PrintStream outStream = System.out;
	
	/**
	 * Main method of the weather service, requesting and printing the weather for the specified parameters.
	 * @param args the parameters for requesting the weather.
	 * @see #getUsageString()
	 * @see #getUsageHelpString()
	 */
    public static void main( String[] args )
    {
    	if(args == null || args.length < 1){
    		outStream.println(getUsageString());
    	} else {
    		WeatherService weatherService = new WeatherService();
    		if(weatherService.initializeParameter(args))
    			weatherService.getAndPrintWeather();
    	}
    }
    
    /**
     * Gives the possible parameters for the main method of this class
     * @return the possible parameters
     */
    protected static String getUsageString(){
    	return "WeatherService -c city -f filename [-h -d -r retrials -s secondsToWait -ms maxSecondsToWait]";
    }
    
    /**
     * Gives a detailed description of the possible parameters for the main method of this class
     * @return the description for the parameters
     */
    protected String getUsageHelpString(){
    	StringWriter writer = new StringWriter();
    	writer.write(getUsageString()+"\n");
    	writer.write("\t-hc\t\tprint this description\n");
    	writer.write("\t-c\t\tcity to get weather for\n");
    	writer.write("\t-f\t\tfilename of the html file to write weather to\n");
    	writer.write("\t-d (optional)\t\tif a detailed weather description shall be printed\n");
    	writer.write("\t-r (optional)\tnumber of retrials if service is not available\n");
    	writer.write("\t-s (optional)\tnumber of seconds to wait before retrying\n");
    	writer.write("\t-ms (optional)\tif service suggests how many seconds to wait before retrying, "
    			+ "\n\t\t\t\tthis number must not exceed maxSecondsToWait\n");
    	return writer.toString();
    }
    
    /**
     * Evaluates the user parameters and sets the values of the according variables.
     * @param args the user parameters
     * @return true, if the parameters are valid 
     */
    private boolean initializeParameter(String[] args){
    	for(int i=0; i<args.length; i++){
    		if(args[i].equalsIgnoreCase("-h")) {
    			outStream.println(getUsageHelpString());
    			return false;
    		}
    		if(args[i].equalsIgnoreCase("-d")) {
    			detail = true;
    		} else {
    			if(args.length < i+2){
    				outStream.println(getUsageString());
    	    		return false;
    			}
    			if(args[i].equalsIgnoreCase("-c")) {
    				city = args[++i];
    			} else if(args[i].equalsIgnoreCase("-f")) {
    				filename = args[++i];
    			} else if(args[i].equalsIgnoreCase("-r")) {
    				retryCount = Integer.parseInt(args[++i]);
    			} else if(args[i].equalsIgnoreCase("-s")) {
    				secondsBeforeRetry = Integer.parseInt(args[++i]);
    			} else if(args[i].equalsIgnoreCase("-ms")) {
    				maxSecondsBeforeRetry = Integer.parseInt(args[++i]);
    			} else {
    				outStream.println(getUsageString());
        			return false;
    			}
    		}
    	}
    	if(city.equals("") || filename.equals("")){
    		outStream.println(getUsageString());
    		return false;
    	}
    	return true;
    }
    
    /**
     * Requests the weather using the given parameters and if successful, prints the returned weather information
     * to the console and the given file 
     */
    private void getAndPrintWeather(){
    	WeatherRequest weather;
		try {
			weather = new WeatherRequest();
		} catch (IOException | WeatherServiceException | SAXException | ParserConfigurationException e1) {
			outStream.println("Could not load weather service configuration");
			return;
		}
    	if(retryCount > -1)
    		weather.setRetryCount(retryCount);
    	if(maxSecondsBeforeRetry > -1)
    		weather.setMaxSecondsBeforeRetry(maxSecondsBeforeRetry);
    	if(secondsBeforeRetry > -1)
    		weather.setSecondsBeforeRetry(secondsBeforeRetry);
    	try {
	    	WeatherResponse response = weather.getWeatherDataFromName(city);
	    	outStream.println(response.toString(false, detail));
	    	writeToFile(filename, response);
    	} catch (WeatherServiceException e){
    		outStream.println(e.getMessage());
    	} catch (FileNotFoundException e){
    		outStream.println("Could not write to file");
    	} catch (IOException e){
    		outStream.println("Could not read weather information");
    	} catch (XPathExpressionException e) {
    		outStream.println("Could not load weather service configuration");
    	} catch (Exception e) {
    		outStream.println("Unknown error occurred");
    	} finally {
    		try {
				weather.closeHttpClient();
			} catch (IOException e) {
				outStream.println("Http Client could not be closed");
			}
    	}
    }
    
    /**
     * Writes the weather information contained in the response to the given file.
     * @param fileName the name of the file, the weather information should be written to
     * @param response the response containing the weather information
     * @throws IOException if writing to the file fails 
     */
    private void writeToFile(String fileName, WeatherResponse response) throws IOException {
    	if(!fileName.endsWith(".html"))
    		fileName += ".html";
    	File file = new File(fileName);
    	PrintWriter writer = new PrintWriter(file);
    	try{
    		writer.write(response.toString(true, detail));
    		writer.flush();
    	} finally {
    		writer.close();
    	}
    	
    }
}
