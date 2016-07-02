package dev.wolveringer.report.info;

import dev.wolveringer.BungeeUtil.Player;
import dev.wolveringer.ban.BannedServerManager;
import dev.wolveringer.booster.BoosterManager;
import dev.wolveringer.booster.BoosterType;
import dev.wolveringer.bs.Main;
import dev.wolveringer.client.threadfactory.ThreadFactory;
import dev.wolveringer.client.threadfactory.ThreadRunner;
import dev.wolveringer.dataserver.protocoll.packets.PacketReportRequest.RequestType;
import dev.wolveringer.permission.PermissionManager;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.protocol.packet.Chat;

public class ActionBarInformation {
	private ThreadRunner thread;
	private ThreadRunner updater;
	private int intervall;
	private int updateIntervall;
	private boolean active = false;
	
	private int openReports = 0;
	
	public ActionBarInformation(int intervall, int updateIntervall) {
		this.intervall = intervall;
		this.updateIntervall = updateIntervall;
		thread = ThreadFactory.getFactory().createThread(()->{
			while (active) {
				try {
					Thread.sleep(intervall);
				} catch (Exception e) {
				}
				try {
					sendBar();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		updater = ThreadFactory.getFactory().createThread(()->{
			while (active) {
				try {
					Thread.sleep(updateIntervall);
				} catch (Exception e) {
				}
				try {
					if(Main.getDatenServer().isActive())
						openReports = Main.getDatenServer().getClient().getReportEntity(RequestType.OPEN_REPORTS, -1).getSync().length;
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		});
	}
	
	public void start(){
		active = true;
		thread.start();
		updater.start();
	}
	
	private void sendBar(){
		if(!Main.getDatenServer().isActive())
			return;
		String message = "§c§lEs gibt momentan "+buildReportColor()+"§l"+openReports+" §c§loffene Reports!";
		for(ProxiedPlayer p : BungeeCord.getInstance().getPlayers())
			if(p != null && !BannedServerManager.getInstance().isBanned((Player) p))
				if(PermissionManager.getManager().hasPermission(p, "bungee.report.info") && openReports != 0){
					p.unsafe().sendPacket(new Chat("{\"text\": \"" + message + "\"}", (byte) 2));
				}
				else
				{
					if(Main.getBoosterManager().getBooster(BoosterType.ARCADE).isActive())
						if(p.getServer().getInfo().getName().startsWith("a")){
							String m = "§a§lDouble-Coin Booster wurde aktiviert von §e§l"+Main.getDatenServer().getClient().getPlayerAndLoad(Main.getBoosterManager().getBooster(BoosterType.ARCADE).getPlayer()).getName();
							p.unsafe().sendPacket(new Chat("{\"text\": \"" + m + "\"}", (byte) 2));
						}
					if(Main.getBoosterManager().getBooster(BoosterType.SKY).isActive())
						if(p.getServer().getInfo().getName().equalsIgnoreCase("sky")){
							String m = "§a§lFarm Booster wurde aktiviert von §e§l"+Main.getDatenServer().getClient().getPlayerAndLoad(Main.getBoosterManager().getBooster(BoosterType.ARCADE).getPlayer()).getName();
							p.unsafe().sendPacket(new Chat("{\"text\": \"" + m + "\"}", (byte) 2));
						}
				}
		
	
	}
	private String buildReportColor(){
		if(openReports < 5)
			return "§a";
		else if(openReports < 15)
			return "§e";
		else if(openReports < 25)
			return "§6";
		else 
			return "§4";
	}
}
//http://hastebin.com/qusoqofepu.avrasm
