package eu.epicpvp.bungee.system.report.commands;

import java.util.Random;

import dev.wolveringer.BungeeUtil.Player;
import eu.epicpvp.bungee.system.bs.Main;
import eu.epicpvp.datenclient.client.LoadedPlayer;
import dev.wolveringer.dataserver.protocoll.packets.PacketReportRequest.RequestType;
import eu.epicpvp.bungee.system.permission.PermissionManager;
import dev.wolveringer.report.ReportEntity;
import dev.wolveringer.report.ReportState;
import eu.epicpvp.bungee.system.report.gui.GuiPlayerMenue;
import dev.wolveringer.thread.ThreadFactory;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;

public class CMD_Report extends Command{
	public CMD_Report() {
		super("report");
	}

	@Override
	public void execute(CommandSender cs, String[] args) {
		if(!(cs instanceof Player)){
			cs.sendMessage("§cYou arent an instance of a Player!");
			return;
		}



		if(args.length == 1 && args[0].equalsIgnoreCase("closeAll") && PermissionManager.getManager().hasPermission(cs, "report.closeall")){
			cs.sendMessage("§aClosing....");
			ThreadFactory.getFactory().createThread(new Runnable() {
				@Override
				public void run() {
					for(ReportEntity re : Main.getDatenServer().getClient().getReportEntity(RequestType.OPEN_REPORTS, -1).getSync())
							Main.getDatenServer().getClient().closeReport(re, ReportState.UNDEFINED_CLOSED.ordinal());
					cs.sendMessage("§aDone!");
				}
			}).start();
			return;
		}
		else if(args.length == 1 && args[0].equalsIgnoreCase("test") && PermissionManager.getManager().hasPermission(cs, "report.test")){
			cs.sendMessage("§aCrating reports....");
			ThreadFactory.getFactory().createThread(new Runnable() {
				@Override
				public void run() {
					LoadedPlayer lplayer = Main.getDatenServer().getClient().getPlayerAndLoad(cs.getName());
					Random r = new Random();
					for(int i = 0;i<100;i++)
						Main.getDatenServer().getClient().createReport(lplayer.getPlayerId(), (int) (Math.abs(r.nextInt())%300000), "Test", "kp");
					cs.sendMessage("§aDone!");
				}
			}).start();
			return;
		}
		Player p = (Player) cs;
		//new PlayerSarchMenue(p).open();
		GuiPlayerMenue menue = new GuiPlayerMenue();
		menue.setPlayer(p);
		menue.openGui();
	}
}
