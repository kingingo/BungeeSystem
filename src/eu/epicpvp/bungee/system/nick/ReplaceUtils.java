package dev.wolveringer.nick;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dev.wolveringer.chat.ChatSerializer;
import dev.wolveringer.chat.IChatBaseComponent;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;

public class ReplaceUtils {
	public static final Pattern NICKNAME_PATTERN = Pattern.compile("(?=(\\{player_([a-zA-Z0-9_]{3,16})\\}))");
	public static final Pattern BUYCRAFT_UNBAN_PATTERN = Pattern.compile("(?=(\\{buycraft_unban_([-0-9]+)\\}))");
	
	public static interface Replacer {
		public List<BaseComponent> replace(Matcher match,TextComponent styleCopy);
	}
	public static IChatBaseComponent replaceNames(Pattern pattern, IChatBaseComponent textComponent, Replacer replacer) {
		BaseComponent[] comps = ComponentSerializer.parse(ChatSerializer.toJSONString(textComponent));
		return ChatSerializer.fromJSON(ComponentSerializer.toString(replaceNames(pattern, comps, replacer)));
	}
	
	public static BaseComponent[] replaceNames(Pattern pattern, BaseComponent textComponent, Replacer replacer) {
		return replaceNames(pattern, new BaseComponent[]{textComponent}, replacer);
	}
	
	public static BaseComponent[] replaceNames(Pattern pattern, BaseComponent[] textComponent, Replacer replacer) {
		List<BaseComponent> out = new ArrayList<>();
		for(BaseComponent c : textComponent)
			for(BaseComponent c1 : replaceNames0(pattern, c, replacer))
				out.add(c1);
		return out.toArray(new BaseComponent[0]);
	}
	
	private static List<BaseComponent> replaceNames0(Pattern pattern, BaseComponent bcomp, Replacer replacer){
		ArrayList<BaseComponent> out = new ArrayList<>();
		if(bcomp instanceof TextComponent){
			TextComponent comp = (TextComponent) bcomp;
			
			String text = comp.getText();
			Matcher m = pattern.matcher(text);
			while (m.find()) {
				TextComponent add = new TextComponent(comp);//Copy Style
				add.setText(text.substring(0, m.start()));
				out.add(add);
				
				TextComponent stylecopy= new TextComponent(comp); //Copy Style
				List<BaseComponent> rout = replacer.replace(m, stylecopy);
				if(rout != null)
					out.addAll(rout);
				
				comp.setText(text.substring(m.start()+m.group(1).length()));
			}
			if(comp.getText().length() > 0)
				out.add(comp);
		}
		if(bcomp.getExtra() != null)
			for(BaseComponent s : bcomp.getExtra())
				out.addAll(replaceNames0(pattern,s, replacer));
		return out;
	}
	
	public static void main(String[] args) {
		IChatBaseComponent comp = ChatSerializer.fromMessage("Hello world {buycraft_unban_1}");
		comp = replaceNames(BUYCRAFT_UNBAN_PATTERN, comp, new Replacer() {
			@Override
			public List<BaseComponent> replace(Matcher match, TextComponent styleCopy) {
				styleCopy.setText("-"+match.group(2)+"-");
				return Arrays.asList(styleCopy);
			}
		});
		System.out.println(comp.getRawText()+" - "+BUYCRAFT_UNBAN_PATTERN.toString());
	}
}
