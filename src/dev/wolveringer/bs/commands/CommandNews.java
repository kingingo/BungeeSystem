package dev.wolveringer.bs.commands;

import dev.wolveringer.bs.Main;
import lombok.Getter;
import me.kingingo.kBungeeCord.Language.Language;
import me.kingingo.kBungeeCord.Language.LanguageType;
import me.kingingo.kBungeeCord.Permission.PermissionManager;
import me.kingingo.kBungeeCord.Permission.PermissionType;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Listener;

public class CommandNews extends Command implements Listener {

	public CommandNews(String name) {
		super(name);
		BungeeCord.getInstance().getPluginManager().registerListener(instance, this);
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		if(!(sender instanceof ProxiedPlayer))return;
		ProxiedPlayer player =(ProxiedPlayer)sender;
		if (PermissionManager.getManager().hasPermission(player, PermissionType.MOTD,true)) {
			if(args.length==0){
				player.sendMessage(Language.getText(player, "PREFIX")+"/news list");
				player.sendMessage(Language.getText(player, "PREFIX")+"/news load");
				player.sendMessage(Language.getText(player, "PREFIX")+"/news remove [B/T] [LANGUAGE] [ID]");
				player.sendMessage(Language.getText(player, "PREFIX")+"/news [B/T] [LANGUAGE] [NEWS]");
			}else{
				if(args[0].equalsIgnoreCase("list")){
					for(String type : getInstance().getNews().getNews().keySet()){
						for(LanguageType language : getInstance().getNews().getNews().get(type).keySet()){
							for(int i = 0 ; i<getInstance().getNews().getNews().get(type).get(language).size(); i++){
								player.sendMessage("§c"+type+" §7L:§e"+language.getDef()+" §7ID:§e"+i+" §7Title:§e"+getInstance().getNews().getNews().get(type).get(language).get(i));
							}
						}
					}
				}else if(args[0].equalsIgnoreCase("remove")){
					if(args[1].toLowerCase().startsWith("b"))args[1]="BROADCAST";
					if(args[1].toLowerCase().startsWith("t"))args[1]="TITLE";
					
					if(getInstance().getNews().getNews().containsKey(args[1].toUpperCase())){
						player.sendMessage("§cWurde entfernt..");
						player.sendMessage(instance.getNews().getNews().get(args[1]).get(LanguageType.get(args[2])).get(Integer.valueOf(args[3])));
						instance.getNews().loadNews();
					}
				}else if(args[0].equalsIgnoreCase("load")){
					player.sendMessage("§aNews reload...");
					instance.getNews().loadNews();
				}else{

					if(args[0].toLowerCase().startsWith("b"))args[0]="BROADCAST";
					if(args[0].toLowerCase().startsWith("t"))args[0]="TITLE";
					
						String m = "";
						StringBuilder sb = new StringBuilder();
						for (int i = 2; i < args.length; i++) {
							sb.append(args[i]);
							sb.append(" ");
						}
						sb.setLength(sb.length() - 1);
						m = sb.toString();
						instance.getNews().loadNews();
						player.sendMessage("§awurde hinzugefuegt..");
						player.sendMessage(m);
				}
			}
		}
	}
}
