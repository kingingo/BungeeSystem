package eu.epicpvp.bungee.system.bs.listener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import eu.epicpvp.bungee.system.bs.Main;
import eu.epicpvp.bungee.system.bs.information.InformationManager;
import eu.epicpvp.thread.ThreadFactory;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.ServerPing.Players;
import net.md_5.bungee.api.event.ProxyPingEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class PingListener implements Listener {

	private static final Pattern ELEMENT_PATTERN = Pattern.compile("\\[\\?([A-Za-z0-9]*) (.+)\\]");

	private String motd = "§aDefault MOTD!";

	public PingListener() {
		ThreadFactory.getFactory().createThread(() -> {
			while (BungeeCord.getInstance().isRunning) {
				motd = formatMOTD(InformationManager.getManager().getInfo("motd1")) + "\n" + formatMOTD(InformationManager.getManager().getInfo("motd2"));
				try {
					Thread.sleep(1000);
				} catch (Exception ignored) {
				}
			}
		}).start();
		;
	}

	@EventHandler
	public void a(ProxyPingEvent e) {
		if (InformationManager.getManager() == null) {
			return;
		}
		String[] extraData = e.getConnection().getVirtualHost() != null && e.getConnection().getVirtualHost().getHostName() != null ? e.getConnection().getVirtualHost().getHostName().split("-", 2) : new String[0];
		if (extraData.length > 1) {
			if (extraData[1].equalsIgnoreCase("bungeeOnly")) {
				e.getResponse().setDescription(motd);
				return;
			}
		}
		Players player = new Players(Integer.parseInt(InformationManager.getManager().getInfo("maxPlayers")), Main.getDatenServer().getPlayerCount(), new ServerPing.PlayerInfo[0]);
		e.getResponse().setPlayers(player);
		e.getResponse().setDescription(motd);
	}

	private static String formatMOTD(String in) {
		Matcher m = ELEMENT_PATTERN.matcher(in);
		while (m.find()) {
			HashMap<String, String> parms = paradiseParms(m.group(2));
			String out = null;
			switch (m.group(1)) {
				case "date":
					out = replaceDate(m.group(1), parms);
					break;
				default:
					out = "§f<§cCant find " + m.group(1) + "§f>";
					break;
			}
			in = in.replace(m.group(), out);
		}
		return ChatColor.translateAlternateColorCodes('&', in);
	}

	private static HashMap<String, String> paradiseParms(String in) {
		HashMap<String, String> parms = new HashMap<>();

		int currentIndex = 0;
		int lastIndex = 0;
		String parmKey = null;
		while (lastIndex != -1) {
			currentIndex = in.indexOf('=', lastIndex);
			String key = in.substring(lastIndex, currentIndex == -1 ? in.length() : currentIndex);
			if (parmKey != null) {
				String kkey = parmKey;
				if (kkey.lastIndexOf(' ') != -1)
					kkey = kkey.substring(kkey.lastIndexOf(' ') + 1);
				String val = key;
				if (currentIndex != -1) {
					val = val.substring(0, val.lastIndexOf(' '));
				}
				if (val.startsWith("'") && val.endsWith("'"))
					val = val.substring(1, val.length() - 1);
				parms.put(kkey, val);
			}
			parmKey = key;
			if (currentIndex != -1)
				lastIndex = currentIndex + 1;
			else
				lastIndex = -1;
		}

		return parms;
	}

	private static EnumMap<TimeUnit, String> defaultMapping = new EnumMap<>(TimeUnit.class);

	{
		defaultMapping.put(TimeUnit.DAYS, "Tage");
		defaultMapping.put(TimeUnit.HOURS, "Stunden");
		defaultMapping.put(TimeUnit.MINUTES, "Minuten");
		defaultMapping.put(TimeUnit.SECONDS, "Sekunden");
	}

	private static String replaceDate(String key, HashMap<String, String> parms) {
		SimpleDateFormat format = new SimpleDateFormat(parms.getOrDefault("targetFormat", "dd.MM.yyyy'-'HH:mm:ss"));
		if (!parms.containsKey("target"))
			throw new RuntimeException("Cant find target date!");
		Date targetDate;
		Date currentDate = new Date(System.currentTimeMillis());
		try {
			targetDate = format.parse(parms.get("target"));
		} catch (ParseException e) {
			throw new RuntimeException("Invalid target date!");
		}
		if (targetDate.before(currentDate))
			return parms.getOrDefault("end", "now");

		long time = targetDate.getTime() - currentDate.getTime();

		EnumMap<TimeUnit, String> mapping = new EnumMap<>(TimeUnit.class);
		mapping.putAll(defaultMapping);

		for (TimeUnit u : TimeUnit.values())
			if (parms.containsKey(u.toString().toLowerCase()))
				mapping.put(u, parms.get(u.toString().toLowerCase()));

		EnumMap<TimeUnit, String> pmapping = new EnumMap<>(TimeUnit.class);

		for (TimeUnit u : TimeUnit.values())
			if (parms.containsKey(u.toString().toLowerCase() + "_p"))
				pmapping.put(u, parms.get(u.toString().toLowerCase() + "_p"));

		return PlayerJoinListener.getDurationBreakdown(time, "§cerror", pmapping, mapping);
	}

	public static void main(String[] args) {
		long start = System.currentTimeMillis();
		System.out.println("MOTD: " + formatMOTD("Starting in: [?date target=10.09.2016-14:55:00 days=Tag hours=Stunde minutes=Minute seconds=Sekunde minutes_p=n]"));
		System.out.println(System.currentTimeMillis() - start);
	}
}
