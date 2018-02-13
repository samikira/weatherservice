package smkra.weather.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;

import com.google.gson.Gson;

import smkra.weather.model.WeatherResponse;

/**
 * Implementation of Apache's HTTP ResponseHandler, parsing the response data and returning 
 * the included weather information as WeatherResponse object
 */
public class WeatherResponseHandler implements ResponseHandler<WeatherResponse> {

	@Override
	public WeatherResponse handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
		int status = response.getStatusLine().getStatusCode();
		if(status == HttpStatus.SC_OK){
			InputStream content = response.getEntity().getContent();
			InputStreamReader reader = new InputStreamReader(content);
			try {
				WeatherResponse ret = new Gson().fromJson(reader, WeatherResponse.class);
				ret.setHttpResponse(response);
				return ret;
			} finally {
				reader.close();
			}
		}
		WeatherResponse ret = new WeatherResponse();
		ret.setHttpResponse(response);
		return ret;
		
	}

}
