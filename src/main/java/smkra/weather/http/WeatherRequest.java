package smkra.weather.http;

import java.io.IOException;
import java.io.PrintStream;
import java.net.UnknownHostException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.xml.sax.SAXException;

import smkra.weather.model.WeatherResponse;
import smkra.weather.xml.CityID;

/**
 * This class sends the GET request to the weather API site and reacts if the service does not
 * respond as expected.
 * 
 * If reasonable, the GET request will be retried as often as specified in <code>retryCount</code>. 
 * Before retrying the thread will wait as many seconds as specified in <code>secondsBeforeRetry</code>.
 * A request is retried in the following cases:
 * <ul>
 * <li>If the status code of the response is one of these: 500, 502, 503 or 504</li>
 * <li>If the host can't be reached (<code>UnknownHostException</code>)</li>
 * </ul>
 * 
 * If the status code of the response is OK, the {@link WeatherResponseHandler} will parse the response content. 
 * 
 * @see WeatherResponseHandler
 * @see smkra.weather.xml.CityID
 */

public class WeatherRequest {

	protected static String URI = "http://weather.livedoor.com/forecast/webservice/json/v1?city=";
	protected static PrintStream outStream = System.out;

	/**
	 * City ID instance to get the correct ID for the requested city
	 */
	private CityID cityID = null;
	/**
	 * Number of request attempts
	 */
	private int retryCount = 3;
	/**
	 * Seconds to wait before retrying
	 */
	private int secondsBeforeRetry = 5;
	/**
	 * If the server suggests, how long to wait before retrying (using response header "retry-after"),
	 * this value is used instead of <code>secondsBeforeRetry</code> if it does not exceed maxSecondsBeforeRetry
	 */
	private int maxSecondsBeforeRetry = 10;
	
	private CloseableHttpClient httpclient;
	private WeatherResponseHandler weatherResponseHandler;
	
	/**
	 * Constructor for this class. Creates a http client instance, a response handler and the city-ID-configuration.
	 * @throws IOException if the city-ID-configuration could not be read
	 * @throws WeatherServiceException if the city-ID-configuration could not be read
	 * @throws SAXException if the city-ID-configuration could not be read
	 * @throws ParserConfigurationException if the city-ID-configuration could not be read
	 */
	public WeatherRequest() throws IOException, WeatherServiceException, SAXException, ParserConfigurationException {
		HttpClientBuilder builder = HttpClientBuilder.create().disableAutomaticRetries();
		weatherResponseHandler = new WeatherResponseHandler();
		cityID = new CityID();
		httpclient = builder.build();
	}

	/**
	 * Sends the GET request for the given city ID and reacts to the response
	 * @param cityID	the ID of the city to get weather information for
	 * @return the response containing the weather information if successful
	 * @throws WeatherServiceException if errors occurred when requesting the weather information
	 */
	public WeatherResponse getWeatherFromID(String cityID) throws WeatherServiceException {

		WeatherResponse response;
		StatusLine statusLine = null;
		int i = 0;
		int retryAfter = -1;
		boolean doWait = false;
		do {
			try {
				response = sendRequest(cityID, retryAfter, doWait);
				if (response != null) {
					HttpResponse httpResponse = response.getHttpResponse();
					statusLine = httpResponse.getStatusLine();
					switch (statusLine.getStatusCode()) {
					case HttpStatus.SC_OK:
						return response;
					case HttpStatus.SC_MOVED_PERMANENTLY:
					case HttpStatus.SC_MOVED_TEMPORARILY:
					case HttpStatus.SC_TEMPORARY_REDIRECT:
						throw new WeatherServiceException("Service moved to another location");
					case HttpStatus.SC_SERVICE_UNAVAILABLE:
					case HttpStatus.SC_INTERNAL_SERVER_ERROR:
					case HttpStatus.SC_BAD_GATEWAY:
					case HttpStatus.SC_GATEWAY_TIMEOUT:
						break;
					default:
						throw new WeatherServiceException(statusLine.getReasonPhrase());
					}
					retryAfter = -1;
					Header[] retryAfterHeaders = httpResponse.getHeaders("Retry-After");
					if (retryAfterHeaders != null && retryAfterHeaders.length > 0) {
						retryAfter = Integer.parseInt(retryAfterHeaders[0].getValue());
					}
				}
			} catch (UnknownHostException e) {
				// try again, maybe connection was down
			} catch (NumberFormatException e) {
				// value is given as date not in seconds, ignore and try again not using
				// retry-after
			} catch (Exception e){
				if(e instanceof WeatherServiceException)
					throw (WeatherServiceException)e;
				else
					throw new WeatherServiceException("Could not get weather information");
			}
			
			doWait = true;
			i++;
		} while (i <= retryCount);
		if (statusLine != null)
			throw new WeatherServiceException(statusLine.getReasonPhrase());
		else
			throw new WeatherServiceException("Weather service currently not available");
	}

	/**
	 * Looks up the corresponding ID for the city in the configuration, then
	 * sends the GET request for the given city name and reacts to the response
	 * @param city	name of the city to get weather information for
	 * @return the response containing the weather information if successful
	 * @throws XPathExpressionException if the configuration does not contain the given city name
	 * @throws WeatherServiceException if errors occurred when requesting the weather information
	 */
	public WeatherResponse getWeatherDataFromName(String city) throws WeatherServiceException, XPathExpressionException {
		return getWeatherFromID(cityID.getCityID(city));

	}

	/**
	 * Send the request for the given cityID after the <code>secondsToWait</code> seconds if <code>doWait</code> is true.
	 * @param cityID	ID of the city to get weather information for
	 * @param secondsToWait	seconds to wait before executing the request if <code>doWait</code> is true
	 * @param doWait	if the request should not be executed immediately
	 * @return the response to the executed request
	 * @throws IOException if the request could not be sent
	 * @throws InterruptedException if waiting for sending the request was interrupted
	 */
	protected WeatherResponse sendRequest(String cityID, int secondsToWait, boolean doWait) throws IOException, InterruptedException {
		if (doWait) {
			int actualSeconds = secondsBeforeRetry;
			if (secondsToWait > 0 && secondsToWait < maxSecondsBeforeRetry) {
				actualSeconds = secondsToWait;
			}
			outStream.println("Could not get weather information, automatic retry after "+actualSeconds+" seconds");
			Thread.sleep(actualSeconds*1000);
		}
		HttpGet httpget = new HttpGet(URI + cityID);
		return httpclient.execute(httpget,weatherResponseHandler);
	}
	
	public void closeHttpClient() throws IOException {
		if(httpclient != null)
			httpclient.close();
	}
	
	public int getRetryCount() {
		return retryCount;
	}
	
	public void setRetryCount(int retryCount) {
		this.retryCount = retryCount;
	}

	public int getSecondsBeforeRetry() {
		return secondsBeforeRetry;
	}

	public void setSecondsBeforeRetry(int secondsBeforeRetry) {
		this.secondsBeforeRetry = secondsBeforeRetry;
	}

	/**
	 * Get the value, used for responses with a retry-after header.
	 * If the server suggests, how long to wait before retrying (using response header "retry-after"),
	 * this value is used instead of <code>secondsBeforeRetry</code> if it does not exceed maxSecondsBeforeRetry.
	 * @return the maximum number of seconds to wait before executing a request
	 */
	public int getMaxSecondsBeforeRetry() {
		return maxSecondsBeforeRetry;
	}

	/**
	 * Set the value, used for responses with a retry-after header.
	 * If the server suggests, how long to wait before retrying (using response header "retry-after"),
	 * this value is used instead of <code>secondsBeforeRetry</code> if it does not exceed maxSecondsBeforeRetry.
	 * @param maxSecondsBeforeRetry the maximum number of seconds to wait before executing a request
	 */
	public void setMaxSecondsBeforeRetry(int maxSecondsBeforeRetry) {
		this.maxSecondsBeforeRetry = maxSecondsBeforeRetry;
	}


}
