package smkra.weather.xml;

import java.io.FileInputStream;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import smkra.weather.http.WeatherServiceException;

/**
 * This class maps names of cities to there IDs as required by the weather API. If available, the
 * IDs are parsed from the xml-file retrieved from the <code>AREA_XML_URL</code>. Otherwise, a older copy of 
 * this xml-file is used.
 */
public class CityID {
	
	/**
	 * The URL to the xml-file containing the mapping of city names to city IDs.
	 */
	public static String AREA_XML_URL = "http://weather.livedoor.com/forecast/rss/primary_area.xml";
	
	private Document document;
	private XPath xpath;
	
	/**
	 * Creates a new instance of this class and parses the xml-file containing the name-ID-mapping.
	 * @throws ParserConfigurationException if the XML-parser could not be created
	 * @throws WeatherServiceException If the xml-file could not be read
	 */
	public CityID() throws ParserConfigurationException, IOException, WeatherServiceException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    	DocumentBuilder builder = factory.newDocumentBuilder();
    	try {
    		document = builder.parse(AREA_XML_URL);
    	} catch (Exception e) {
    		try (FileInputStream fis = new FileInputStream("data/primary_area.xml")) {
    			document = builder.parse(fis);
    		} catch (Exception e2) {
    			throw new WeatherServiceException("Could not load configuration from weather service nor backup file.");
    		}
    	}
    	XPathFactory xPathfactory = XPathFactory.newInstance();
    	xpath = xPathfactory.newXPath();
	}
	
	/**
	 * Searches the xml-file for the given city name and returns the ID if found. If no ID could
	 * be found, an exception will be thrown.
	 * @param city The name of the city to get the ID for
	 * @return the ID of the requested city
	 * @throws XPathExpressionException If the search is invalid
	 * @throws WeatherServiceException If the ID was not found
	 */
	public String getCityID(String city) throws XPathExpressionException, WeatherServiceException {
		
    	XPathExpression expr = xpath.compile("//city[@title=\""+city+"\"]");
    	NodeList nl = (NodeList) expr.evaluate(document, XPathConstants.NODESET);
    	if(nl == null || nl.getLength() < 1)
    		throw new WeatherServiceException("Could not get weather information for specified city.");
    	Element el = (org.w3c.dom.Element) nl.item(0);
    	String attribute = el.getAttribute("id");
    	if(attribute == null || attribute == "")
    		throw new WeatherServiceException("Could not get weather information for specified city.");
    	return attribute;
	}

}
