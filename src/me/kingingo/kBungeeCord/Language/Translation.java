package me.kingingo.kBungeeCord.Language;

import java.util.HashMap;

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
}
