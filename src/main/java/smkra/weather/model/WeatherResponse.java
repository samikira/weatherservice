package smkra.weather.model;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;

/**
 * This class contains all the weather information returned by the weather API. Also the appropriate methods to print
 * the information are provided.
 */
public class WeatherResponse {
	private HttpResponse httpResponse;
	private Location location;
	private String title;
	private String link;
	private String publicTime;
	private Description description;
	private Forecast[] forecasts;
	private Link[] pinpointLocations;
	private Copyright copyright;

	@Override
	public String toString(){
		return toString(false, false);
	}
	
	/**
	 * Prints the weather information to a String.
	 * @param html If true, the appropriate html tags and the weather image are added
	 * @param detail If true, the weather description is added.
	 * @return the String representation of the contained weather information
	 */
	public String toString(boolean html, boolean detail){
		String separator;
		if(detail)
			separator = html?"\n<br><br>":"\n\n";
		else
			separator = html?"<br>\n":"\n";
		StringWriter writer = new StringWriter();
		
		if(title != null && !title.equals(""))
			writer.write(title+separator);
		else
			writer.write(getBackupTitleString()+separator);
		
		if(forecasts.length>0){
			if(html)
				writer.write("<table>\n");
			for(Forecast forecast : forecasts){
				String forecastString = forecast.toString(html);
				if(!forecastString.equals("")){
					writer.write(forecastString);
					if(!html)
						writer.write(separator);
				}
			}
			if(html)
				writer.write("</table>"+separator);
		}
		
		String descriptionString = description.toString(html, separator);
		if(detail && !descriptionString.equals(""))
			writer.write(descriptionString.toString()+separator);
		
		if(link != null && !link.equals("")){
			if (html)
				writer.write("あなたは "+"<a href=\""+link+"\">ここで</a> ぴったりの 情報を 見つけられるでしょう。");
			else
				writer.write("あなたは "+link+"で ぴったりの 情報を 見つけられるでしょう。");
		}
		return writer.toString();
	}
	
	/**
	 * Creates an alternative title if no title is contained in the response of the weather API
	 * @return the alternative title
	 */
	private String getBackupTitleString(){
		String city = (location.getCity()==null)?"the requested city":location.getCity();
		List<String> areaPrefList = new ArrayList<>();
		if(location.getPref() != null && !location.getPref().equals(""))
			areaPrefList.add(location.getPref());
		if(location.getArea() != null && !location.getArea().equals(""))
			areaPrefList.add(location.getArea());
		String areaPref = String.join(", ", areaPrefList);
		String cityDetails = "";
		if(areaPref != null && !areaPref.equals(""))
			cityDetails = " ("+areaPref+")";
				
		return "Weather forecast for "+city+cityDetails+":";
	}

	public HttpResponse getHttpResponse() {
		return httpResponse;
	}

	public void setHttpResponse(HttpResponse httpResponse) {
		this.httpResponse = httpResponse;
	}

	public Location getLocation() {
		return location;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public String getPublicTime() {
		return publicTime;
	}

	public void setPublicTime(String publicTime) {
		this.publicTime = publicTime;
	}

	public Description getDescription() {
		return description;
	}

	public void setDescription(Description description) {
		this.description = description;
	}

	public Forecast[] getForecasts() {
		return forecasts;
	}

	public void setForecasts(Forecast[] forecasts) {
		this.forecasts = forecasts;
	}

	public Link[] getPinpointLocations() {
		return pinpointLocations;
	}

	public void setPinpointLocations(Link[] pinpointLocations) {
		this.pinpointLocations = pinpointLocations;
	}

	public Copyright getCopyright() {
		return copyright;
	}

	public void setCopyright(Copyright copyright) {
		this.copyright = copyright;
	}
	
}
