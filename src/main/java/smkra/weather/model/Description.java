package smkra.weather.model;

import org.apache.commons.text.StringEscapeUtils;

public class Description {
	private String text;
	private String publicTime;
	
	public String toString(){
		return toString(false, "\n");
	}
	
	public String toString(boolean html, String separator){
		if(text == null || text.equals(""))
			return "";
		
		if(html){
			String escapedText = text;
			escapedText = StringEscapeUtils.escapeHtml4(escapedText);
			return escapedText.replace("\n", "\n<br>");
		} else {
			return text;
		}
	}
}
