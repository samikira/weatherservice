package smkra.weather.http;

/**
 * Exception class for errors specific to this weather service
 */
public class WeatherServiceException extends Exception {

	private static final long serialVersionUID = 8204536006967158008L;

	public WeatherServiceException(String e) {
		super(e);
	}
	
}
