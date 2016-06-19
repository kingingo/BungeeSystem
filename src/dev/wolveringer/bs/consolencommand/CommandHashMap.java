package dev.wolveringer.bs.consolencommand;

import dev.wolveringer.hashmaps.InitHashMap;
import dev.wolveringer.util.apache.StringUtils;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;

public class CommandHashMap extends InitHashMap<String, Command>{
	private String firstPart = null;
	
	@Override
	public Command defaultValue(String key) {
		if(!PrefixCommandRegistry.getInstance().canHandle(key))
			return null;
		firstPart = key;
		return new Command("") {
			@Override
			public void execute(CommandSender sender, String[] args) {
				if(PrefixCommandRegistry.getInstance() != null)
					PrefixCommandRegistry.getInstance().dispatchLine(firstPart+(args.length > 0 ? " "+StringUtils.join(args," ") : ""));
				remove(this);
			}
		};
	}
}
