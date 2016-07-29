package dev.wolveringer.nick;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dev.wolveringer.BungeeUtil.PacketHandleEvent;
import dev.wolveringer.BungeeUtil.PacketHandler;
import dev.wolveringer.BungeeUtil.Player;
import dev.wolveringer.BungeeUtil.gameprofile.PlayerInfoData;
import dev.wolveringer.BungeeUtil.packets.Packet;
import dev.wolveringer.BungeeUtil.packets.PacketPlayInChat;
import dev.wolveringer.BungeeUtil.packets.PacketPlayOutChat;
import dev.wolveringer.BungeeUtil.packets.PacketPlayOutPlayerInfo;
import dev.wolveringer.BungeeUtil.packets.PacketPlayOutScoreboardScore;
import dev.wolveringer.BungeeUtil.packets.PacketPlayOutScoreboardTeam;
import dev.wolveringer.BungeeUtil.packets.PacketPlayOutTitle;
import dev.wolveringer.arrays.CachedArrayList;
import dev.wolveringer.arrays.CachedArrayList.UnloadListener;
import dev.wolveringer.bs.Main;
import dev.wolveringer.bs.client.event.ServerMessageEvent;
import dev.wolveringer.chat.ChatManager;
import dev.wolveringer.chat.ChatSerializer;
import dev.wolveringer.chat.IChatBaseComponent;
import dev.wolveringer.client.LoadedPlayer;
import dev.wolveringer.hashmaps.CachedHashMap;
import dev.wolveringer.hashmaps.InitHashMap;
import dev.wolveringer.permission.PermissionManager;
import dev.wolveringer.thread.ThreadFactory;
import dev.wolveringer.thread.ThreadRunner;
import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.HoverEvent.Action;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import net.md_5.bungee.event.EventHandler;

public class NickHandler implements PacketHandler<Packet>, Listener {
	/*
	private static final Pattern PATTERN = Pattern.compile("\\{player_(?=(([a-zA-Z0-9_]){3,16})\\})");

	/*
	public static interface PacketBlocker {
		public void handlePacket(PacketHandleEvent e);

		public void sendPackets();
	}

	private static abstract class AbstractPacketBlocker implements PacketBlocker {
		private HashMap<Player, ArrayList<Packet>> toSend = new InitHashMap<Player, ArrayList<Packet>>() {
			@Override
			public ArrayList<Packet> defaultValue(Player key) {
				return new ArrayList<>();
			}
		};

		public void blockPacket(PacketHandleEvent e) {
			toSend.get(e.getPlayer()).add(e.getPacket());
		}

		@Override
		public void sendPackets() {
			for (Entry<Player, ArrayList<Packet>> entry : toSend.entrySet())
				for (Packet packet : entry.getValue())
					sendPacket(entry.getKey(), packet);
		}
	}

	private abstract static class ProgressThread implements Runnable {
		protected List<PacketHandleEvent> elements = Collections.synchronizedList(new ArrayList<>());
		protected ThreadRunner runner;

		public ProgressThread() {
			runner = ThreadFactory.getFactory().createThread(this);
			runner.start();
		}

		@Override
		public void run() {
			while (true) {
				try {
					if (elements.size() > 0) {
						progressElement(elements.get(0));
						elements.remove(0);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		abstract void progressElement(PacketHandleEvent e);
	}

	private static class ChatProgressThread extends ProgressThread {
		void progressElement(PacketHandleEvent e) {
			PacketPlayOutChat chat = (PacketPlayOutChat) e.getPacket();
			boolean replace = true;
			if (!PATTERN.matcher(ChatSerializer.toJSONString(chat.getMessage())).find()) { //Check if message have a player variable (messages will send too while datenserver is disconnected and then i cant check the permission)
				replace = false;
			} else if (!Main.getDatenServer().isActive()) {
				chat.setMessage(ChatSerializer.fromMessage("§cNo message."));
				replace = false;
			}
			if (replace)
				chat.setMessage(replaceNames(chat.getMessage(), PermissionManager.getManager().hasPermission(e.getPlayer(), "chat.nick.see")));
			if (ChatManager.getInstance() != null) {
				if (!ChatManager.getInstance().handle0(e)) {
					sendPacket(e.getPlayer(), e.getPacket());
				}
			} else
				sendPacket(e.getPlayer(), e.getPacket());
		}
	}

	private static class ScoreboardTeamProgressThread extends ProgressThread {
		void progressElement(PacketHandleEvent e) {
			PacketPlayOutScoreboardTeam team = (PacketPlayOutScoreboardTeam) e.getPacket();
			if (team.getPlayers() != null)
				for (int i = 0; i < team.getPlayers().length; i++) {
					team.getPlayers()[i] = replaceNames(team.getPlayers()[i], PermissionManager.getManager().hasPermission(e.getPlayer(), "nametag.nick.see"), true);
				}
			sendPacket(e.getPlayer(), e.getPacket());
		}
	}
	
	private static class SchoreboardScoreThread extends ProgressThread {
		@Override
		void progressElement(PacketHandleEvent e) {
			PacketPlayOutScoreboardScore score = (PacketPlayOutScoreboardScore) e.getPacket();
			if (score.getScoreName() != null) {
				score.setObjektiveName(replaceNames(score.getObjektiveName(), PermissionManager.getManager().hasPermission(e.getPlayer(), "scoreboard.nick.see"), true));
			}
			sendPacket(e.getPlayer(), e.getPacket());
		}
	}

	@Getter
	@Setter
	private static NickHandler instance;

	public static CachedArrayList<Packet> whitelist = new CachedArrayList<>(1, TimeUnit.SECONDS);

	//private CachedArrayList<PacketBlocker> blocker = new CachedArrayList<>(5, TimeUnit.SECONDS);
	private CachedHashMap<Player, ProgressThread> chatThreads = new CachedHashMap<>(30, TimeUnit.SECONDS);
	private CachedHashMap<Player, ProgressThread> scoreboardTeamThread = new CachedHashMap<>(30, TimeUnit.SECONDS);
	private CachedHashMap<Player, ProgressThread> scoreboardScoreThread = new CachedHashMap<>(30, TimeUnit.SECONDS);
	
	public NickHandler() {
		chatThreads.addUnloadListener(new UnloadListener<Map.Entry<Player, ProgressThread>>() {
			@Override
			public boolean canUnload(Entry<Player, ProgressThread> element) {
				if (element.getValue().elements.size() > 0)
					return false;
				element.getValue().runner.stop();
				return true;
			}
		});
		scoreboardTeamThread.addUnloadListener(new UnloadListener<Map.Entry<Player, ProgressThread>>() {
			@Override
			public boolean canUnload(Entry<Player, ProgressThread> element) {
				if (element.getValue().elements.size() > 0)
					return false;
				element.getValue().runner.stop();
				return true;
			}
		});
		scoreboardScoreThread.addUnloadListener(new UnloadListener<Map.Entry<Player, ProgressThread>>() {
			@Override
			public boolean canUnload(Entry<Player, ProgressThread> element) {
				if (element.getValue().elements.size() > 0)
					return false;
				element.getValue().runner.stop();
				return true;
			}
		});
	}

	@Override
	public void handle(PacketHandleEvent<Packet> e) {
		if (whitelist.contains(e.getPacket()))
			return;
		long start = System.currentTimeMillis();
		if (e.getPacket() instanceof PacketPlayOutChat) {
			e.setCancelled(true);
			if (!chatThreads.containsKey(e.getPlayer()))
				chatThreads.put(e.getPlayer(), new ChatProgressThread());
			else
				chatThreads.resetTime(e.getPlayer());
			chatThreads.get(e.getPlayer()).elements.add(e);
		} else if (e.getPacket() instanceof PacketPlayInChat) {
			PacketPlayInChat chat = (PacketPlayInChat) e.getPacket();
			if (PATTERN.matcher(chat.getMessage()).find())
				e.setCancelled(!PermissionManager.getManager().hasPermission(e.getPlayer(), "chat.syntax.nick", true));
		} else if (e.getPacket() instanceof PacketPlayOutPlayerInfo) {
			for (PlayerInfoData i : ((PacketPlayOutPlayerInfo) e.getPacket()).getData()) {
				if (i.getName() != null)
					i.setName(replaceNames(i.getName(), PermissionManager.getManager().hasPermission(e.getPlayer(), "tab.nick.see")));
				if (i.getGameprofile() != null && i.getGameprofile().getName() != null) {
					i.getGameprofile().setName(max(replaceNames(i.getGameprofile().getName(), PermissionManager.getManager().hasPermission(e.getPlayer(), "nametag.nick.see"), true), 16));
				}
			}
		} else if (e.getPacket() instanceof PacketPlayOutScoreboardTeam) {
			e.setCancelled(true);
			if (!scoreboardTeamThread.containsKey(e.getPlayer()))
				scoreboardTeamThread.put(e.getPlayer(), new ScoreboardTeamProgressThread());
			else
				scoreboardTeamThread.resetTime(e.getPlayer());
			scoreboardTeamThread.get(e.getPlayer()).elements.add(e);
		} else if (e.getPacket() instanceof PacketPlayOutScoreboardScore) {
			e.setCancelled(true);
			if (!scoreboardScoreThread.containsKey(e.getPlayer()))
				scoreboardScoreThread.put(e.getPlayer(), new SchoreboardScoreThread());
			else
				scoreboardScoreThread.resetTime(e.getPlayer());
			scoreboardScoreThread.get(e.getPlayer()).elements.add(e);
		} else if (e.getPacket() instanceof PacketPlayOutTitle) {
			e.setCancelled(true);
			ThreadFactory.getFactory().createThread(() -> {
				PacketPlayOutTitle title = (PacketPlayOutTitle) e.getPacket();
				if (title.getAction() == dev.wolveringer.BungeeUtil.packets.PacketPlayOutTitle.Action.SET_SUBTITLE || title.getAction() == dev.wolveringer.BungeeUtil.packets.PacketPlayOutTitle.Action.SET_TITLE) {
					title.setTitle(replaceNames(title.getTitle(), PermissionManager.getManager().hasPermission(e.getPlayer(), "title.nick.see")));
				}
				sendPacket(e.getPlayer(), e.getPacket());
			}).start();
		}
		long end = System.currentTimeMillis();
		if (end - start > 50) {
			System.out.println("Needed more than 100ms (" + (end - start) + "ms) to replace nick in packet " + e.getPacket().getClass().getName() + " - " + e.getPlayer().getName());
		}
	}

	public static void sendPacket(Player player, Packet packet) {
		whitelist.add(packet);
		player.getInitialHandler().sendPacket(packet);
	}

	private static String max(String in, int length) {
		return in.length() > length ? in.substring(0, length) : in;
	}

	private static String replaceNames(String str, boolean info) {
		return replaceNames(str, info, false);
	}

	private static String replaceNames(String str, boolean info, boolean warning) {
		long start = System.currentTimeMillis();
		Matcher m = PATTERN.matcher(str);
		while (m.find()) {
			LoadedPlayer player = Main.getDatenServer().getClient().getPlayerAndLoad(m.group(1));
			str = str.replace("{player_" + m.group(1) + "}", player.hasNickname() ? info ? player.getName() : player.getNickname() : player.getName());
		}
		long end = System.currentTimeMillis();
		if (warning && str.length() > 16) {
			System.out.println("Replaced String is longer than 16 Characters (" + str + ")");
		}
		if (end - start > 50) {
			System.out.println("Replace a string needs more than 50ms (" + (end - start) + "ms). String: " + str);
		}
		return str;
	}

	private static IChatBaseComponent replaceNames(IChatBaseComponent textComponent, boolean info) {
		List<BaseComponent> out = new ArrayList<>();
		BaseComponent[] comps = ComponentSerializer.parse(ChatSerializer.toJSONString(textComponent));
		for (BaseComponent c : comps)
			for (BaseComponent c1 : replaceNames(c, info))
				out.add(c1);
		return ChatSerializer.fromJSON(ComponentSerializer.toString(out.toArray(new BaseComponent[0])));
	}

	private static List<BaseComponent> replaceNames(BaseComponent bcomp, boolean info) {
		ArrayList<BaseComponent> out = new ArrayList<>();
		if (bcomp instanceof TextComponent) {
			TextComponent comp = (TextComponent) bcomp;

			String text = comp.getText();
			Matcher m = PATTERN.matcher(text);
			while (m.find()) {
				TextComponent add = new TextComponent(comp);//Copy Style
				add.setText(text.substring(0, m.start()));
				out.add(add);

				LoadedPlayer player = Main.getDatenServer().getClient().getPlayerAndLoad(m.group(1));

				TextComponent nickname = new TextComponent(comp); //Copy Style
				nickname.setText(!player.hasNickname() ? m.group(1) : info ? player.getName() : player.getNickname());
				if (player.hasNickname() && info) {
					nickname.setItalic(true);
					nickname.setHoverEvent(new HoverEvent(Action.SHOW_TEXT, new ComponentBuilder("§aDer Spieler §e" + player.getName() + " §aist genickt als §b" + player.getNickname()).create()));
				}
				out.add(nickname);

				comp.setText(text.substring(m.start() + ("{player_" + m.group(1) + "}").length()));
			}
			if (comp.getText().length() > 0)
				out.add(comp);
		}
		if (bcomp.getExtra() != null)
			for (BaseComponent s : bcomp.getExtra())
				out.addAll(replaceNames(s, info));
		return out;
	}

	@EventHandler
	public void a(ServerMessageEvent e) {
		if (e.getChannel().equalsIgnoreCase("bnick")) {
			String name = e.getBuffer().readString();
			LoadedPlayer player = Main.getDatenServer().getClient().getPlayer(name);
			if (player != null) {
				System.out.println("§5Reloading nick for " + name);
				Main.getDatenServer().getClient().clearCacheForPlayer(player);
				Main.getDatenServer().getClient().getPlayerAndLoad(name);
			}
		}
	}
	
	*/
	private static final Pattern PATTERN = Pattern.compile("\\{player_(?=(([a-zA-Z0-9_]){3,30})\\})");
	
	@Getter
	@Setter
	private static NickHandler instance;
	private CachedArrayList<Packet> whitelist = new CachedArrayList<>(1, TimeUnit.SECONDS);
			
	@Override
	public void handle(PacketHandleEvent<Packet> e) {
		if(whitelist.contains(e.getPacket()))
			return;
		if (e.getPacket() instanceof PacketPlayOutChat) {
			PacketPlayOutChat chat = (PacketPlayOutChat) e.getPacket();
			boolean replace = true;
			if(!PATTERN.matcher(ChatSerializer.toJSONString(chat.getMessage())).find()){ //Check if message have a player variable (messages will send too while datenserver is disconnected and then i cant check the permission)
				replace = false;
			}
			else if(!Main.getDatenServer().isActive()){
				chat.setMessage(ChatSerializer.fromMessage("§cNo message."));
				replace = false;
			}
			if(replace)
				chat.setMessage(replaceNames(chat.getMessage(), PermissionManager.getManager().hasPermission(e.getPlayer(), "chat.nick.see")));
			e.setCancelled(ChatManager.getInstance().handle0(e));
		}
		else if(e.getPacket() instanceof PacketPlayInChat){
			PacketPlayInChat chat = (PacketPlayInChat) e.getPacket();
			if(PATTERN.matcher(chat.getMessage()).find())
				e.setCancelled(!PermissionManager.getManager().hasPermission(e.getPlayer(), "chat.syntax.nick", true));
		}
		else if(e.getPacket() instanceof PacketPlayOutPlayerInfo){
			//if(((PacketPlayOutPlayerInfo)e.getPacket()).getAction() == EnumPlayerInfoAction.ADD_PLAYER || ((PacketPlayOutPlayerInfo)e.getPacket()).getAction() == EnumPlayerInfoAction.UPDATE_DISPLAY_NAME)
			for(PlayerInfoData i : ((PacketPlayOutPlayerInfo)e.getPacket()).getData()){
				if(i.getName() != null)
					i.setName(replaceNames(i.getName(), PermissionManager.getManager().hasPermission(e.getPlayer(), "tab.nick.see")));
				if(i.getGameprofile() != null && i.getGameprofile().getName() != null){
					i.getGameprofile().setName(replaceNames(i.getGameprofile().getName(), PermissionManager.getManager().hasPermission(e.getPlayer(), "nametag.nick.see"), 16));
				}
			}
		}
		else if(e.getPacket() instanceof PacketPlayOutScoreboardTeam){
			PacketPlayOutScoreboardTeam team = (PacketPlayOutScoreboardTeam) e.getPacket();
			if(team.getPlayers() != null){
				boolean sync = true;
				for(String player : team.getPlayers()){
					if(!Main.getDatenServer().getClient().getPlayer(player).isLoaded())
						sync = false;
				}
				if(sync){
					for(int i = 0;i<team.getPlayers().length;i++){
						team.getPlayers()[i] = replaceNames(team.getPlayers()[i], PermissionManager.getManager().hasPermission(e.getPlayer(), "nametag.nick.see"), 40);
					}
				}
				else
				{
					e.setCancelled(true);
					ThreadFactory.getFactory().createThread(()->{
						for(int i = 0;i<team.getPlayers().length;i++){
							team.getPlayers()[i] = replaceNames(team.getPlayers()[i], PermissionManager.getManager().hasPermission(e.getPlayer(), "nametag.nick.see"), 40);
						}
						sendPacket(e.getPlayer(), team);
					}).start();
				}
			}
		}
		else if(e.getPacket() instanceof PacketPlayOutScoreboardScore){
			PacketPlayOutScoreboardScore score = (PacketPlayOutScoreboardScore) e.getPacket();
			if(score.getScoreName() != null){
				score.setObjektiveName(replaceNames(score.getObjektiveName(), PermissionManager.getManager().hasPermission(e.getPlayer(), "scoreboard.nick.see"), 40));
			}
		}
		else if(e.getPacket() instanceof PacketPlayOutTitle){
			PacketPlayOutTitle title = (PacketPlayOutTitle) e.getPacket();
			if(title.getAction() == dev.wolveringer.BungeeUtil.packets.PacketPlayOutTitle.Action.SET_SUBTITLE || title.getAction() == dev.wolveringer.BungeeUtil.packets.PacketPlayOutTitle.Action.SET_TITLE){
				title.setTitle(replaceNames(title.getTitle(), PermissionManager.getManager().hasPermission(e.getPlayer(), "title.nick.see")));
			}
		}
	}
	
	public void sendPacket(Player player, Packet packet) {
		whitelist.add(packet);
		player.getInitialHandler().sendPacket(packet);
	}

	
	private static String replaceNames(String str,boolean info){
		return replaceNames(str, info, -1);
	}
	
	private static String replaceNames(String str,boolean info, int maxlength){
		Matcher m = PATTERN.matcher(str);
		while (m.find()) {
			LoadedPlayer player = Main.getDatenServer().getClient().getPlayer(m.group(1));
			if(player.isLoaded())
				str = str.replace("{player_"+m.group(1)+"}", player.hasNickname() ? info ? player.getName() : player.getNickname() : player.getName());
			else{
				System.err.println("Cant replace nickname (Player not loaded in replaceNames(String,boolean,int), Player: "+m.group(1)+")");
				str = str.replace("{player_"+m.group(1)+"}", m.group(1));
			}
		}
		if(maxlength != -1 && str.length()> maxlength)
			str = str.substring(0, maxlength);
		return str;
	}

	private static IChatBaseComponent replaceNames(IChatBaseComponent textComponent, boolean info) {
		List<BaseComponent> out = new ArrayList<>();
		BaseComponent[] comps = ComponentSerializer.parse(ChatSerializer.toJSONString(textComponent));
		for(BaseComponent c : comps)
			for(BaseComponent c1 : replaceNames(c, info))
				out.add(c1);
		return ChatSerializer.fromJSON(ComponentSerializer.toString(out.toArray(new BaseComponent[0])));
	}
	
	private static List<BaseComponent> replaceNames(BaseComponent bcomp, boolean info){
		ArrayList<BaseComponent> out = new ArrayList<>();
		if(bcomp instanceof TextComponent){
			TextComponent comp = (TextComponent) bcomp;
			
			String text = comp.getText();
			Matcher m = PATTERN.matcher(text);
			while (m.find()) {
				TextComponent add = new TextComponent(comp);//Copy Style
				add.setText(text.substring(0, m.start()));
				out.add(add);
				
				LoadedPlayer player = Main.getDatenServer().getClient().getPlayer(m.group(1));
				
				TextComponent nickname = new TextComponent(comp); //Copy Style
				if(player.isLoaded())
					nickname.setText(!player.hasNickname() ? m.group(1) : info ? player.getName() : player.getNickname());
				else{
					nickname.setText(m.group(1));
					System.err.println("Cant replace nickname (Player not loaded in replaceNames(BaseComponent,boolean), Player: "+m.group(1)+")");
				}
				if(player.isLoaded() && player.hasNickname() && info){
					nickname.setItalic(true);
					nickname.setHoverEvent(new HoverEvent(Action.SHOW_TEXT, new ComponentBuilder("§aDer Spieler §e"+player.getName()+" §aist genickt als §b"+player.getNickname()).create()));
				}
				out.add(nickname);
				
				comp.setText(text.substring(m.start()+("{player_"+m.group(1)+"}").length()));
			}
			if(comp.getText().length() > 0)
				out.add(comp);
		}
		if(bcomp.getExtra() != null)
			for(BaseComponent s : bcomp.getExtra())
				out.addAll(replaceNames(s, info));
		return out;
	}
	
	@EventHandler
	public void a(ServerMessageEvent e) {
		if (e.getChannel().equalsIgnoreCase("bnick")) {
			String name = e.getBuffer().readString();
			LoadedPlayer player = Main.getDatenServer().getClient().getPlayer(name);
			if (player != null) {
				System.out.println("§5Reloading nick for " + name);
				Main.getDatenServer().getClient().clearCacheForPlayer(player);
				Main.getDatenServer().getClient().getPlayerAndLoad(name);
			}
		}
	}
}
