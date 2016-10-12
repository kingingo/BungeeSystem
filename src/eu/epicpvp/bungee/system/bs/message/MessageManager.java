package eu.epicpvp.bungee.system.bs.message;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import eu.epicpvp.bungee.system.bs.Main;
import eu.epicpvp.bungee.system.mysql.MySQL;
import eu.epicpvp.datenserver.definitions.booster.BoosterType;
import eu.epicpvp.datenserver.definitions.connection.ClientType;
import eu.epicpvp.datenserver.definitions.dataserver.player.LanguageType;
import eu.epicpvp.datenserver.definitions.dataserver.protocoll.DataBuffer;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.Title;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.scheduler.ScheduledTask;

public class MessageManager implements Listener {
	private static ScheduledTask task;

	public static void start() {
		for (LanguageType t : LanguageType.values()) getManager(t);

		task = BungeeCord.getInstance().getScheduler().runAsync(Main.getInstance(), new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
				}
				while (task != null) {
					nextBroadcastMessage();
					try {
						Thread.sleep(5 * 60 * 1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		});

		BungeeCord.getInstance().getPluginManager().registerListener(Main.getInstance(), new MessageListener());
	}

	public static void stop() {
		task.cancel();
		task = null;
	}

	protected static HashMap<LanguageType, MessageManager> messageManagers = new HashMap<>();

	public static MessageManager getManager(LanguageType lang) {
		if (!messageManagers.containsKey(lang))
			messageManagers.put(lang, new MessageManager(lang));
		return messageManagers.get(lang);
	}

	public static void nextBroadcastMessage() {
		for (MessageManager m : messageManagers.values())
			m.broadcastNext();
	}

	private ArrayList<String> titles = new ArrayList<>();
	private ArrayList<String> messages = new ArrayList<>();
	private Iterator<String> loopMessages;
	private LanguageType lang;

	public MessageManager(LanguageType lang) {
		this.lang = lang;
		updateMessages();
	}

	public void updateMessages() {
		titles.clear();
		messages.clear();
		MySQL.getInstance().commandSync(
				"CREATE TABLE IF NOT EXISTS `BG_News` ( `motd` TEXT NOT NULL , `language` TEXT NOT NULL , `type` TEXT NOT NULL ) ENGINE = InnoDB;");
		ArrayList<String[]> out = MySQL.getInstance()
				.querySync("SELECT `type`,`motd` FROM `BG_News` WHERE `language`='" + lang + "'", -1);
		for (String[] var : out) {
			if (var[0].equalsIgnoreCase("BROADCAST")) {
				messages.add(var[1]);
			} else
				titles.add(var[1]);
		}
	}

	public void addMessage(String message, String type) {
		MySQL.getInstance().commandSync("INSERT INTO `BG_News`(`motd`, `language`, `type`) VALUES ('" + message + "','"
				+ lang + "','" + type + "')");
		if (type.equalsIgnoreCase("BROADCAST"))
			messages.add(message);
		else
			titles.add(message);
		Main.getDatenServer().getClient().sendServerMessage(ClientType.BUNGEECORD, "news",
				new DataBuffer().writeByte(0)); // Update Messages
	}

	public String removeMessage(int id, String type) {
		String message;
		if (type.equalsIgnoreCase("BROADCAST")) {
			message = messages.get(id);
			messages.remove(message);
		} else {
			message = titles.get(id);
			titles.remove(message);
		}
		MySQL.getInstance()
				.commandSync("DELETE FROM `BG_News` WHERE `motd`='" + message + "' AND `type`='" + type + "'");
		Main.getDatenServer().getClient().sendServerMessage(ClientType.BUNGEECORD, "news",
				new DataBuffer().writeByte(0)); // Update Messages
		return message;
	}

	public ArrayList<String> getTitles() {
		return titles;
	}

	public ArrayList<String> getMessages() {
		return messages;
	}

	public void broadcastNext() {
		if (loopMessages == null || !loopMessages.hasNext())
			loopMessages = new ArrayList(messages).iterator();
		if (loopMessages.hasNext()) {
			String message = ChatColor.translateAlternateColorCodes('&', loopMessages.next());
			for (ProxiedPlayer player : BungeeCord.getInstance().getPlayers())
				if (Main.getTranslationManager().getLanguage(player) == lang)
					player.sendMessage(message);
			System.out.println("[" + lang + "] Brotcast: " + message);
		}
	}

	public void playTitles(ProxiedPlayer player) {
		BungeeCord.getInstance().getScheduler().runAsync(Main.getInstance(), new Runnable() {
			@Override
			public void run() {
				/*
				 * 	if(Main.getBoosterManager().getBooster(BoosterType.ARCADE).isActive())
						if(p.getServer().getInfo().getName().startsWith("a")){
							String m = "§a§lDouble-Coin Booster wurde aktiviert von §e§l"+Main.getDatenServer().getClient().getPlayerAndLoad(Main.getBoosterManager().getBooster(BoosterType.ARCADE).getPlayer()).getName();
							p.unsafe().sendPacket(new Chat("{\"text\": \"" + m + "\"}", (byte) 2));
						}
					if(Main.getBoosterManager().getBooster(BoosterType.SKY).isActive())
						if(p.getServer().getInfo().getName().equalsIgnoreCase("sky")){
							String m = "§a§lFarm Booster wurde aktiviert von §e§l"+Main.getDatenServer().getClient().getPlayerAndLoad(Main.getBoosterManager().getBooster(BoosterType.ARCADE).getPlayer()).getName();
							p.unsafe().sendPacket(new Chat("{\"text\": \"" + m + "\"}", (byte) 2));
						}
				 */

				ArrayList<String> titles = new ArrayList<>(MessageManager.this.titles);
				if(Main.getDatenServer().isActive()){
					if(Main.getBoosterManager().getBooster(BoosterType.ARCADE).isActive())
						titles.add("§aArcade-Booster activiert von §e"+Main.getDatenServer().getClient().getPlayerAndLoad(Main.getBoosterManager().getBooster(BoosterType.ARCADE).getPlayer()).getName());
					if(Main.getBoosterManager().getBooster(BoosterType.SKY).isActive())
						titles.add("§aSky-Booster activiert von §e"+Main.getDatenServer().getClient().getPlayerAndLoad(Main.getBoosterManager().getBooster(BoosterType.ARCADE).getPlayer()).getName());
				}
				Iterator<String> left = titles.iterator();
				while (left.hasNext()) {
					String next = left.next();
					Title title = BungeeCord.getInstance().createTitle();
					title.fadeIn(0);
					title.fadeOut(50);
					title.stay(30);
					title.title(TextComponent.fromLegacyText("§6§lCLASHMC.EU"));
					title.subTitle(TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', next)));
					title.send(player);
					try {
						Thread.sleep(2 * 1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}

			}
		});
	}
}
