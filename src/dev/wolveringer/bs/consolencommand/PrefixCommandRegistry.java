package dev.wolveringer.bs.consolencommand;

import java.util.HashMap;
import java.util.Map.Entry;

import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.BungeeCord;

public class PrefixCommandRegistry {
	@Getter
	@Setter
	private static PrefixCommandRegistry instance;
	private HashMap<String, CommandExecutor> commands = new HashMap<>();

	protected void dispatchLine(String line) {
		int charMatch = 0;
		CommandExecutor exec = null;
		for (Entry<String, CommandExecutor> c : commands.entrySet())
			if (line.startsWith(c.getKey()))
				if (charMatch < c.getKey().length()) {
					exec = c.getValue();
					charMatch = c.getKey().length();
				}
		if (exec != null)
			exec.onCommand(BungeeCord.getInstance().getConsole(), line);
	}

	protected boolean canHandle(String line){
		int charMatch = 0;
		CommandExecutor exec = null;
		for (Entry<String, CommandExecutor> c : commands.entrySet())
			if (line.startsWith(c.getKey()))
				if (charMatch < c.getKey().length()) {
					exec = c.getValue();
					charMatch = c.getKey().length();
				}
		return exec != null;
	}
	
	public void registerCommandListener(String prefix, CommandExecutor listener) {
		commands.put(prefix, listener);
	}

	public void unregisterCommandListener(CommandExecutor listener) {
		for (Entry<String, CommandExecutor> c : new HashMap<>(commands).entrySet())
			if (c.getValue().equals(listener))
				commands.remove(c.getKey());
	}
}
