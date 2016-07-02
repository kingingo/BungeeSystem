package dev.wolveringer.ban;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import dev.wolveringer.BungeeUtil.Player;
import dev.wolveringer.bs.Main;
import dev.wolveringer.bs.listener.PlayerJoinListener;
import dev.wolveringer.bs.message.MessageManager;
import dev.wolveringer.bs.servermanager.ServerManager;
import dev.wolveringer.client.threadfactory.ThreadFactory;
import dev.wolveringer.dataserver.ban.BanEntity;
import dev.wolveringer.dataserver.player.LanguageType;
import dev.wolveringer.server.CostumServer;
import dev.wolveringer.server.ServerConfiguration;
import dev.wolveringer.server.ServerConfiguration.ServerConfigurationBuilder;
import dev.wolveringer.server.world.World;
import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.event.ServerConnectEvent;

public class BannedServerManager {
	
	static {
		Main.getTranslationManager().registerFallback(LanguageType.ENGLISH, "server.banned.title", "§cYou are banned!");
		Main.getTranslationManager().registerFallback(LanguageType.ENGLISH, "server.banned.subtitle.permanent", "§cYou are permanently banned level %s0!");
		Main.getTranslationManager().registerFallback(LanguageType.ENGLISH, "server.banned.actionbar.permanent", "§a§lYour ban reason: §c§l%s0");
		Main.getTranslationManager().registerFallback(LanguageType.ENGLISH, "server.banned.tab.permanent.header", "§cYou are permanently banned level %s0!");
		Main.getTranslationManager().registerFallback(LanguageType.ENGLISH, "server.banned.tab.permanent.footer", "§aYour ban reason: §c%s0");
		
		Main.getTranslationManager().registerFallback(LanguageType.ENGLISH, "server.banned.chat", "\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n§eDu wurdest vom dem §6ClashMC §eNetzwerk %s3 wegen\n"
				+ "§b%s0§e von§b %s1 §eauf der Stufe§c %s2 §egebannt.\n"
				+ "§eFalls du wieder spielen möchtest, dann kannst\n"
				+ "§edu dir eine Entsperrung in unserem Onlineshop\n"
				+ "§eunter§b shop.ClashMC.de§e kaufen.\n"
				+ "§a▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬\n"
				+ "§eFalls du Fragen zu deinem Ban hast,\n"
				+ "§emelde dich bitte auf unserem Teamspeak 3 Server unter§7\n"
				+ "§bts.ClashMC.de§7§e oder schreibe ein Support-Ticket\n"
				+ "§eals Entbannungsantrag unter §bwww.ClashMC.de");
																			//Ja/Nein
		Main.getTranslationManager().registerFallback(LanguageType.ENGLISH, "server.banned.subtitle.temporary", "§cYou are temporary banned for %s0!");
		Main.getTranslationManager().registerFallback(LanguageType.ENGLISH, "server.banned.actionbar.temporary", "§a§lYour ban reason: §c§l%s0");
		Main.getTranslationManager().registerFallback(LanguageType.ENGLISH, "server.banned.tab.temporary.header", "§cYou are permanently banned for %s0!");
		Main.getTranslationManager().registerFallback(LanguageType.ENGLISH, "server.banned.tab.temporary.footer", "§aYour ban reason: §c%s0");
	}

	@Setter
	@Getter
	private static BannedServerManager instance;
	
	private final World world;
	private final int render;
	private HashMap<Player, CostumServer> server = new HashMap<>();
	private ArrayList<Runnable> updates = new ArrayList<>();
	
	public BannedServerManager(World world, int render) {
		this.world = world;
		this.render = render;
		ThreadFactory.getFactory().createThread(()->{
			while (true) {
				try {
					Thread.sleep(1000);
				} catch (Exception e) {
					e.printStackTrace();
				}
				for(Runnable r : new ArrayList<>(updates))
					try{
						r.run();
					}catch(Exception e){
						e.printStackTrace();
					}
			}
		}).start();
	}
	
	public void joinServer(Player player,BanEntity ban){
		ServerConfigurationBuilder cfgBuilder = ServerConfiguration.builder();
		cfgBuilder.title(Main.getTranslationManager().translate("server.banned.title",player));
		if(!ban.isTempBanned()){
			cfgBuilder.subTitle(Main.getTranslationManager().translate("server.banned.subtitle.permanent",player,ban.getLevel()));
			cfgBuilder.actionBar(Main.getTranslationManager().translate("server.banned.actionbar.permanent",player,ban.getReson()));
			cfgBuilder.tab(Arrays.asList(Main.getTranslationManager().translate("server.banned.tab.permanent.header",player,ban.getLevel()),Main.getTranslationManager().translate("server.banned.tab.permanent.footer",player,ban.getReson())));
		}
		else
		{
			cfgBuilder.subTitle(Main.getTranslationManager().translate("server.banned.subtitle.temporary",player,PlayerJoinListener.getDurationBreakdown(ban.getEnd()-System.currentTimeMillis()),"now"));
			cfgBuilder.actionBar(Main.getTranslationManager().translate("server.banned.actionbar.temporary",player,ban.getReson()));
			cfgBuilder.tab(Arrays.asList(Main.getTranslationManager().translate("server.banned.tab.temporary.header",player,PlayerJoinListener.getDurationBreakdown(ban.getEnd()-System.currentTimeMillis())),Main.getTranslationManager().translate("server.banned.tab.temporary.footer",player,ban.getReson())));
		}
		cfgBuilder.chat(Arrays.asList("","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","",""));
		cfgBuilder.renderDistance(render);
		cfgBuilder.world(world);
		server.put(player, CostumServer.createServer(Main.getInstance(), player, cfgBuilder.build()));
		updates.add(new Runnable() {
			int count = 0;
			@Override
			public void run() {
				if(!player.isConnected()){
					server.remove(player);
					updates.remove(this);
					return;
				}
				if(!ban.isActive() || !isBanned(player)){
					unban(player);
					updates.remove(this);
					return;
				}
				if(ban.isTempBanned()){
					CostumServer cserver = server.get(player);
					if(cserver != null){
						cserver.getConfig().setSubTitle(Main.getTranslationManager().translate("server.banned.subtitle.temporary",player,PlayerJoinListener.getDurationBreakdown(ban.getEnd()-System.currentTimeMillis(),"now")));
						cserver.getConfig().setActionBar(Main.getTranslationManager().translate("server.banned.actionbar.temporary",player,ban.getReson()));
						cserver.getConfig().setTab(Arrays.asList(Main.getTranslationManager().translate("server.banned.tab.temporary.header",player,PlayerJoinListener.getDurationBreakdown(ban.getEnd()-System.currentTimeMillis())),Main.getTranslationManager().translate("server.banned.tab.temporary.footer",player,ban.getReson())));
						cserver.getConnection().updateActrionBar();
						cserver.getConnection().updateTab();
						cserver.getConnection().updateTitle();
					}
				}
				if(count % 9 == 0){
					String reason = ban.getReson();
					String banner = ban.getBanner();
					String level = String.valueOf(ban.getLevel());
					String temp = ban.isTempBanned() ? "temporary" : "permanent";
					player.sendMessage(Main.getTranslationManager().translate("server.banned.chat",reason,banner,level,temp));
				}
				count++;
			}
		});
	}
	
	public void playerQuit(Player player){
		if(isBanned(player))
			server.remove(player);
	}
	
	public boolean isBanned(Player player){
		return server.get(player) != null;
	}

	public void unban(Player player) {
		if(server.get(player) == null)
			return;
		server.get(player).switchTo(BungeeCord.getInstance().getPluginManager().callEvent(new ServerConnectEvent(player, ServerManager.DEFAULT_HUB)).getTarget());
		MessageManager.getmanager(Main.getTranslationManager().getLanguage(player)).playTitles(player);
		server.remove(player);
	}
}
