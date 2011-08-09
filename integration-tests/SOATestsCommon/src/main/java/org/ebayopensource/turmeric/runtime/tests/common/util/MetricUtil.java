package org.ebayopensource.turmeric.runtime.tests.common.util;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.FactoryConfigurationError;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;



public class MetricUtil {
	
	public static HttpTestClient http = HttpTestClient.getInstance();

	
	public static String invokeHttpClient(Map<String, String> queryParams, String action) {
		String response = null;
		if (action.contentEquals("update")) {
			response = http.getResponse("http://localhost:8080/admin/v3console/UpdateConfigCategoryXml", queryParams);
		} else {
			response = http.getResponse("http://localhost:8080/admin/v3console/ViewConfigCategoryXml", queryParams);
		}
		return response; 
	}
	public static String parseXML(String resp, String name) {
		String value = null;
		try {
			DocumentBuilderFactory factory = 
				DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(new InputSource( new StringReader(resp)));
			doc.getDocumentElement ().normalize ();
			/*System.out.println ("Root element of the doc is " + 
					doc.getDocumentElement().getNodeName());*/
			NodeList listOfProperties = doc.getElementsByTagName("ConfigCategory");
//			System.out.println ("No. of nodes " + 
//					listOfProperties.getLength());
			for (int i = 0; i < listOfProperties.getLength(); i++) {

				Node listOfProperty = listOfProperties.item(i);
				if(listOfProperty.getNodeType() == Node.ELEMENT_NODE){
					Element firstPropertyElement = (Element)listOfProperty;
					NodeList list = firstPropertyElement.getElementsByTagName("attribute");
					//System.out.println("No of attribute Nodes - " + list.getLength());
					for (int j = 0; j < list.getLength(); j++) {
						Element PropElement = (Element)list.item(j);

						String key = PropElement.getAttribute("name");
						if (key.contentEquals(name)) 
							value = PropElement.getAttribute("value");
						//System.out.println(key + " - " + value);
					} 
					
				}
			} 
		} catch (FactoryConfigurationError e) {
			// unable to get a document builder factory
		} catch (ParserConfigurationException e) {
			// parser was unable to be configured
		} catch (SAXException e) {
			// parsing error
		} catch (IOException e) {

		}
		return value;
	}
	
	public static HashMap<String, String> parseXMLString(String resp) {
		HashMap<String, String> map = new HashMap<String, String>(); 
		//HashMap<String, String> tmpMap = new HashMap<String, String>(); 
		try {
			DocumentBuilderFactory factory = 
				DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(new InputSource( new StringReader(resp)));
			doc.getDocumentElement ().normalize ();
			System.out.println ("Root element of the doc is " + 
					doc.getDocumentElement().getNodeName());
			NodeList listOfProperties = doc.getElementsByTagName("Properties");
			System.out.println ("No. of nodes " + 
					listOfProperties.getLength());
			for (int i = 0; i < listOfProperties.getLength(); i++) {

				Node listOfProperty = listOfProperties.item(i);

				if(listOfProperty.getNodeType() == Node.ELEMENT_NODE){
					Element firstPropertyElement = (Element)listOfProperty;
					NodeList list = firstPropertyElement.getElementsByTagName("Property");
					System.out.println("No of Property Nodes - " + list.getLength());
					for (int j = 0; j < list.getLength(); j++) {
						Element PropElement = (Element)list.item(j);
						NodeList textProp = PropElement.getChildNodes();
						String key = PropElement.getAttribute("name");
						if (textProp.item(0) != null){
							String value = textProp.item(0).getNodeValue().trim();
							map.put(key, value);
							//System.out.println(key + " - " + value);
						} else {
							map.put(PropElement.getAttribute("name"), null);
							//System.out.println(key + " - " + null);
						}
						if (key.contentEquals("ServiceID")) 
							if (!textProp.item(0).getNodeValue().trim().contains("MetricsService")) break;
						
					} 
				}
			}  
		} catch (javax.xml.stream.FactoryConfigurationError e) {
			// unable to get a document builder factory
		} catch (ParserConfigurationException e) {
			// parser was unable to be configured
		} catch (SAXException e) {
			// parsing error
		} catch (IOException e) {

		}
		return map;
	}
}



