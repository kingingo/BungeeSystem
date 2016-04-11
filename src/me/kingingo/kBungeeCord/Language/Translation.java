package me.kingingo.kBungeeCord.Language;

import java.util.HashMap;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import dev.wolveringer.dataserver.player.LanguageType;

public abstract class Translation {
	private HashMap<String, String> values = new HashMap<>();
	
	public abstract LanguageType getLanguage();
	public abstract void registerTranslations();
	
	public void registerTranslation(String key,String value){
		values.put(key, value);
	}
	
	public String translate(String key,Object... value){
		if(!values.containsKey(key))
			return "<Translation not found ["+key+"]>";
		String message = values.get(key);
		
		for(int i = 0;i<value.length;i++)
			message = message.replaceAll("\\{INPUT"+i+"\\}", String.valueOf(value[i]));
		return message;
	}
	public boolean hasTranslation(String name) {
		return values.containsKey(name);
	}
	
	/*
	 * NodeList list = document.getDocumentElement().getElementsByTagName("string");
				for (int i = 0; i < list.getLength(); i++) {
					Node n = list.item(i);
					if (n.getNodeType() == Node.ELEMENT_NODE) {
						Element e = (Element) n;
						translation.put(e.getAttribute("name"), ((Node) e.getChildNodes().item(0)).getNodeValue().trim().replaceAll("Â", ""));
					}
				}
	 */
}
