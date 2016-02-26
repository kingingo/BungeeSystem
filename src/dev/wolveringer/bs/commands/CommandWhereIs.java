package dev.wolveringer.bs.commands;

import java.util.UUID;

import dev.wolveringer.bs.Main;
import dev.wolveringer.bs.client.event.ServerMessageEvent;
import dev.wolveringer.client.connection.ClientType;
import dev.wolveringer.dataclient.protocoll.DataBuffer;
import me.kingingo.kBungeeCord.Language.Language;
import me.kingingo.kBungeeCord.Permission.PermissionManager;
import me.kingingo.kBungeeCord.Permission.PermissionType;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class CommandWhereIs extends Command implements Listener {

	public CommandWhereIs(String name) {
		super(name);
		BungeeCord.getInstance().getPluginManager().registerListener(Main.getInstance(), this);
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		ProxiedPlayer p = (ProxiedPlayer) sender;
		
		if (args.length==0) {
			p.sendMessage(Language.getText(p, "PREFIX")+Language.getText(p, "BG_WHEREIS",new String[]{p.getName(),p.getServer().getInfo().getName(),Main.getInstance().getServerId()}));
			return;
		}
		
		if (PermissionManager.getManager().hasPermission(p, PermissionType.WHERE_IS,true)) {
			if (args.length == 1) {
				String name = args[0];
				if(BungeeCord.getInstance().getPlayer(name) != null){
					p.sendMessage(Language.getText(p, "PREFIX")+Language.getText(p, "BG_WHEREIS",new String[]{name,BungeeCord.getInstance().getPlayer(name).getServer().getInfo().getName(),Main.getInstance().getServerId()}));
				}else{
					p.sendMessage(Language.getText(p, "PREFIX")+Language.getText(p, "BG_WHEREIS_SEARCH", name));
					Main.getDatenServer().getClient().sendServerMessage(ClientType.BUNGEECORD, "whereis", new DataBuffer().writeUUID(p.getUniqueId()).writeString(name));
				}
			}
		}
	}
	@EventHandler
	public void a(ServerMessageEvent e){
		if(e.getChannel().equalsIgnoreCase("whereis")){
			UUID player = e.getBuffer().readUUID();
			String user = e.getBuffer().readString();
			if(BungeeCord.getInstance().getPlayer(user) != null){
				ProxiedPlayer p = BungeeCord.getInstance().getPlayer(user);
				Main.getDatenServer().getClient().sendMessage(player, Language.getText(p, "PREFIX")+Language.getText(p, "BG_WHEREIS",new String[]{user,BungeeCord.getInstance().getPlayer(user).getServer().getInfo().getName(),Main.getInstance().getServerId()}));
			}
		}
	}

}
