package smkra.weather.model;

import java.io.StringWriter;

public class Forecast {
	private String date;
	private String dateLabel;
	private String telop;
	private Image image;
	private Temperature temperature;
	
	public String toString(boolean html){
		String forecastText = "";
		if(telop != null && !telop.equals(""))
			forecastText += dateLabel+"の天気: "+telop+"。 ";
		forecastText += temperature.toString();
		
		if(!forecastText.equals("") && html) {
			StringWriter writer = new StringWriter();
			writer.write("<tr>\n<td>\n");
			writer.write(forecastText+"\n");
			writer.write("</td>\n<td>\n");
			if(image.getUrl() != null && !image.getUrl().equals("")){
				writer.write("<img src=\""+image.getUrl()+"\" alt=\""+image.getTitle()+"\">\n");
			}
			writer.write("</td>\n</tr>");
			return writer.toString();
		}
		return forecastText;
	}
	
	public String toString(){
		return toString(false);
	}
}
