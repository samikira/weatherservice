package smkra.weather.http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.junit.Assert;
import org.junit.Test;
import org.xml.sax.SAXException;

import smkra.weather.model.WeatherResponse;

public class RequestTest {

	@Test
	public void testStatus500() {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		int wait = 1;
		int retryCount = 3; // default value
		try{
			PrintStream ps = new PrintStream(baos, true, "utf-8");
			TestWeatherRequest.outStream = ps;
			TestWeatherRequest request = new TestWeatherRequest(HttpStatus.SC_INTERNAL_SERVER_ERROR);
			request.setSecondsBeforeRetry(wait);
			request.getWeatherDataFromName("青森");
		} catch(Exception e){
			Assert.assertTrue("incorrect exception: "+e.getMessage(), e instanceof WeatherServiceException && e.getMessage().equals("Internal Server Error"));
			String expOutput = "Could not get weather information, automatic retry after "+wait+" seconds\n";
			for(int i=0; i<retryCount-1; i++)
				expOutput += "Could not get weather information, automatic retry after "+wait+" seconds\n";
			Assert.assertEquals("did not output retrials", baos.toString(), expOutput);
		}
	}
	
	@Test
	public void testStatus500WithRetryAfter() {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		String wait = "3";
		int retryCount = 2;
		try{
			PrintStream ps = new PrintStream(baos, true, "utf-8");
			TestWeatherRequest.outStream = ps;
			TestWeatherRequest request = new TestWeatherRequest(HttpStatus.SC_INTERNAL_SERVER_ERROR, wait);
			request.setSecondsBeforeRetry(5); // set higher value, but should prefer retry-after header
			request.setRetryCount(retryCount);
			request.getWeatherDataFromName("青森");
		} catch(Exception e){
			Assert.assertTrue("incorrect exception: "+e.getMessage(), e instanceof WeatherServiceException && e.getMessage().equals("Internal Server Error"));
			String expOutput = "Could not get weather information, automatic retry after "+wait+" seconds\n";
			for(int i=0; i<retryCount-1; i++)
				expOutput += "Could not get weather information, automatic retry after "+wait+" seconds\n";
			Assert.assertEquals("did not output retrials", baos.toString(), expOutput);
		}
	}
	
	@Test
	public void testStatus301() {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		int wait = 1;
		try{
			PrintStream ps = new PrintStream(baos, true, "utf-8");
			TestWeatherRequest.outStream = ps;
			TestWeatherRequest request = new TestWeatherRequest(HttpStatus.SC_MOVED_PERMANENTLY);
			request.setSecondsBeforeRetry(wait);
			request.getWeatherDataFromName("青森");
		} catch(Exception e){
			Assert.assertTrue("incorrect exception: "+e.getMessage(), e instanceof WeatherServiceException && e.getMessage().equals("Service moved to another location"));
			Assert.assertEquals("no output expected", baos.toString(), "");
		}
	}
	
	@Test
	public void testStatus403() {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		int wait = 1;
		try{
			PrintStream ps = new PrintStream(baos, true, "utf-8");
			TestWeatherRequest.outStream = ps;
			TestWeatherRequest request = new TestWeatherRequest(HttpStatus.SC_FORBIDDEN);
			request.setSecondsBeforeRetry(wait);
			request.getWeatherDataFromName("青森");
		} catch(Exception e){
			Assert.assertTrue("incorrect exception: "+e.getMessage(), e instanceof WeatherServiceException && e.getMessage().equals("Forbidden"));
			Assert.assertEquals("no output expected", baos.toString(), "");
		}
	}
		
	@Test
	public void testWrongUrl() {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try{
			PrintStream ps = new PrintStream(baos, true, "utf-8");
			TestWeatherRequest.outStream = ps;
			TestWeatherRequest.URI = "http://www.isawrongurl.com/service?city=";
			TestWeatherRequest request = new TestWeatherRequest(HttpStatus.SC_BAD_GATEWAY);
			request.setRetryCount(0);
			request.getWeatherDataFromName("青森");
		} catch(Exception e){
			Assert.assertTrue("incorrect exception: "+e.getMessage(), e instanceof WeatherServiceException && e.getMessage().equals("Weather service currently not available"));
			Assert.assertEquals("no output expected", baos.toString(), "");
		} finally {
			TestWeatherRequest.URI = "http://weather.livedoor.com/forecast/webservice/json/v1?city=";
		}
	}
	
}

class TestWeatherRequest extends WeatherRequest{
	
	private String retryAfter;
	private int error;
	
	protected TestWeatherRequest(int error, String retryAfter) throws IOException, WeatherServiceException, SAXException, ParserConfigurationException{
		super();
		this.retryAfter = retryAfter;
		this.error = error;
	}
	
	protected TestWeatherRequest(int error) throws IOException, WeatherServiceException, SAXException, ParserConfigurationException{
		this(error,"-1");
	}
	
	@Override
	protected WeatherResponse sendRequest(String cityID, int secondsToWait, boolean doWait) throws IOException, InterruptedException {
		WeatherResponse response = super.sendRequest(cityID, secondsToWait, doWait);
		HttpResponse httpResponse = response.getHttpResponse();
		httpResponse.setStatusCode(error);
		if(!retryAfter.equals("-1"))
			httpResponse.setHeader("Retry-After",retryAfter);
		return response;
	}
}
