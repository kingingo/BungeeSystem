package dev.wolveringer.bs.commands;

import lombok.Getter;
import me.kingingo.kBungeeCord.kBungeeCord;
import me.kingingo.kBungeeCord.Language.Language;
import me.kingingo.kBungeeCord.Login.Events.PlayerAcceptEvent;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Listener;

public class CommandLogin extends Command implements Listener {

	@Getter
	private kBungeeCord instance;
	
	public CommandLogin(String name,kBungeeCord instance) {
		super(name);
		this.instance=instance;
		BungeeCord.getInstance().getPluginManager().registerListener(instance, this);
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		ProxiedPlayer p = (ProxiedPlayer) sender;
		
		if(instance.getLoginManager().is(p)){
			if(args.length==0){
				p.sendMessage(Language.getText(p,"PREFIX")+"/login [Password]");
			}else{
				if(getInstance().getLoginManager().isRegister(p)){
					p.sendMessage(Language.getText(p,"PREFIX")+"/register [Password]");
				}else{
					if(getInstance().getLoginManager().getPws().get(p.getName().toLowerCase()).equalsIgnoreCase(args[0])){
						getInstance().getLoginManager().remove(p);
						p.sendMessage(Language.getText(p, "PREFIX")+Language.getText(p, "LOGIN_ACCEPT"));
						p.connect(instance.getLobbybalancer().nextLobby());
						BungeeCord.getInstance().getPluginManager().callEvent(new PlayerAcceptEvent(p));
					}else{
						p.sendMessage(Language.getText(p, "PREFIX")+Language.getText(p, "LOGIN_DENY"));
					}
				}
			}
		}
	}
}
