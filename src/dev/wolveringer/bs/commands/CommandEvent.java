package dev.wolveringer.bs.commands;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import dev.wolveringer.arrays.CachedArrayList;
import dev.wolveringer.bs.Main;
import dev.wolveringer.bs.client.event.ServerMessageEvent;
import dev.wolveringer.client.connection.ClientType;
import dev.wolveringer.dataclient.protocoll.DataBuffer;
import me.kingingo.kBungeeCord.Language.Language;
import me.kingingo.kBungeeCord.Permission.PermissionManager;
import me.kingingo.kBungeeCord.Permission.PermissionType;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class CommandEvent extends Command implements Listener {

	private ServerInfo server = null;
	private boolean active = false;
	
	private CachedArrayList<UUID> connections = new CachedArrayList<>(2, TimeUnit.SECONDS);
	public CommandEvent(String name) {
		super(name);
		BungeeCord.getInstance().getPluginManager().registerListener(Main.getInstance(), this);
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		ProxiedPlayer p = (ProxiedPlayer)sender;
		 	
		if (args.length == 0 ){
	           if(active){
	        	   if(connections.size() >= 20){
	        		   p.sendMessage(Language.getText(p, "PREFIX")+Language.getText(p, "BG_EVENT_SERVER_WAIT"));
	        		   return;
	        	   }
	        	   connections.add(UUID.randomUUID());
	        	   p.sendMessage(Language.getText(p, "PREFIX")+Language.getText(p, "BG_EVENT_SERVER"));
	        	   p.connect(server);
	           }else{
	    		   p.sendMessage(Language.getText(p, "PREFIX")+Language.getText(p, "BG_EVENT_NOT_NOW"));
	           }
	    }else if(args[0].equalsIgnoreCase("toggle")&& PermissionManager.getManager().hasPermission(p, PermissionType.ALL_PERMISSION,true)){
	    	if(active){
	    		active=false;
	    		p.sendMessage(Language.getText(p, "PREFIX")+"§6Event:§a "+active);
	    	}else{
	    		active=true;
	    		p.sendMessage(Language.getText(p, "PREFIX")+"§6Event:§c "+active);
	    	}
	    	Main.getDatenServer().getClient().sendServerMessage(ClientType.BUNGEECORD, "eventServer", new DataBuffer().writeByte(1).writeBoolean(active));
	    }else if(args[0].equalsIgnoreCase("set")&& PermissionManager.getManager().hasPermission(p, PermissionType.ALL_PERMISSION,true)){
	    	if(args.length==2){
	    		if(BungeeCord.getInstance().getServerInfo(args[1])!=null){
	    	    	server=BungeeCord.getInstance().getServerInfo(args[1]);
	    	    	Main.getDatenServer().getClient().sendServerMessage(ClientType.BUNGEECORD, "eventServer", new DataBuffer().writeByte(0).writeString(BungeeCord.getInstance().getServerInfo(args[1]).getName()));
		    		p.sendMessage(Language.getText(p, "PREFIX")+"§6Der Event server wurde gesetzt!");
		    		return;
	    		}
	    	}
	    	server=p.getServer().getInfo();
	    	Main.getDatenServer().getClient().sendServerMessage(ClientType.BUNGEECORD, "eventServer", new DataBuffer().writeByte(0).writeString(server.getName()));
	    	p.sendMessage(Language.getText(p, "PREFIX")+"§6Der Event server wurde auf deinem Server gesetzt!");
	    }
	}
	@EventHandler
	public void a(ServerMessageEvent e){
		if(e.getChannel().equalsIgnoreCase("eventServer")){
			byte action = e.getBuffer().readByte();
			if(action == 0){
				server = BungeeCord.getInstance().getServerInfo(e.getBuffer().readString());
			}
			else if(action == 1){
				active = e.getBuffer().readBoolean();
			}
		}
	}
}
